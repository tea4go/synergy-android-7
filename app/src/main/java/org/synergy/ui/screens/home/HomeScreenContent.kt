package org.synergy.ui.screens.home

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import org.synergy.R
import org.synergy.data.db.entities.ServerConfig
import org.synergy.services.ConnectionStatus
import org.synergy.services.ConnectionStatus.*
import org.synergy.ui.common.*
import org.synergy.ui.theme.BarrierClientTheme
import org.synergy.utils.ServerConfigListPreviewParameterProvider

@Composable
fun HomeScreenContent(
    modifier: Modifier = Modifier,
    serverConfigs: List<ServerConfig> = emptyList(),
    selectedConfigId: Long? = null,
    barrierClientConnectionStatus: ConnectionStatus = Disconnected(),
    hasOverlayDrawPermission: Boolean = false,
    hasAccessibilityPermission: Boolean = false,
    showOverlayDrawPermissionDialog: Boolean = false,
    showAccessibilityPermissionDialog: Boolean = false,
    showAddServerConfigDialog: Boolean = false,
    editServerConfig: ServerConfig? = null,
    connectedServerConfig: ServerConfig? = null,
    onServerConfigSelectionChange: (ServerConfig) -> Unit = {},
    onConnectClick: () -> Unit = {},
    onFixPermissionsClick: () -> Unit = {},
    onAcceptPermissionClick: () -> Unit = {},
    onDismissPermissionDialog: () -> Unit = {},
    onAddServerConfigClick: () -> Unit = {},
    onSaveServerConfig: (ServerConfig) -> Unit = {},
    onDismissAddServerConfigDialog: () -> Unit = {},
    onEditServerConfigClick: (ServerConfig) -> Unit = {},
) {
    val hasPermissions by remember(hasOverlayDrawPermission, hasAccessibilityPermission) {
        derivedStateOf { hasOverlayDrawPermission && hasAccessibilityPermission }
    }
    val selectedConfig by remember(serverConfigs, selectedConfigId) {
        derivedStateOf { serverConfigs.find { it.id == selectedConfigId } }
    }

    Column {
        if (!hasPermissions) {
            PermissionsBanner(
                hasAccessibilityPermission = hasAccessibilityPermission,
                hasOverlayDrawPermission = hasOverlayDrawPermission,
                onFixClick = onFixPermissionsClick,
            )
        }
        if (barrierClientConnectionStatus == Connected) {
            ConnectedStatus(
                serverConfig = connectedServerConfig,
            )
        }
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp)
                .weight(1f)
                .verticalScroll(state = rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = if (serverConfigs.isEmpty()) Arrangement.Center else Arrangement.Top,
        ) {
            if (serverConfigs.isEmpty()) {
                OutlinedButton(
                    onClick = onAddServerConfigClick,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "add"
                    )
                    Spacer(modifier = Modifier.requiredWidth(12.dp))
                    Text(
                        text = stringResource(id = R.string.add_server).uppercase(),
                        style = MaterialTheme.typography.button,
                    )
                }
            } else {
                ServerConfigDropdown(
                    serverConfigs = serverConfigs,
                    selectedConfigId = selectedConfigId,
                    onChange = onServerConfigSelectionChange,
                    onAddServerConfigClick = onAddServerConfigClick,
                )
            }
            selectedConfig?.let {
                Spacer(modifier = Modifier.requiredHeight(16.dp))
                ServerConfigDetail(
                    serverConfig = it,
                    onEditClick = { onEditServerConfigClick(it) },
                )
            }
        }
        if (selectedConfigId != null) {
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                enabled = when (barrierClientConnectionStatus) {
                    Connected -> true
                    Connecting -> false
                    else -> hasPermissions
                },
                onClick = onConnectClick,
            ) {
                Text(
                    style = MaterialTheme.typography.button,
                    text = stringResource(
                        id = when (barrierClientConnectionStatus) {
                            Connected -> R.string.disconnect
                            Connecting -> R.string.connecting
                            else -> R.string.connect
                        }
                    ).uppercase()
                )
            }
        }
    }

    if (showOverlayDrawPermissionDialog || showAccessibilityPermissionDialog) {
        AlertDialog(
            text = {
                Text(
                    text = stringResource(
                        when {
                            showOverlayDrawPermissionDialog -> R.string.requires_overlay_perm_detail
                            else -> R.string.requires_accessibility_perm_detail
                        }
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = onAcceptPermissionClick) {
                    Text(
                        text = stringResource(R.string.ok).uppercase(),
                        style = MaterialTheme.typography.button,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissPermissionDialog) {
                    Text(
                        text = stringResource(R.string.cancel).uppercase(),
                        style = MaterialTheme.typography.button,
                    )
                }
            },
            onDismissRequest = onDismissPermissionDialog,
        )
    }
    if (showAddServerConfigDialog) {
        Dialog(onDismissRequest = onDismissAddServerConfigDialog) {
            AddEditServerConfigDialogContent(
                serverConfig = editServerConfig,
                onSaveClick = onSaveServerConfig,
                onCancelClick = onDismissAddServerConfigDialog,
            )
        }
    }
}

@Composable
private fun PermissionsBanner(
    modifier: Modifier = Modifier,
    hasAccessibilityPermission: Boolean,
    hasOverlayDrawPermission: Boolean,
    onFixClick: () -> Unit,
) {
    FixPermissionsBanner(
        modifier = modifier,
        text = {
            Text(
                text = stringResource(
                    when {
                        !hasAccessibilityPermission && !hasOverlayDrawPermission -> R.string.requires_accessibility_overlay_perms
                        !hasAccessibilityPermission -> R.string.requires_accessibility_perm
                        else -> R.string.requires_overlay_perm
                    }
                )
            )
        },
        onFixClick = onFixClick,
    )
    Divider()
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewHomeScreenContent(
    @PreviewParameter(ServerConfigListPreviewParameterProvider::class) serverConfigs: List<ServerConfig>,
) {
    BarrierClientTheme {
        Surface {
            HomeScreenContent(
                serverConfigs = serverConfigs,
                selectedConfigId = serverConfigs.randomOrNull()?.id,
            )
        }
    }
}