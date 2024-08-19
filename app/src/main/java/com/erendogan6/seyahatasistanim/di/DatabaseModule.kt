package com.erendogan6.seyahatasistanim.di

import androidx.room.Room
import com.erendogan6.seyahatasistanim.data.local.TravelDatabase
import com.erendogan6.seyahatasistanim.data.repository.TravelRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule =
    module {
        single {
            Room
                .databaseBuilder(
                    androidContext(),
                    TravelDatabase::class.java,
                    "travel_database",
                ).build()
        }

        single { get<TravelDatabase>().travelDao() }

        single { TravelRepository(get()) }
    }
