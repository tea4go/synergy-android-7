package org.synergy.ui.common

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import org.synergy.R
import org.synergy.data.db.entities.ServerConfig
import org.synergy.ui.theme.BarrierClientTheme
import org.synergy.utils.ServerConfigListPreviewParameterProvider

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ServerConfigDropdown(
    modifier: Modifier = Modifier,
    serverConfigs: List<ServerConfig> = emptyList(),
    selectedConfigId: Long? = null,
    onChange: (ServerConfig) -> Unit = {},
    onAddServerConfigClick: () -> Unit = {},
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedConfig by remember(serverConfigs, selectedConfigId) {
        derivedStateOf { serverConfigs.find { it.id == selectedConfigId } }
    }

    ExposedDropdownMenuBox(
        modifier = modifier,
        expanded = expanded,
        onExpandedChange = { expanded = it },
    ) {
        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true },
            enabled = false,
            label = { Text(text = stringResource(id = R.string.server)) },
            value = selectedConfig?.name ?: "",
            onValueChange = {},
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.textFieldColors(
                disabledTextColor = ExposedDropdownMenuDefaults.textFieldColors()
                    .textColor(enabled = true).value
            ),
        )
        DropdownMenu(
            // Using DropdownMenu instead of ExposedDropdownMenu. see https://issuetracker.google.com/205589613
            modifier = Modifier.exposedDropdownSize(),
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
            DropdownMenuItem(onClick = {
                onAddServerConfigClick()
                expanded = false
            }) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    tint = MaterialTheme.colors.primary,
                    contentDescription = "add",
                )
                Spacer(modifier = Modifier.requiredWidth(12.dp))
                Text(
                    text = stringResource(id = R.string.add_server),
                    color = MaterialTheme.colors.primary,
                )
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