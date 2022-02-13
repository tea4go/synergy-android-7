package org.synergy.ui.screens.home

import android.app.Application
import android.os.Build
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.synergy.data.db.entities.ServerConfig
import org.synergy.data.repositories.ServerConfigRepository
import org.synergy.services.BarrierAccessibilityService
import org.synergy.utils.AccessibilityUtils
import javax.inject.Inject

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    application: Application,
    private val serverConfigRepository: ServerConfigRepository,
) : AndroidViewModel(application) {
    private val _uiState: MutableStateFlow<UiState> = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    init {
        viewModelScope.launch {
            serverConfigRepository.getAll().collectLatest { serverConfigs ->
                _uiState.update {
                    it.copy(
                        serverConfigs = serverConfigs,
                        selectedConfigId = it.selectedConfigId ?: serverConfigs.firstOrNull()?.id,
                    )
                }
            }
        }
        checkPermissions()
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

    fun setBarrierClientConnected(connected: Boolean) {
        _uiState.update { it.copy(barrierClientConnected = connected) }
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
    val barrierClientConnected: Boolean = false,
    val showOverlayDrawPermissionDialog: Boolean = false,
    val showAccessibilityPermissionDialog: Boolean = false,
    val showAddServerConfigDialog: Boolean = false,
    val editServerConfig: ServerConfig? = null,
    val connectedServerConfigId: Long? = null,
) {
    val connectedServerConfig: ServerConfig?
        get() = serverConfigs.find { it.id == connectedServerConfigId }
}