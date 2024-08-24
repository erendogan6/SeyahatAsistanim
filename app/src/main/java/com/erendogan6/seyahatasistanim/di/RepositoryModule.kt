package com.erendogan6.seyahatasistanim.di

import com.erendogan6.seyahatasistanim.data.repository.ChatGptRepositoryImpl
import com.erendogan6.seyahatasistanim.data.repository.ChecklistRepositoryImpl
import com.erendogan6.seyahatasistanim.data.repository.LocalInfoRepositoryImpl
import com.erendogan6.seyahatasistanim.data.repository.TravelRepositoryImpl
import com.erendogan6.seyahatasistanim.data.repository.WeatherRepositoryImpl
import com.erendogan6.seyahatasistanim.domain.repository.ChatGptRepository
import com.erendogan6.seyahatasistanim.domain.repository.ChecklistRepository
import com.erendogan6.seyahatasistanim.domain.repository.LocalInfoRepository
import com.erendogan6.seyahatasistanim.domain.repository.TravelRepository
import com.erendogan6.seyahatasistanim.domain.repository.WeatherRepository
import org.koin.dsl.module

val repositoryModule =
    module {
        single<WeatherRepository> {
            WeatherRepositoryImpl(
                weatherApiService = get(),
                cityApiService = get(),
                weatherDao = get(),
            )
        }

        single<ChatGptRepository> {
            ChatGptRepositoryImpl(
                chatGptApiService = get(),
                chatMessageDao = get(),
            )
        }

        single<TravelRepository> {
            TravelRepositoryImpl(
                travelDao = get(),
            )
        }

        single<LocalInfoRepository> {
            LocalInfoRepositoryImpl(
                localInfoDao = get(),
            )
        }

        single<ChecklistRepository> {
            ChecklistRepositoryImpl(
                checklistDao = get(),
            )
        }
    }
