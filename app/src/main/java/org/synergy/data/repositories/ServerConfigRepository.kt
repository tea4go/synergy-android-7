package org.synergy.data.repositories

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import org.synergy.data.db.daos.ServerConfigDao
import org.synergy.data.db.entities.ServerConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServerConfigRepository @Inject constructor(
    private val serverConfigDao: ServerConfigDao,
) {
    fun getAll() = serverConfigDao.getAll().flowOn(Dispatchers.IO)

    suspend fun save(serverConfig: ServerConfig) = withContext(Dispatchers.IO) {
        val temp = serverConfig.copy(name = serverConfig.name.trim())
        if (temp.id == 0L) {
            serverConfigDao.insert(temp)
        } else {
            serverConfigDao.update(temp)
        }
    }
}
