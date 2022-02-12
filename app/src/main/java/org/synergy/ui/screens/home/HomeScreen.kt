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
import androidx.annotation.RequiresApi
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
import org.synergy.services.BarrierAccessibilityService
import org.synergy.services.BarrierClientService
import org.synergy.ui.common.FixPermissionsBanner
import org.synergy.ui.common.OnLifecycleEvent
import org.synergy.ui.common.ServerConfigForm
import org.synergy.ui.theme.BarrierClientTheme
import org.synergy.utils.AccessibilityUtils
import org.synergy.utils.DisplayUtils

@Composable
fun HomeScreen(
    viewModel: HomeScreenViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var barrierClientService: BarrierClientService? by remember { mutableStateOf(null) }

    val overlayPermActivityLauncher = rememberLauncherForActivityResult(StartActivityForResult()) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(context)) {
            viewModel.setHasOverlayDrawPermission(true)
            return@rememberLauncherForActivityResult
        }
        Toast.makeText(
            context,
            context.getString(R.string.overlay_permission_denied),
            Toast.LENGTH_SHORT
        ).show()
    }

    val accessibilityPermLauncher = rememberLauncherForActivityResult(StartActivityForResult()) {
        val enabled = AccessibilityUtils.isAccessibilityServiceEnabled(
            context,
            BarrierAccessibilityService::class.java
        )
        if (enabled) {
            viewModel.setHasAccessibilityPermission(true)
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
                if (!uiState.hasRequestedOverlayDrawPermission && !uiState.hasOverlayDrawPermission) {
                    requestOverlayDrawingPermission(
                        context,
                        overlayPermActivityLauncher,
                    )
                    viewModel.setRequestedOverlayDrawPermission(true)
                }
                if (!uiState.hasRequestedAccessibilityPermission && !uiState.hasAccessibilityPermission) {
                    requestAccessibilityPermission(accessibilityPermLauncher)
                    viewModel.setRequestedAccessibilityPermission(true)
                }
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
        onFixPermissionsClick = {},
        onPermissionsLearnMoreClick = {},
    )
}

@Composable
private fun HomeScreenContent(
    modifier: Modifier = Modifier,
    serverConfig: ServerConfig = ServerConfig(),
    barrierClientConnected: Boolean = false,
    hasOverlayDrawPermission: Boolean = false,
    hasAccessibilityPermission: Boolean = false,
    onServerConfigChange: (ServerConfig) -> Unit = {},
    onConnectClick: () -> Unit = {},
    onFixPermissionsClick: () -> Unit = {},
    onPermissionsLearnMoreClick: () -> Unit = {},
) {
    val hasPermissions = hasOverlayDrawPermission && hasAccessibilityPermission

    Column {
        if (!hasPermissions) {
            PermissionsBanner(
                hasAccessibilityPermission = hasAccessibilityPermission,
                hasOverlayDrawPermission = hasOverlayDrawPermission,
                onFixClick = onFixPermissionsClick,
                onLearnMoreClick = onPermissionsLearnMoreClick,
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
}

@Composable
private fun PermissionsBanner(
    modifier: Modifier = Modifier,
    hasAccessibilityPermission: Boolean,
    hasOverlayDrawPermission: Boolean,
    onFixClick: () -> Unit,
    onLearnMoreClick: () -> Unit,
) {
    FixPermissionsBanner(
        modifier = modifier,
        text = {
            Text(
                text = stringResource(
                    when {
                        !hasAccessibilityPermission && !hasOverlayDrawPermission -> R.string.requires_accessibility_overlay_perms
                        !hasAccessibilityPermission -> R.string.requires_accessibility_perm
                        else -> R.string.requires_ovelay_perm
                    }
                )
            )
        },
        onFixClick = onFixClick,
        onLearnMoreClick = onLearnMoreClick,
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

@RequiresApi(Build.VERSION_CODES.M)
private fun requestOverlayDrawingPermission(
    context: Context,
    overlayPermActivityLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>,
) {
    // TODO: Need to first show dialog to explain the request, and what the user has to do
    val intent = Intent(
        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
        Uri.parse("package:${context.packageName}")
    )
    overlayPermActivityLauncher.launch(intent)
}

private fun requestAccessibilityPermission(
    accessibilityPermLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>
) = accessibilityPermLauncher.launch(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
