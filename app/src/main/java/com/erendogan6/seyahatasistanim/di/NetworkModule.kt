package com.erendogan6.seyahatasistanim.di

import com.erendogan6.seyahatasistanim.BuildConfig
import com.erendogan6.seyahatasistanim.data.remote.ChatGptApiService
import com.erendogan6.seyahatasistanim.data.remote.CityApiService
import com.erendogan6.seyahatasistanim.data.remote.WeatherApiService
import com.erendogan6.seyahatasistanim.data.repository.ChatGptRepository
import com.erendogan6.seyahatasistanim.data.repository.WeatherRepository
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

val networkModule =
    module {

        single {
            val apiKeyInterceptor = { chain: okhttp3.Interceptor.Chain ->
                val original = chain.request()
                val requestBuilder =
                    original
                        .newBuilder()
                        .header("Authorization", "Bearer ${BuildConfig.OPENWEATHER_API_KEY}")
                val request = requestBuilder.build()
                chain.proceed(request)
            }

            OkHttpClient
                .Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(apiKeyInterceptor)
                .addInterceptor(
                    HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BODY
                    },
                ).build()
        }

        single(named("weatherRetrofit")) {
            Retrofit
                .Builder()
                .baseUrl("https://pro.openweathermap.org/")
                .client(get())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }

        single(named("chatGptRetrofit")) {
            Retrofit
                .Builder()
                .baseUrl("https://api.openai.com/")
                .client(
                    OkHttpClient
                        .Builder()
                        .addInterceptor { chain ->
                            val request =
                                chain
                                    .request()
                                    .newBuilder()
                                    .header("Authorization", "Bearer ${BuildConfig.CHATGPT_API_KEY}")
                                    .build()
                            chain.proceed(request)
                        }.addInterceptor(get<OkHttpClient>().interceptors[0])
                        .build(),
                ).addConverterFactory(GsonConverterFactory.create())
                .build()
        }

        single(named("cityRetrofit")) {
            Retrofit
                .Builder()
                .baseUrl("https://api.cityapi.com/")
                .client(get())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }

        single { get<Retrofit>().create(CityApiService::class.java) }

        single { get<Retrofit>().create(WeatherApiService::class.java) }

        single { get<Retrofit>().create(ChatGptApiService::class.java) }

        single {
            WeatherRepository(get())
        }

        single {
            ChatGptRepository(get())
        }
    }
