package com.erendogan6.seyahatasistanim.domain.repository

import com.erendogan6.seyahatasistanim.data.model.entity.LocalInfoEntity

interface LocalInfoRepository {
    suspend fun saveLocalInfo(localInfo: LocalInfoEntity)

    suspend fun getLocalInfo(): LocalInfoEntity?
}
