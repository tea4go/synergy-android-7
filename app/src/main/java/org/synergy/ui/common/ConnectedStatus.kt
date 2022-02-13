package org.synergy.ui.common

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import org.synergy.R
import org.synergy.data.db.entities.ServerConfig
import org.synergy.ui.theme.BarrierClientTheme
import org.synergy.utils.ServerConfigPreviewParameterProvider

@Composable
fun ConnectedStatus(
    modifier: Modifier = Modifier,
    serverConfig: ServerConfig? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(color = Color.Green)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(id = R.string.connected_to_server_name, serverConfig?.name ?: ""),
            style = MaterialTheme.typography.caption,
            color = Color.Black,
            maxLines = 1,
        )
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewConnectedStatus(
    @PreviewParameter(
        ServerConfigPreviewParameterProvider::class,
        limit = 1
    ) serverConfig: ServerConfig,
) {
    BarrierClientTheme {
        Surface {
            ConnectedStatus(
                serverConfig = serverConfig,
            )
        }
    }
}