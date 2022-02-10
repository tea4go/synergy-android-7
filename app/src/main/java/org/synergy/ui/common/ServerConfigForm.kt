package org.synergy.ui.common

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import org.synergy.R
import org.synergy.data.ServerConfig
import org.synergy.ui.theme.BarrierClientTheme

@Composable
fun ServerConfigForm(
    modifier: Modifier = Modifier,
    serverConfig: ServerConfig = ServerConfig(),
    onChange: (ServerConfig) -> Unit = {},
) {
    var localServerConfig by remember { mutableStateOf(serverConfig) }

    LaunchedEffect(serverConfig) {
        // overwrite local config if incoming config changes
        localServerConfig = serverConfig
    }

    LaunchedEffect(localServerConfig) {
        // trigger onChange if local config changes
        if (localServerConfig == serverConfig) {
            return@LaunchedEffect
        }
        onChange(localServerConfig)
    }

    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = stringResource(id = R.string.barrier_client_name)) },
            value = localServerConfig.clientName,
            onValueChange = {
                localServerConfig = localServerConfig.copy(clientName = it.trim())
            },
        )
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = stringResource(id = R.string.server_ip)) },
            value = localServerConfig.serverHost,
            onValueChange = {
                localServerConfig = localServerConfig.copy(serverHost = it.trim())
            },
        )
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = stringResource(id = R.string.server_port)) },
            value = localServerConfig.serverPort,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
            ),
            onValueChange = {
                // in case of any invalid input, ignore the value
                val portInt = it.trim().toIntOrNull() ?: return@OutlinedTextField
                localServerConfig = localServerConfig.copy(serverPort = portInt.toString())
            },
        )
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = stringResource(id = R.string.input_device_name)) },
            value = localServerConfig.inputDeviceName,
            onValueChange = {
                localServerConfig = localServerConfig.copy(inputDeviceName = it.trim())
            },
        )
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewServerConfigForm() {
    BarrierClientTheme {
        Surface {
            ServerConfigForm()
        }
    }
}