package com.erendogan6.seyahatasistanim.di

import androidx.room.Room
import com.erendogan6.seyahatasistanim.data.local.TravelDatabase
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

        single { get<TravelDatabase>().weatherDao() }
        single { get<TravelDatabase>().travelDao() }
        single { get<TravelDatabase>().localInfoDao() }
    }
