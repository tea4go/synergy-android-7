package org.synergy.data.db.daos

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import org.synergy.data.db.entities.ServerConfig

@Dao
interface ServerConfigDao {
    @Query("SELECT * FROM server_configs")
    fun getAll(): Flow<List<ServerConfig>>

    @Query("SELECT * FROM server_configs where id = :id")
    fun getById(id: Long): Flow<ServerConfig>

    @Insert
    suspend fun insert(vararg serverConfig: ServerConfig)

    @Update
    suspend fun update(vararg serverConfig: ServerConfig)

    @Delete
    suspend fun delete(vararg serverConfig: ServerConfig)
}
