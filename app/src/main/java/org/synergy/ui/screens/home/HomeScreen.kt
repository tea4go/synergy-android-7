package org.synergy.ui.screens.home

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import org.synergy.R
import org.synergy.barrier.base.utils.Timber
import org.synergy.barrier.base.utils.e
import org.synergy.data.ServerConfig
import org.synergy.services.BarrierClientService
import org.synergy.ui.common.FixPermissionsBanner
import org.synergy.ui.common.OnLifecycleEvent
import org.synergy.ui.common.ServerConfigForm
import org.synergy.ui.theme.BarrierClientTheme
import org.synergy.utils.DisplayUtils

@Composable
fun HomeScreen(
    viewModel: HomeScreenViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var barrierClientService: BarrierClientService? by remember { mutableStateOf(null) }

    fun showPermissionDialog(force: Boolean = false) {
        if ((force || !uiState.hasRequestedOverlayDrawPermission) && !uiState.hasOverlayDrawPermission) {
            viewModel.setShowOverlayDrawPermissionDialog(true)
            return
        }
        if ((force || !uiState.hasRequestedAccessibilityPermission) && !uiState.hasAccessibilityPermission) {
            viewModel.setShowAccessibilityPermissionDialog(true)
        }
    }

    val overlayPermActivityLauncher = rememberLauncherForActivityResult(StartActivityForResult()) {
        // refresh permission status in viewmodel
        val (hasOverlayDrawPermission, _) = viewModel.checkPermissions()
        if (hasOverlayDrawPermission) {
            return@rememberLauncherForActivityResult
        }
        Toast.makeText(
            context,
            context.getString(R.string.overlay_permission_denied),
            Toast.LENGTH_SHORT
        ).show()
    }

    val accessibilityPermLauncher = rememberLauncherForActivityResult(StartActivityForResult()) {
        // refresh permission status in viewmodel
        val (_, hasAccessibilityPermission) = viewModel.checkPermissions()
        if (hasAccessibilityPermission) {
            return@rememberLauncherForActivityResult
        }
        Toast.makeText(
            context,
            context.getString(R.string.accessibility_permission_denied),
            Toast.LENGTH_SHORT
        ).show()
    }

    val serviceConnection = remember {
        object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                if (service !is BarrierClientService.LocalBinder) {
                    return
                }
                service.service
                    .also { barrierClientService = it }
                    .addOnConnectionChangeListener { viewModel.setBarrierClientConnected(it) }
                viewModel.setBarrierClientServiceBound(true)
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                barrierClientService = null
                viewModel.setBarrierClientServiceBound(false)
            }
        }
    }

    OnLifecycleEvent { _, event ->
        when (event) {
            Lifecycle.Event.ON_RESUME -> {
                bindToClientService(
                    context = context,
                    serviceConnection = serviceConnection,
                    autoCreate = false,
                )
                // show permission dialog if required
                showPermissionDialog()
            }
            Lifecycle.Event.ON_PAUSE -> {
                context.unbindService(serviceConnection)
            }
            else -> Unit
        }
    }

    HomeScreenContent(
        modifier = Modifier.fillMaxHeight(),
        serverConfig = uiState.serverConfig,
        barrierClientConnected = uiState.barrierClientConnected,
        hasOverlayDrawPermission = uiState.hasOverlayDrawPermission,
        hasAccessibilityPermission = uiState.hasAccessibilityPermission,
        showOverlayDrawPermissionDialog = uiState.showOverlayDrawPermissionDialog,
        showAccessibilityPermissionDialog = uiState.showAccessibilityPermissionDialog,
        onServerConfigChange = { viewModel.updateServerConfig(it) },
        onConnectClick = {
            if (uiState.barrierClientConnected) {
                barrierClientService?.disconnect()
                return@HomeScreenContent
            }
            viewModel.saveServerConfig()
            uiState.serverConfig.run {
                connect(
                    context = context,
                    barrierClientServiceBound = uiState.barrierClientServiceBound,
                    serviceConnection = serviceConnection,
                    clientName = clientName,
                    serverHost = serverHost,
                    serverPort = serverPortInt,
                    // deviceName = inputDeviceName,
                )
            }
        },
        onFixPermissionsClick = { showPermissionDialog(true) },
        // onPermissionsLearnMoreClick = {},
        onAcceptPermissionClick = {
            if (uiState.showOverlayDrawPermissionDialog) {
                requestOverlayDrawingPermission(
                    context,
                    overlayPermActivityLauncher,
                )
                viewModel.setRequestedOverlayDrawPermission(true)
                viewModel.setShowOverlayDrawPermissionDialog(false)
                return@HomeScreenContent
            }
            requestAccessibilityPermission(accessibilityPermLauncher)
            viewModel.setRequestedAccessibilityPermission(true)
            viewModel.setShowAccessibilityPermissionDialog(false)
        },
        onDismissPermissionDialog = {
            if (uiState.showOverlayDrawPermissionDialog) {
                viewModel.setShowOverlayDrawPermissionDialog(false)
                return@HomeScreenContent
            }
            viewModel.setShowAccessibilityPermissionDialog(false)
        }
    )
}

@Composable
private fun HomeScreenContent(
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

private fun connect(
    context: Context,
    barrierClientServiceBound: Boolean,
    serviceConnection: ServiceConnection,
    clientName: String,
    serverHost: String,
    serverPort: Int,
    // deviceName: String,
) {
    val displayBounds = DisplayUtils.getDisplayBounds(context)
    if (displayBounds == null) {
        Timber.e("displayBounds is null")
        return
    }

    val intent = Intent(
        context,
        BarrierClientService::class.java,
    ).apply {
        putExtra(BarrierClientService.EXTRA_IP_ADDRESS, serverHost)
        putExtra(BarrierClientService.EXTRA_PORT, serverPort)
        putExtra(BarrierClientService.EXTRA_CLIENT_NAME, clientName)
        putExtra(BarrierClientService.EXTRA_SCREEN_WIDTH, displayBounds.width())
        putExtra(BarrierClientService.EXTRA_SCREEN_HEIGHT, displayBounds.height())
    }

    ContextCompat.startForegroundService(context.applicationContext, intent)
    if (!barrierClientServiceBound) {
        bindToClientService(
            context = context,
            serviceConnection = serviceConnection,
        )
    }
}

private fun bindToClientService(
    context: Context,
    serviceConnection: ServiceConnection,
    autoCreate: Boolean = true,
) = context.bindService(
    Intent(context, BarrierClientService::class.java),
    serviceConnection,
    if (autoCreate) ComponentActivity.BIND_AUTO_CREATE else 0
)

private fun requestOverlayDrawingPermission(
    context: Context,
    overlayPermActivityLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>,
) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return
    val intent = Intent(
        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
        Uri.parse("package:${context.packageName}")
    )
    overlayPermActivityLauncher.launch(intent)
}

private fun requestAccessibilityPermission(
    accessibilityPermLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>
) = accessibilityPermLauncher.launch(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
