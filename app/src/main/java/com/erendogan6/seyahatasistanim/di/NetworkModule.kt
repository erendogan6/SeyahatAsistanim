package com.erendogan6.seyahatasistanim.di

import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.erendogan6.seyahatasistanim.BuildConfig
import com.erendogan6.seyahatasistanim.data.remote.ChatGptApiService
import com.erendogan6.seyahatasistanim.data.remote.CityApiService
import com.erendogan6.seyahatasistanim.data.remote.WeatherApiService
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
            val okHttpClientBuilder =
                OkHttpClient
                    .Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .addInterceptor(
                        HttpLoggingInterceptor().apply {
                            level = HttpLoggingInterceptor.Level.BODY
                        },
                    )

            if (BuildConfig.DEBUG) {
                okHttpClientBuilder.addInterceptor(
                    ChuckerInterceptor
                        .Builder(get())
                        .collector(ChuckerCollector(get()))
                        .maxContentLength(250_000L)
                        .redactHeaders("Authorization", "Cookie")
                        .alwaysReadResponseBody(true)
                        .build(),
                )
            }

            okHttpClientBuilder.build()
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
    }
