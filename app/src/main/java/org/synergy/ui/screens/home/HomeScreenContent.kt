package org.synergy.ui.screens.home

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.synergy.R
import org.synergy.data.db.entities.ServerConfig
import org.synergy.ui.common.FixPermissionsBanner
import org.synergy.ui.common.ServerConfigForm
import org.synergy.ui.theme.BarrierClientTheme

@Composable
fun HomeScreenContent(
    modifier: Modifier = Modifier,
    serverConfig: ServerConfig = ServerConfig(),
    barrierClientConnected: Boolean = false,
    hasOverlayDrawPermission: Boolean = false,
    hasAccessibilityPermission: Boolean = false,
    showOverlayDrawPermissionDialog: Boolean = false,
    showAccessibilityPermissionDialog: Boolean = false,
    onServerConfigChange: (ServerConfig) -> Unit = {},
    onConnectClick: () -> Unit = {},
    onFixPermissionsClick: () -> Unit = {},
    // onPermissionsLearnMoreClick: () -> Unit = {},
    onAcceptPermissionClick: () -> Unit = {},
    onDismissPermissionDialog: () -> Unit = {},
) {
    val hasPermissions = hasOverlayDrawPermission && hasAccessibilityPermission

    Column {
        if (!hasPermissions) {
            PermissionsBanner(
                hasAccessibilityPermission = hasAccessibilityPermission,
                hasOverlayDrawPermission = hasOverlayDrawPermission,
                onFixClick = onFixPermissionsClick,
                // onLearnMoreClick = onPermissionsLearnMoreClick,
            )
        }
        Column(
            modifier = modifier.padding(16.dp),
        ) {
            ServerConfigForm(
                modifier = Modifier.weight(1f),
                serverConfig = serverConfig,
                onChange = onServerConfigChange,
            )
            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = if (barrierClientConnected) true else hasPermissions,
                onClick = onConnectClick,
            ) {
                Text(
                    style = MaterialTheme.typography.button,
                    text = stringResource(
                        id = if (barrierClientConnected) R.string.disconnect else R.string.connect
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
}

@Composable
private fun PermissionsBanner(
    modifier: Modifier = Modifier,
    hasAccessibilityPermission: Boolean,
    hasOverlayDrawPermission: Boolean,
    onFixClick: () -> Unit,
    // onLearnMoreClick: () -> Unit,
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
        // onLearnMoreClick = onLearnMoreClick,
    )
    Divider()
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewHomeScreenContent() {
    BarrierClientTheme {
        Surface {
            HomeScreenContent()
        }
    }
}