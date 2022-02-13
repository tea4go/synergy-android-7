package org.synergy.ui.common

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import org.synergy.R
import org.synergy.data.db.entities.ServerConfig
import org.synergy.ui.theme.BarrierClientTheme
import org.synergy.utils.ServerConfigNullablePreviewParameterProvider

@Composable
fun AddEditServerConfigDialogContent(
    modifier: Modifier = Modifier,
    serverConfig: ServerConfig? = null,
    onSaveClick: (ServerConfig) -> Unit = {},
    onCancelClick: () -> Unit = {},
) {
    var localServerConfig by remember { mutableStateOf(serverConfig?.copy() ?: ServerConfig()) }
    val saveEnabled by remember(localServerConfig) {
        derivedStateOf {
            localServerConfig.run {
                this != serverConfig
                    && name.isNotBlank()
                    && clientName.isNotBlank()
                    && serverHost.isNotBlank()
                    && serverPortInt > 0
                    && inputDeviceName.isNotBlank()
            }
        }
    }

    LaunchedEffect(serverConfig) {
        localServerConfig = serverConfig?.copy() ?: ServerConfig()
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
    ) {
        Column(
            modifier = modifier
                .padding(start = 16.dp, end = 16.dp, top = 16.dp)
                .width(IntrinsicSize.Min),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Add New Server",
                style = MaterialTheme.typography.h6,
            )
            ServerConfigForm(
                serverConfig = localServerConfig,
                onChange = { localServerConfig = it }
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = onCancelClick) {
                    Text(
                        text = stringResource(id = R.string.cancel).uppercase(),
                        style = MaterialTheme.typography.button,
                    )
                }
                TextButton(
                    enabled = saveEnabled,
                    onClick = { onSaveClick(localServerConfig) },
                ) {
                    Text(
                        text = stringResource(
                            id = if (serverConfig == null) R.string.add else R.string.update
                        ).uppercase(),
                        style = MaterialTheme.typography.button,
                    )
                }
            }
        }
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewAddEditServerConfigDialogContent(
    @PreviewParameter(
        ServerConfigNullablePreviewParameterProvider::class,
        limit = 2
    ) serverConfig: ServerConfig?,
) {
    BarrierClientTheme {
        AddEditServerConfigDialogContent(
            serverConfig = serverConfig,
        )
    }
}