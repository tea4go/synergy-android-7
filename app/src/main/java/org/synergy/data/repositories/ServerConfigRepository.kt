package org.synergy.data.repositories

import org.synergy.data.db.daos.ServerConfigDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServerConfigRepository @Inject constructor(
    private val serverConfigDao: ServerConfigDao,
) {
    fun getAll() = serverConfigDao.getAll()
}
