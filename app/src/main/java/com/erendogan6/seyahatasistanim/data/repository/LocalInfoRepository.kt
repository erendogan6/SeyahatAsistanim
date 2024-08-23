package com.erendogan6.seyahatasistanim.data.repository
import com.erendogan6.seyahatasistanim.data.local.dao.LocalInfoDao
import com.erendogan6.seyahatasistanim.data.model.entity.LocalInfoEntity

class LocalInfoRepository(
    private val localInfoDao: LocalInfoDao,
) {
    suspend fun saveLocalInfo(localInfo: LocalInfoEntity) {
        localInfoDao.saveLocalInfo(localInfo)
    }

    suspend fun getLocalInfo(destination: String): LocalInfoEntity? = localInfoDao.getLocalInfo(destination)
}
