package org.synergy.services

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import org.synergy.MainActivity
import org.synergy.R
import org.synergy.barrier.base.EventQueue
import org.synergy.barrier.base.EventType
import org.synergy.utils.Timber
import org.synergy.utils.d1
import org.synergy.utils.e
import org.synergy.barrier.client.Client
import org.synergy.barrier.common.screens.BasicScreen
import org.synergy.barrier.net.NetworkAddress
import org.synergy.barrier.net.SocketFactoryInterface
import org.synergy.barrier.net.TCPSocketFactory
import org.synergy.services.ConnectionStatus.*
import org.synergy.utils.Constants.BARRIER_CLIENT_SERVICE_ONGOING_NOTIFICATION_ID
import org.synergy.utils.Constants.SILENT_NOTIFICATIONS_CHANNEL_ID
import javax.inject.Inject

@AndroidEntryPoint
class BarrierClientService : Service() {
    @Inject
    lateinit var eventQueue: EventQueue
    var configId: Long? = null
        private set
    private var client: Client? = null
    private val binder: IBinder = LocalBinder()
    private var quitEventLoop = false
    private var job = SupervisorJob()
    private var coroutineScope = CoroutineScope(Dispatchers.IO + job)
    private val onConnectionStatusChangeListeners = mutableListOf<(ConnectionStatus) -> Unit>()

    var connectionStatus: ConnectionStatus = Disconnected()
        private set(value) {
            field = value
            onConnectionStatusChangeListeners.forEach { it(value) }
        }

    inner class LocalBinder : Binder() {
        val service: BarrierClientService = this@BarrierClientService
    }

    override fun onBind(intent: Intent): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        showNotification()
        return START_STICKY
    }

    private fun showNotification() {
        val pendingIntent: PendingIntent = Intent(
            this,
            MainActivity::class.java
        ).let {
            val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            PendingIntent.getActivity(
                this,
                0,
                it,
                pendingIntentFlags,
            )
        }

        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, SILENT_NOTIFICATIONS_CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
        }
        val notification: Notification = builder.apply {
            setContentTitle(getText(R.string.app_name))
            setContentText("Barrier client running")
            setSmallIcon(R.drawable.icon)
            setContentIntent(pendingIntent)
            setTicker("Barrier client running")
        }.build()

        startForeground(BARRIER_CLIENT_SERVICE_ONGOING_NOTIFICATION_ID, notification)
    }

    fun connect(
        configId: Long,
        ipAddress: String,
        port: Int,
        clientName: String,
        screenWidth: Int,
        screenHeight: Int,
    ) {
        this.configId = configId

        // if connected to any server, disconnect from it
        if (connectionStatus != Disconnected()) {
            client?.disconnect()
            client = null
            connectionStatus = Disconnected()
        }

        if (job.isCancelled) {
            job = SupervisorJob()
            coroutineScope = CoroutineScope(Dispatchers.IO + job)
        }

        connectionStatus = Connecting
        val socketFactory: SocketFactoryInterface = TCPSocketFactory(eventQueue)
        val serverAddress = NetworkAddress(ipAddress, port)
        val basicScreen = BasicScreen(this)
        basicScreen.setShape(screenWidth, screenHeight)
        Timber.d1("Resolution: $screenWidth x $screenHeight")

        client = Client(
            clientName,
            serverAddress,
            socketFactory,
            basicScreen,
            eventQueue,
        ) { connected ->
            if (connected) {
                connectionStatus = Connected
                startService(Intent(this, BarrierAccessibilityService::class.java))
                return@Client
            }
            connectionStatus = Disconnected()
            stopService(Intent(this, BarrierAccessibilityService::class.java))
            stopForeground(true)
            stopSelf()
        }

        quitEventLoop = false
        coroutineScope.launch {
            try {
                @Suppress("BlockingMethodInNonBlockingContext")
                client?.connect()
                startEventQueue()
            } catch (e: Exception) {
                Timber.e("Error:", e)
                connectionStatus = Disconnected(e)
                stopForeground(true)
                stopSelf()
            }
        }
    }

    private fun startEventQueue() {
        var event = eventQueue.getEvent(-1.0) ?: return
        while (!quitEventLoop && event.type != EventType.QUIT) {
            eventQueue.dispatchEvent(event)
            // TODO event.deleteData ();
            event = eventQueue.getEvent(-1.0) ?: return
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }

    fun addOnConnectionStatusChangeListener(listener: (ConnectionStatus) -> Unit) {
        onConnectionStatusChangeListeners.add(listener)
    }

    fun disconnect() {
        client?.disconnect()
        quitEventLoop = true
    }
}

sealed class ConnectionStatus {
    object Connected : ConnectionStatus()
    object Connecting : ConnectionStatus()
    class Disconnected(error: Throwable? = null) : ConnectionStatus()
}
