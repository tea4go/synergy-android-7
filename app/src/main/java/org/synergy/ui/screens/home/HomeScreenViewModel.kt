package org.synergy.ui.screens.home

import android.app.Application
import android.os.Build
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.synergy.data.db.entities.ServerConfig
import org.synergy.data.preferences.AppPreferences
import org.synergy.data.repositories.AppPreferencesRepository
import org.synergy.data.repositories.ServerConfigRepository
import org.synergy.services.BarrierAccessibilityService
import org.synergy.services.ConnectionStatus
import org.synergy.utils.AccessibilityUtils
import javax.inject.Inject

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    application: Application,
    private val serverConfigRepository: ServerConfigRepository,
    private val appPreferencesRepository: AppPreferencesRepository,
) : AndroidViewModel(application) {
    private val _uiState: MutableStateFlow<UiState> = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    init {
        viewModelScope.launch {
            combine(
                serverConfigRepository.getAll(),
                appPreferencesRepository.appPreferencesFlow,
            ) { serverConfigs, appPreferences ->
                _uiState.update {
                    val selectedConfigId = getSelectedConfigId(it, appPreferences, serverConfigs)
                    it.copy(
                        serverConfigs = serverConfigs,
                        selectedConfigId = selectedConfigId,
                    )
                }
            }.collect()
        }
        checkPermissions()
    }

    private suspend fun getSelectedConfigId(
        it: UiState,
        appPreferences: AppPreferences,
        serverConfigs: List<ServerConfig>,
    ): Long? {
        if (it.selectedConfigId != null) {
            return it.selectedConfigId
        }
        if (appPreferences.selectedServerConfigId != null) {
            return appPreferences.selectedServerConfigId
        }
        val first = serverConfigs.firstOrNull()
        if (first != null) {
            appPreferencesRepository.updateSelectedServerConfigId(first.id)
            return first.id
        }
        return null
    }

    fun checkPermissions(): Pair<Boolean, Boolean> {
        // For pre-API 23, overlay drawing permission is granted by default
        val hasOverlayDrawPermission = (Build.VERSION.SDK_INT < Build.VERSION_CODES.M
            || Settings.canDrawOverlays(getApplication()))
        val hasAccessibilityPermission = AccessibilityUtils.isAccessibilityServiceEnabled(
            getApplication(),
            BarrierAccessibilityService::class.java
        )
        _uiState.update {
            it.copy(
                hasOverlayDrawPermission = hasOverlayDrawPermission,
                hasAccessibilityPermission = hasAccessibilityPermission,
            )
        }
        return Pair(hasOverlayDrawPermission, hasAccessibilityPermission)
    }

    fun setBarrierClientServiceBound(bound: Boolean) {
        _uiState.update { it.copy(barrierClientServiceBound = bound) }
    }

    fun setBarrierClientConnectionStatus(connectionStatus: ConnectionStatus) {
        _uiState.update { it.copy(barrierClientConnectionStatus = connectionStatus) }
    }

    fun setRequestedOverlayDrawPermission(requested: Boolean) {
        _uiState.update { it.copy(hasRequestedOverlayDrawPermission = requested) }
    }

    fun setRequestedAccessibilityPermission(requested: Boolean) {
        _uiState.update { it.copy(hasRequestedAccessibilityPermission = requested) }
    }

    fun setShowOverlayDrawPermissionDialog(show: Boolean) {
        _uiState.update { it.copy(showOverlayDrawPermissionDialog = show) }
    }

    fun setShowAccessibilityPermissionDialog(show: Boolean) {
        _uiState.update { it.copy(showAccessibilityPermissionDialog = show) }
    }

    fun setSelectedConfig(serverConfig: ServerConfig) {
        _uiState.update { it.copy(selectedConfigId = serverConfig.id) }
        viewModelScope.launch {
            appPreferencesRepository.updateSelectedServerConfigId(serverConfig.id)
        }
    }

    fun setShowAddServerConfigDialog(show: Boolean, editConfig: ServerConfig? = null) {
        _uiState.update {
            it.copy(
                showAddServerConfigDialog = show,
                editServerConfig = editConfig,
            )
        }
    }

    fun saveServerConfig(serverConfig: ServerConfig) {
        viewModelScope.launch {
            serverConfigRepository.save(serverConfig)
        }
    }

    fun setConnectedServerConfigId(connectedServerConfigId: Long?) {
        _uiState.update { it.copy(connectedServerConfigId = connectedServerConfigId) }
    }
}

data class UiState(
    val serverConfigs: List<ServerConfig> = emptyList(),
    val selectedConfigId: Long? = null,
    val hasRequestedOverlayDrawPermission: Boolean = false,
    val hasOverlayDrawPermission: Boolean = false,
    val hasRequestedAccessibilityPermission: Boolean = false,
    val hasAccessibilityPermission: Boolean = false,
    val barrierClientServiceBound: Boolean = false,
    val barrierClientConnectionStatus: ConnectionStatus = ConnectionStatus.Disconnected(),
    val showOverlayDrawPermissionDialog: Boolean = false,
    val showAccessibilityPermissionDialog: Boolean = false,
    val showAddServerConfigDialog: Boolean = false,
    val editServerConfig: ServerConfig? = null,
    val connectedServerConfigId: Long? = null,
) {
    val connectedServerConfig: ServerConfig?
        get() = serverConfigs.find { it.id == connectedServerConfigId }
}