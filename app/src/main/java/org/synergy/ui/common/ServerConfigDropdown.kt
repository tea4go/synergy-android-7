package org.synergy.ui.common

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import org.synergy.R
import org.synergy.data.db.entities.ServerConfig
import org.synergy.ui.theme.BarrierClientTheme
import org.synergy.utils.ServerConfigListPreviewParameterProvider

@Composable
fun ServerConfigDropdown(
    modifier: Modifier = Modifier,
    serverConfigs: List<ServerConfig> = emptyList(),
    selectedConfigId: Long? = null,
    onChange: (ServerConfig) -> Unit = {},
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedConfig by remember(serverConfigs, selectedConfigId) {
        derivedStateOf { serverConfigs.find { it.id == selectedConfigId } }
    }

    Box(
        modifier = modifier.wrapContentSize(Alignment.TopStart)
    ) {
        TextField(
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            value = selectedConfig?.name ?: "",
            trailingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_round_arrow_drop_down_24),
                    contentDescription = "arrow down"
                )
            },
            onValueChange = {},
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            serverConfigs.forEach {
                DropdownMenuItem(
                    onClick = {
                        onChange(it)
                        expanded = false
                    },
                ) { Text(text = it.name) }
            }
        }
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewServerConfigDropdown(
    @PreviewParameter(ServerConfigListPreviewParameterProvider::class) serverConfigs: List<ServerConfig>,
) {
    BarrierClientTheme {
        Surface {
            ServerConfigDropdown(
                serverConfigs = serverConfigs,
                selectedConfigId = serverConfigs.randomOrNull()?.id,
            )
        }
    }
}