package org.synergy.ui.screens.home

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.synergy.R
import org.synergy.data.ServerConfig
import org.synergy.ui.common.ServerConfigForm
import org.synergy.ui.theme.BarrierClientTheme

@Composable
fun HomeScreen(
    viewModel: HomeScreenViewModel = viewModel(),
    barrierClientConnected: Boolean = false,
    onConnectClick: (
        clientName: String,
        serverHost: String,
        serverPort: Int,
        deviceName: String
    ) -> Unit,
    disconnect: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    HomeScreenContent(
        modifier = Modifier.fillMaxHeight(),
        serverConfig = uiState.serverConfig,
        barrierClientConnected = barrierClientConnected,
        onServerConfigChange = { viewModel.updateServerConfig(it) },
        onConnectClick = {
            if (barrierClientConnected) {
                disconnect()
                return@HomeScreenContent
            }
            viewModel.saveServerConfig()
            uiState.serverConfig.run {
                onConnectClick(
                    clientName,
                    serverHost,
                    serverPortInt,
                    inputDeviceName,
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