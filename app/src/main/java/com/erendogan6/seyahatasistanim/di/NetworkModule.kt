package com.erendogan6.seyahatasistanim.di

import com.erendogan6.seyahatasistanim.data.remote.ChatGptService
import com.erendogan6.seyahatasistanim.data.remote.CityApiService
import com.erendogan6.seyahatasistanim.data.remote.WeatherApiService
import com.erendogan6.seyahatasistanim.data.repository.WeatherRepository
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val networkModule =
    module {
        single {
            OkHttpClient
                .Builder()
                .addInterceptor(
                    HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BODY
                    },
                ).build()
        }

        single {
            Retrofit
                .Builder()
                .baseUrl("https://pro.openweathermap.org/")
                .client(get())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }

        single { get<Retrofit>().create(CityApiService::class.java) }

        single { get<Retrofit>().create(WeatherApiService::class.java) }

        single { get<Retrofit>().create(ChatGptService::class.java) }

        single {
            WeatherRepository(get())
        }
    }
