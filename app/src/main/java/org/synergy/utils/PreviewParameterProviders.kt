package org.synergy.utils

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import org.synergy.data.db.entities.ServerConfig
import kotlin.random.Random

private val serverConfigList = List(3) {
    ServerConfig(
        id = it.toLong(),
        name = "Test category ${it + 1}",
        clientName = "Client name ${it + 1}",
        serverHost = "Server Host ${it + 1}",
        serverPort = Random.nextInt(1000, 9999).toString(),
        inputDeviceName = "touchscreen",
    )
}

class ServerConfigNullablePreviewParameterProvider : PreviewParameterProvider<ServerConfig?> {
    override val values = sequenceOf(null, *serverConfigList.toTypedArray())
}

class ServerConfigPreviewParameterProvider : PreviewParameterProvider<ServerConfig> {
    override val values = serverConfigList.asSequence()
}

class ServerConfigListPreviewParameterProvider : PreviewParameterProvider<List<ServerConfig>> {
    override val values = sequenceOf(
        emptyList(),
        serverConfigList,
    )
}
