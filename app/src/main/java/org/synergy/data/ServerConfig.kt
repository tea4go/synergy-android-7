package org.synergy.data

data class ServerConfig(
    val clientName: String = "",
    val serverHost: String = "",
    val serverPort: String = "", // to allow blank input
    val inputDeviceName: String = "",
) {
    val serverPortInt: Int by lazy { serverPort.toIntOrNull() ?: 0 }
}
