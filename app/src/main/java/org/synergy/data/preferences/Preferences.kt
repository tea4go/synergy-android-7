package org.synergy.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

private const val APP_PREFERENCES_NAME = "app_preferences"

val Context.dataStore by preferencesDataStore(name = APP_PREFERENCES_NAME)

object PreferencesKeys {
    val SELECTED_SERVER_CONFIG_ID = longPreferencesKey("selected_server_config_id")
}

data class AppPreferences(
    val selectedServerConfigId: Long?,
)
