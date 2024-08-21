package com.erendogan6.seyahatasistanim.data.repository
import com.erendogan6.seyahatasistanim.data.local.LocalInfoDao
import com.erendogan6.seyahatasistanim.data.model.chatGPT.LocalInfoEntity

class LocalInfoRepository(
    private val localInfoDao: LocalInfoDao,
) {
    suspend fun saveLocalInfo(localInfo: LocalInfoEntity) {
        localInfoDao.saveLocalInfo(localInfo)
    }

    suspend fun getLocalInfo(destination: String): LocalInfoEntity? = localInfoDao.getLocalInfo(destination)
}
