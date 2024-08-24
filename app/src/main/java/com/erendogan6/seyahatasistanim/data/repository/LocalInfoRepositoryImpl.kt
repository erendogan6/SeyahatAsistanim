package com.erendogan6.seyahatasistanim.data.repository
import com.erendogan6.seyahatasistanim.data.local.dao.LocalInfoDao
import com.erendogan6.seyahatasistanim.data.model.entity.LocalInfoEntity
import com.erendogan6.seyahatasistanim.domain.repository.LocalInfoRepository

class LocalInfoRepositoryImpl(
    private val localInfoDao: LocalInfoDao,
) : LocalInfoRepository {
    override suspend fun saveLocalInfo(localInfo: LocalInfoEntity) {
        localInfoDao.saveLocalInfo(localInfo)
    }

    override suspend fun getLocalInfo(): LocalInfoEntity? = localInfoDao.getLocalInfo()
}
