package org.synergy.ui.screens.home

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.res.Configuration
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import org.synergy.R
import org.synergy.barrier.base.utils.Timber
import org.synergy.barrier.base.utils.e
import org.synergy.data.ServerConfig
import org.synergy.services.BarrierClientService
import org.synergy.ui.common.ServerConfigForm
import org.synergy.ui.theme.BarrierClientTheme
import org.synergy.utils.DisplayUtils

@Composable
fun HomeScreen(
    viewModel: HomeScreenViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var barrierClientServiceBound by remember { mutableStateOf(false) }
    var barrierClientService: BarrierClientService? by remember { mutableStateOf(null) }
    var barrierClientConnected by remember { mutableStateOf(false) }

    val serviceConnection = remember {
        object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                if (service !is BarrierClientService.LocalBinder) {
                    return
                }
                service.service
                    .also { barrierClientService = it }
                    .apply {
                        addOnConnectionChangeListener {
                            barrierClientConnected = it
                        }
                    }
                barrierClientServiceBound = true
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                barrierClientService = null
                barrierClientServiceBound = false
            }
        }
    }

    DisposableEffect(Unit) {
        bindToClientService(
            context = context,
            serviceConnection = serviceConnection,
            autoCreate = false,
        )
        onDispose {
            context.unbindService(serviceConnection)
        }
    }

    HomeScreenContent(
        modifier = Modifier.fillMaxHeight(),
        serverConfig = uiState.serverConfig,
        barrierClientConnected = barrierClientConnected,
        onServerConfigChange = { viewModel.updateServerConfig(it) },
        onConnectClick = {
            if (barrierClientConnected) {
                barrierClientService?.disconnect()
                return@HomeScreenContent
            }
            viewModel.saveServerConfig()
            uiState.serverConfig.run {
                connect(
                    context = context,
                    barrierClientServiceBound = barrierClientServiceBound,
                    serviceConnection = serviceConnection,
                    clientName = clientName,
                    serverHost = serverHost,
                    serverPort = serverPortInt,
                    // deviceName = inputDeviceName,
                )
            }
        },
    )
}

@Composable
private fun HomeScreenContent(
    modifier: Modifier = Modifier,
    serverConfig: ServerConfig = ServerConfig(),
    barrierClientConnected: Boolean = false,
    onServerConfigChange: (ServerConfig) -> Unit = {},
    onConnectClick: () -> Unit = {},
) {
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
        // Toast.makeText(applicationContext, "displayBounds is null", Toast.LENGTH_LONG).show()
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
