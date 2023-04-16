package org.synergy.data.db.entities

import androidx.room.*

@Entity(
    tableName = "server_configs",
    indices = [Index(value = ["name"], unique = true)]
)
data class ServerConfig(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String = "",
    @ColumnInfo(name = "client_name") val clientName: String = "",
    @ColumnInfo(name = "host") val serverHost: String = "",
    @ColumnInfo(name = "port") val serverPort: String = "24800", // to allow blank input
    @ColumnInfo(name = "device_name") val inputDeviceName: String = "touchscreen",
) {
    @delegate:Ignore
    val serverPortInt: Int by lazy { serverPort.toIntOrNull() ?: 0 }
}
