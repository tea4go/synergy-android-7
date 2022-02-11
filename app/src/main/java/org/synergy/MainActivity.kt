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

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat
import org.synergy.barrier.base.utils.Timber
import org.synergy.barrier.base.utils.d
import org.synergy.services.BarrierAccessibilityService
import org.synergy.utils.AccessibilityUtils
import org.synergy.utils.Constants.SILENT_NOTIFICATIONS_CHANNEL_ID
import org.synergy.utils.Constants.SILENT_NOTIFICATIONS_CHANNEL_NAME

class MainActivity : ComponentActivity() {

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainContent()
        }
        createNotificationChannels()
    }

    override fun onResume() {
        super.onResume()
        // Keep checking for revoked permissions
        requestAccessibilityPermission()
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