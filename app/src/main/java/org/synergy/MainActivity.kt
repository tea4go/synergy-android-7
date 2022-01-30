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

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import org.synergy.base.utils.Log
import org.synergy.services.BarrierAccessibilityService
import org.synergy.services.BarrierClientService
import org.synergy.services.BarrierClientService.Companion.EXTRA_CLIENT_NAME
import org.synergy.services.BarrierClientService.Companion.EXTRA_IP_ADDRESS
import org.synergy.services.BarrierClientService.Companion.EXTRA_PORT
import org.synergy.services.BarrierClientService.Companion.EXTRA_SCREEN_HEIGHT
import org.synergy.services.BarrierClientService.Companion.EXTRA_SCREEN_WIDTH
import org.synergy.utils.AccessibilityUtils
import org.synergy.utils.Constants.SILENT_NOTIFICATIONS_CHANNEL_ID
import org.synergy.utils.Constants.SILENT_NOTIFICATIONS_CHANNEL_NAME
import org.synergy.utils.DisplayUtils

class MainActivity : Activity() {
    private var barrierClientServiceBound: Boolean = false
    private var barrierClientService: BarrierClientService? = null
    private var barrierClientConnected: Boolean = false

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
                        updateConnectButton()
                    }
                }
            barrierClientServiceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            barrierClientService = null
            barrierClientServiceBound = false
        }
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)
        createNotificationChannels()
        val preferences = getPreferences(MODE_PRIVATE)
        val clientName = preferences.getString(PROP_clientName, null)
        if (clientName != null) {
            (findViewById<EditText>(R.id.clientNameEditText)).setText(clientName)
        }
        val serverHost = preferences.getString(PROP_serverHost, null)
        if (serverHost != null) {
            (findViewById<EditText>(R.id.serverHostEditText)).setText(serverHost)
        }

        // TODO make sure we have the appropriate permissions for the accessibility services. Otherwise display error/open settings intent
        val connectButton = findViewById<Button>(R.id.connectButton)
        connectButton.setOnClickListener { connect() }
        if (BuildConfig.DEBUG) {
            Log.setLogLevel(Log.Level.NOTE)
        } else {
            Log.setLogLevel(Log.Level.ERROR)
        }
    }

    override fun onResume() {
        super.onResume()
        // Keep checking for revoked permissions
        requestOverlayDrawingPermission()
        requestAccessibilityPermission()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == OVERLAY_DRAWING_REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
                Toast.makeText(
                    this,
                    "Draw over other apps permission denied",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
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
                Uri.parse(
                    "package:$packageName"
                )
            )
            startActivityForResult(intent, OVERLAY_DRAWING_REQUEST_CODE)
        }
    }

    private fun requestAccessibilityPermission() {
        val enabled = AccessibilityUtils.isAccessibilityServiceEnabled(
            this,
            BarrierAccessibilityService::class.java
        )
        Log.debug("accessibility enabled: $enabled")
        if (!enabled) {
            // show dialog
        }
    }

    private fun connect() {
        val clientName = (findViewById<EditText>(R.id.clientNameEditText)).text.toString()
        val ipAddress = (findViewById<EditText>(R.id.serverHostEditText)).text.toString()
        val portStr = (findViewById<EditText>(R.id.serverPortEditText)).text.toString()
        val port = portStr.toInt()
        val deviceName = (findViewById<EditText>(R.id.inputDeviceEditText)).text.toString()
        val preferences = getPreferences(MODE_PRIVATE)
        val preferencesEditor = preferences.edit()
        preferencesEditor.putString(PROP_clientName, clientName)
        preferencesEditor.putString(PROP_serverHost, ipAddress)
        preferencesEditor.putString(PROP_deviceName, deviceName)
        preferencesEditor.apply()

        val displayBounds = DisplayUtils.getDisplayBounds(this)
        if (displayBounds == null) {
            Log.error("displayBounds is null")
            Toast.makeText(applicationContext, "displayBounds is null", Toast.LENGTH_LONG).show()
            return
        }

        val intent = Intent(
            this,
            BarrierClientService::class.java,
        ).apply {
            putExtra(EXTRA_IP_ADDRESS, ipAddress)
            putExtra(EXTRA_PORT, port)
            putExtra(EXTRA_CLIENT_NAME, clientName)
            putExtra(EXTRA_SCREEN_WIDTH, displayBounds.width())
            putExtra(EXTRA_SCREEN_HEIGHT, displayBounds.height())
        }

        ContextCompat.startForegroundService(applicationContext, intent)
        if (!barrierClientServiceBound) {
            bindService(
                Intent(this, BarrierClientService::class.java),
                serviceConnection,
                BIND_AUTO_CREATE
            )
        }
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

    private fun updateConnectButton() = runOnUiThread {
        val connectButton = findViewById<Button>(R.id.connectButton)
        if (barrierClientConnected) {
            connectButton.text = getString(R.string.disconnect)
            connectButton.setOnClickListener { disconnect() }
        } else {
            connectButton.text = getString(R.string.connect)
            connectButton.setOnClickListener { connect() }
        }
    }

    companion object {
        private const val PROP_clientName = "clientName"
        private const val PROP_serverHost = "serverHost"
        private const val PROP_deviceName = "deviceName"

        private const val OVERLAY_DRAWING_REQUEST_CODE = 4711
    }
}