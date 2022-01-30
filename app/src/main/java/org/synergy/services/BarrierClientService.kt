package org.synergy.services

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import kotlinx.coroutines.*
import org.synergy.MainActivity
import org.synergy.R
import org.synergy.base.EventQueue
import org.synergy.base.EventType
import org.synergy.base.utils.Log
import org.synergy.client.Client
import org.synergy.common.screens.BasicScreen
import org.synergy.net.NetworkAddress
import org.synergy.net.SocketFactoryInterface
import org.synergy.net.TCPSocketFactory
import org.synergy.utils.Constants.BARRIER_CLIENT_SERVICE_ONGOING_NOTIFICATION_ID
import org.synergy.utils.Constants.SILENT_NOTIFICATIONS_CHANNEL_ID


class BarrierClientService : Service() {

    private var client: Client? = null
    private val binder: IBinder = LocalBinder()
    private val job = SupervisorJob()
    private val coroutineScope = CoroutineScope(Dispatchers.IO + job)
    private val onConnectionChangeListeners = mutableListOf<(Boolean) -> Unit>()

    inner class LocalBinder : Binder() {
        val service: BarrierClientService = this@BarrierClientService
    }

    override fun onBind(intent: Intent): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) {
            Log.error("intent is null")
            return START_NOT_STICKY
        }

        val ipAddress = intent.getStringExtra(EXTRA_IP_ADDRESS)
        val port = intent.getIntExtra(EXTRA_PORT, -1)
        val clientName = intent.getStringExtra(EXTRA_CLIENT_NAME)
        val screenWidth = intent.getIntExtra(EXTRA_SCREEN_WIDTH, -1)
        val screenHeight = intent.getIntExtra(EXTRA_SCREEN_HEIGHT, -1)

        Log.debug("ipAddress: $ipAddress, port: $port, clientName: $clientName, resolution: ${screenWidth}x$screenHeight")

        if (ipAddress == null || port <= 0 || clientName == null || screenWidth < 0 || screenHeight < 0) {
            return START_NOT_STICKY
        }

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
        connect(
            ipAddress = ipAddress,
            port = port,
            clientName = clientName,
            screenWidth = screenWidth,
            screenHeight = screenHeight
        )
        return START_STICKY
    }

    private fun connect(
        ipAddress: String,
        port: Int,
        clientName: String,
        screenWidth: Int,
        screenHeight: Int,
    ) {
        val socketFactory: SocketFactoryInterface = TCPSocketFactory()
        val serverAddress = NetworkAddress(ipAddress, port)

        val basicScreen = BasicScreen()
        basicScreen.setShape(screenWidth, screenHeight)
        Log.debug("Resolution: $screenWidth x $screenHeight")

        client = Client(
            clientName,
            serverAddress,
            socketFactory,
            null,
            basicScreen
        ) { connected ->
            onConnectionChangeListeners.forEach { it(connected) }
            if (connected) {
                startService(Intent(this, BarrierAccessibilityService::class.java))
                return@Client
            }
            stopService(Intent(this, BarrierAccessibilityService::class.java))
            stopForeground(true)
            stopSelf()
        }

        coroutineScope.launch {
            try {
                @Suppress("BlockingMethodInNonBlockingContext")
                client?.connect()
                startEventQueue()
            } catch (e: Exception) {
                android.util.Log.e(TAG, "Error:", e)
                stopSelf()
            }
        }
    }

    private fun startEventQueue() {
        val eventQueue = EventQueue.getInstance()
        var event = eventQueue.getEvent(-1.0)
        while (event.type != EventType.QUIT) {
            eventQueue.dispatchEvent(event)
            // TODO event.deleteData ();
            event = eventQueue.getEvent(-1.0)
        }
        android.util.Log.d(TAG, "startEventQueue: $event")
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }

    fun addOnConnectionChangeListener(listener: (Boolean) -> Unit) {
        onConnectionChangeListeners.add(listener)
    }

    fun disconnect() {
        client?.disconnect(null)
    }

    companion object {
        private const val TAG = "BarrierClientService"

        const val EXTRA_IP_ADDRESS = "ip_address"
        const val EXTRA_PORT = "port"
        const val EXTRA_CLIENT_NAME = "client_name"
        const val EXTRA_SCREEN_WIDTH = "screen_width"
        const val EXTRA_SCREEN_HEIGHT = "screen_height"
    }
}