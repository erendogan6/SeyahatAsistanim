package com.erendogan6.seyahatasistanim.di

import com.erendogan6.seyahatasistanim.BuildConfig
import com.erendogan6.seyahatasistanim.data.remote.ChatGptApiService
import com.erendogan6.seyahatasistanim.data.remote.CityApiService
import com.erendogan6.seyahatasistanim.data.remote.WeatherApiService
import com.erendogan6.seyahatasistanim.data.repository.ChatGptRepository
import com.erendogan6.seyahatasistanim.data.repository.LocalInfoRepository
import com.erendogan6.seyahatasistanim.data.repository.TravelRepository
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
            OkHttpClient
                .Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(
                    HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BODY
                    },
                ).build()
        }

        fun provideOkHttpClientWithApiKey(okHttpClient: OkHttpClient): OkHttpClient =
            okHttpClient
                .newBuilder()
                .addInterceptor { chain ->
                    val original = chain.request()
                    val originalHttpUrl = original.url
                    val url =
                        originalHttpUrl
                            .newBuilder()
                            .addQueryParameter("appid", BuildConfig.OPENWEATHER_API_KEY)
                            .build()
                    val requestBuilder = original.newBuilder().url(url)
                    val request = requestBuilder.build()
                    chain.proceed(request)
                }.build()

        single(named("weatherRetrofit")) {
            Retrofit
                .Builder()
                .baseUrl("https://pro.openweathermap.org/")
                .client(provideOkHttpClientWithApiKey(get()))
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }

        single(named("chatGptRetrofit")) {
            Retrofit
                .Builder()
                .baseUrl("https://api.openai.com/")
                .client(
                    get<OkHttpClient>()
                        .newBuilder()
                        .addInterceptor { chain ->
                            val request =
                                chain
                                    .request()
                                    .newBuilder()
                                    .header("Authorization", "Bearer ${BuildConfig.CHATGPT_API_KEY}")
                                    .build()
                            chain.proceed(request)
                        }.build(),
                ).addConverterFactory(GsonConverterFactory.create())
                .build()
        }

        single(named("cityRetrofit")) {
            Retrofit
                .Builder()
                .baseUrl("https://api.openweathermap.org/")
                .client(provideOkHttpClientWithApiKey(get()))
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }

        single { get<Retrofit>(named("weatherRetrofit")).create(WeatherApiService::class.java) }
        single { get<Retrofit>(named("chatGptRetrofit")).create(ChatGptApiService::class.java) }
        single { get<Retrofit>(named("cityRetrofit")).create(CityApiService::class.java) }

        single {
            WeatherRepository(
                weatherApiService = get(),
                cityApiService = get(),
                weatherDao = get(),
            )
        }

        single {
            ChatGptRepository(get())
        }

        single {
            TravelRepository(
                travelDao = get(),
                cityApiService = get(),
            )
        }

        single {
            LocalInfoRepository(
                localInfoDao = get(),
            )
        }
    }
