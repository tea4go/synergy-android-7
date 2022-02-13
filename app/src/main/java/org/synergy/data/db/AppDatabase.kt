package org.synergy.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import org.synergy.data.db.daos.ServerConfigDao
import org.synergy.data.db.entities.ServerConfig

@Database(
    version = 1,
    entities = [
        ServerConfig::class,
    ],
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun serverConfigDao(): ServerConfigDao
}
