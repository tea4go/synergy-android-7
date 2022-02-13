package org.synergy.ui.common

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import org.synergy.R
import org.synergy.data.db.entities.ServerConfig
import org.synergy.ui.theme.BarrierClientTheme
import org.synergy.utils.ServerConfigPreviewParameterProvider

@Composable
fun ServerConfigDetail(
    modifier: Modifier = Modifier,
    serverConfig: ServerConfig = ServerConfig(),
    onEditClick: () -> Unit = {},
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.12f),
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = serverConfig.name,
                    style = MaterialTheme.typography.subtitle1.copy(fontWeight = FontWeight.Bold),
                )
                Icon(
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = rememberRipple(
                            bounded = false,
                            radius = 24.dp,
                        ),
                        onClick = onEditClick,
                    ),
                    imageVector = Icons.Rounded.Edit,
                    tint = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                    contentDescription = "Edit",
                )
            }
            Divider(modifier = Modifier.fillMaxWidth())
            DetailRow(
                label = stringResource(id = R.string.barrier_client_name),
                value = serverConfig.clientName,
            )
            DetailRow(
                label = stringResource(id = R.string.server_ip),
                value = serverConfig.serverHost,
            )
            DetailRow(
                label = stringResource(id = R.string.server_port),
                value = serverConfig.serverPort,
            )
            DetailRow(
                label = stringResource(id = R.string.input_device_name),
                value = serverConfig.inputDeviceName,
            )
        }
    }
}

@Composable
private fun DetailRow(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
) {
    Row(
        modifier = modifier,
    ) {
        Text(
            modifier = Modifier.weight(0.5f),
            text = label,
            style = MaterialTheme.typography.body2.copy(fontWeight = FontWeight.Bold),
        )
        Text(
            modifier = Modifier.weight(0.5f),
            text = value,
        )
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewServerConfigDetail(
    @PreviewParameter(
        ServerConfigPreviewParameterProvider::class,
        limit = 2
    ) serverConfig: ServerConfig,
) {
    BarrierClientTheme {
        ServerConfigDetail(
            serverConfig = serverConfig,
        )
    }
}