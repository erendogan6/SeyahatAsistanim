package com.erendogan6.seyahatasistanim.di

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class SeyahatAsistaniApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@SeyahatAsistaniApp)
            modules(
                listOf(
                    networkModule,
                    viewModelModule,
                    databaseModule,
                    repositoryModule,
                    useCaseModule,
                ),
            )
        }
    }
}
