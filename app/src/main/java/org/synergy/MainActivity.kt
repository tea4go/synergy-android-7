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
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import org.synergy.base.Event
import org.synergy.base.EventQueue
import org.synergy.base.EventType
import org.synergy.base.utils.Log
import org.synergy.client.Client
import org.synergy.common.screens.BasicScreen
import org.synergy.net.NetworkAddress
import org.synergy.net.SocketFactoryInterface
import org.synergy.net.SynergyConnectTask
import org.synergy.net.TCPSocketFactory

class MainActivity : Activity() {
    private var mainLoopThread: Thread? = null

    fun addOverlay() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse(
                        "package:$packageName"
                    )
                )
                startActivityForResult(intent, 4711)
            } else {
                //startService(new Intent(this, MouseAccessibility.class));
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 4711) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(this)) {
                    Toast.makeText(this, "Starting Mouse Service", Toast.LENGTH_SHORT).show()
                    //startService(new Intent(this, MouseAccessibility.class));
                } else {
                    Toast.makeText(
                        this,
                        "ACTION_MANAGE_OVERLAY_PERMISSION Permission Denied",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private inner class MainLoopThread : Thread() {
        override fun run() {
            try {
                addOverlay()
                var event = Event()
                event = EventQueue.getInstance().getEvent(event, -1.0)
                Log.note("Event grabbed")
                while (event.type != EventType.QUIT && mainLoopThread === currentThread()) {
                    EventQueue.getInstance().dispatchEvent(event)
                    // TODO event.deleteData ();
                    event = EventQueue.getInstance().getEvent(event, -1.0)
                    Log.note("Event grabbed")
                }
                mainLoopThread = null
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                // TODO stop the accessibility injection service
            }
        }
    }

    /**
     * Called when the activity is first created.
     */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)
        val preferences = getPreferences(MODE_PRIVATE)
        val clientName = preferences.getString(PROP_clientName, null)
        if (clientName != null) {
            (findViewById<View>(R.id.clientNameEditText) as EditText).setText(clientName)
        }
        val serverHost = preferences.getString(PROP_serverHost, null)
        if (serverHost != null) {
            (findViewById<View>(R.id.serverHostEditText) as EditText).setText(serverHost)
        }

        // TODO make sure we have the appropriate permissions for the accessibility services. Otherwise display error/open settings intent
        val connectButton = findViewById<View>(R.id.connectButton) as Button
        // connect when clicked on the connectButton
        connectButton.setOnClickListener { connect() }
        Log.setLogLevel(Log.Level.DEBUG)
        Toast.makeText(applicationContext, "Client Starting", Toast.LENGTH_LONG).show()
        Log.debug("Client starting....")
    }

    private fun connect() {
        val clientName = (findViewById<View>(R.id.clientNameEditText) as EditText).text.toString()
        val ipAddress = (findViewById<View>(R.id.serverHostEditText) as EditText).text.toString()
        val portStr = (findViewById<View>(R.id.serverPortEditText) as EditText).text.toString()
        val port = portStr.toInt()
        val deviceName = (findViewById<View>(R.id.inputDeviceEditText) as EditText).text.toString()
        val preferences = getPreferences(MODE_PRIVATE)
        val preferencesEditor = preferences.edit()
        preferencesEditor.putString(PROP_clientName, clientName)
        preferencesEditor.putString(PROP_serverHost, ipAddress)
        preferencesEditor.putString(PROP_deviceName, deviceName)
        preferencesEditor.apply()
        try {
            val socketFactory: SocketFactoryInterface = TCPSocketFactory()
            val serverAddress = NetworkAddress(ipAddress, port)

            // TODO start the accessibility service injection here
            val basicScreen = BasicScreen()
            val wm = windowManager
            val display = wm.defaultDisplay
            basicScreen.setShape(display.width, display.height)
            Log.debug("Resolution: " + display.width + " x " + display.height)


            //PlatformIndependentScreen screen = new PlatformIndependentScreen(basicScreen);
            Log.debug("Hostname: $clientName")
            val client = Client(
                applicationContext,
                clientName,
                serverAddress,
                socketFactory,
                null,
                basicScreen
            )
            SynergyConnectTask().execute(client)
            Toast.makeText(applicationContext, "Device Connected", Toast.LENGTH_LONG).show()

            // TODO this looks quite hacky
            if (mainLoopThread == null) {
                MainLoopThread().also {
                    mainLoopThread = it
                }.start()
            }
        } catch (e: Exception) {
            Toast.makeText(applicationContext, "Connection Failed", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    companion object {
        private const val PROP_clientName = "clientName"
        private const val PROP_serverHost = "serverHost"
        private const val PROP_deviceName = "deviceName"
    }
}