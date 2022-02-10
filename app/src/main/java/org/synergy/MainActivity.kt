/*
 * synergy -- mouse and keyboard sharing utility
 * Copyright (C) 2010 Shaun Patterson
 * Copyright (C) 2010 The Synergy Project
 * Copyright (C) 2009 The Synergy+ Project
 * Copyright (C) 2002 Chris Schoeneman
 *
 * This package is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * found in the file COPYING that should have accompanied this file.
 *
 * This package is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.synergy

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import org.synergy.barrier.base.utils.Timber
import org.synergy.barrier.base.utils.d
import org.synergy.barrier.base.utils.e
import org.synergy.services.BarrierAccessibilityService
import org.synergy.services.BarrierClientService
import org.synergy.services.BarrierClientService.Companion.EXTRA_CLIENT_NAME
import org.synergy.services.BarrierClientService.Companion.EXTRA_IP_ADDRESS
import org.synergy.services.BarrierClientService.Companion.EXTRA_PORT
import org.synergy.services.BarrierClientService.Companion.EXTRA_SCREEN_HEIGHT
import org.synergy.services.BarrierClientService.Companion.EXTRA_SCREEN_WIDTH
import org.synergy.ui.screens.home.HomeScreen
import org.synergy.utils.AccessibilityUtils
import org.synergy.utils.Constants.SILENT_NOTIFICATIONS_CHANNEL_ID
import org.synergy.utils.Constants.SILENT_NOTIFICATIONS_CHANNEL_NAME
import org.synergy.utils.DisplayUtils

class MainActivity : ComponentActivity() {
    private var barrierClientServiceBound: Boolean = false
    private var barrierClientService: BarrierClientService? = null
    private var barrierClientConnected by mutableStateOf(false)

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            if (service !is BarrierClientService.LocalBinder) {
                return
            }
            service.service
                .also { barrierClientService = it }
                .apply {
                    addOnConnectionChangeListener {
                        barrierClientConnected = it
                    }
                }
            barrierClientServiceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            barrierClientService = null
            barrierClientServiceBound = false
        }
    }

    private val overlayPermActivityLauncher = registerForActivityResult(StartActivityForResult()) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(this)) {
            return@registerForActivityResult
        }
        Toast.makeText(
            this,
            getString(R.string.overlay_permission_denied),
            Toast.LENGTH_SHORT
        ).show()
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HomeScreen(
                barrierClientConnected = barrierClientConnected,
                onConnectClick = this::connect,
                disconnect = this::disconnect,
            )
        }
        createNotificationChannels()
    }

    override fun onResume() {
        super.onResume()
        // Keep checking for revoked permissions
        requestOverlayDrawingPermission()
        requestAccessibilityPermission()
        bindToClientService(autoCreate = false)
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(serviceConnection)
    }

    private fun requestOverlayDrawingPermission() {
        // TODO: Need to first show dialog to explain the request, and what the user has to do

        // For pre-API 23, overlay drawing permission is granted by default
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            overlayPermActivityLauncher.launch(intent)
        }
    }

    private fun requestAccessibilityPermission() {
        val enabled = AccessibilityUtils.isAccessibilityServiceEnabled(
            this,
            BarrierAccessibilityService::class.java
        )
        Timber.d("accessibility enabled: $enabled")
        if (!enabled) {
            // show dialog
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivity(intent)
        }
    }

    private fun connect(
        clientName: String,
        serverHost: String,
        serverPort: Int,
        deviceName: String,
    ) {
        val displayBounds = DisplayUtils.getDisplayBounds(this)
        if (displayBounds == null) {
            Timber.e("displayBounds is null")
            Toast.makeText(applicationContext, "displayBounds is null", Toast.LENGTH_LONG).show()
            return
        }

        val intent = Intent(
            this,
            BarrierClientService::class.java,
        ).apply {
            putExtra(EXTRA_IP_ADDRESS, serverHost)
            putExtra(EXTRA_PORT, serverPort)
            putExtra(EXTRA_CLIENT_NAME, clientName)
            putExtra(EXTRA_SCREEN_WIDTH, displayBounds.width())
            putExtra(EXTRA_SCREEN_HEIGHT, displayBounds.height())
        }

        ContextCompat.startForegroundService(applicationContext, intent)
        if (!barrierClientServiceBound) {
            bindToClientService()
        }
    }

    private fun bindToClientService(autoCreate: Boolean = true) {
        bindService(
            Intent(this, BarrierClientService::class.java),
            serviceConnection,
            if (autoCreate) BIND_AUTO_CREATE else 0
        )
    }

    private fun disconnect() {
        barrierClientService?.disconnect()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val silentNotificationChannel = NotificationChannelCompat.Builder(
            SILENT_NOTIFICATIONS_CHANNEL_ID,
            NotificationManagerCompat.IMPORTANCE_LOW
        ).apply {
            setName(SILENT_NOTIFICATIONS_CHANNEL_NAME)
            setSound(null, null)
        }.build()
        NotificationManagerCompat.from(applicationContext).run {
            createNotificationChannel(silentNotificationChannel)
        }
    }
}