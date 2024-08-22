package com.erendogan6.seyahatasistanim.di

import com.erendogan6.seyahatasistanim.data.repository.ChatGptRepository
import com.erendogan6.seyahatasistanim.data.repository.ChecklistRepository
import com.erendogan6.seyahatasistanim.data.repository.LocalInfoRepository
import com.erendogan6.seyahatasistanim.data.repository.TravelRepository
import com.erendogan6.seyahatasistanim.data.repository.WeatherRepository
import org.koin.dsl.module

val repositoryModule =
    module {
        single {
            WeatherRepository(
                weatherApiService = get(),
                cityApiService = get(),
                weatherDao = get(),
            )
        }

        single {
            ChatGptRepository(
                chatGptApiService = get(),
                chatMessageDao = get(),
            )
        }

        single {
            TravelRepository(
                travelDao = get(),
            )
        }

        single {
            LocalInfoRepository(
                localInfoDao = get(),
            )
        }

        single {
            ChecklistRepository(
                checklistDao = get(),
            )
        }
    }
