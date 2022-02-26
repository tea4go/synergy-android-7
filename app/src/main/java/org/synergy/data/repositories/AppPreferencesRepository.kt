package org.synergy.data.repositories

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.synergy.data.preferences.AppPreferences
import org.synergy.data.preferences.PreferencesKeys
import org.synergy.utils.Timber
import org.synergy.utils.e
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppPreferencesRepository @Inject constructor(private val dataStore: DataStore<Preferences>) {

    val appPreferencesFlow: Flow<AppPreferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Timber.e("Error reading preferences", exception)
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { mapAppPreferences(it) }
        .flowOn(Dispatchers.IO)

    private fun mapAppPreferences(preferences: Preferences) = AppPreferences(
        selectedServerConfigId = preferences[PreferencesKeys.SELECTED_SERVER_CONFIG_ID],
    )

    suspend fun updateSelectedServerConfigId(serverConfigId: Long) = withContext(Dispatchers.IO) {
        dataStore.edit { it[PreferencesKeys.SELECTED_SERVER_CONFIG_ID] = serverConfigId }
    }
}