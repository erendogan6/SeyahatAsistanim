package com.erendogan6.seyahatasistanim.di

import com.erendogan6.seyahatasistanim.presentation.viewmodel.ChatGptViewModel
import com.erendogan6.seyahatasistanim.presentation.viewmodel.TravelViewModel
import com.erendogan6.seyahatasistanim.presentation.viewmodel.WeatherViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule =
    module {
        viewModel { ChatGptViewModel(chatGptRepository = get(), localInfoRepository = get(), checklistRepository = get(), context = get()) }
        viewModel { WeatherViewModel(weatherRepository = get(), context = get()) }
        viewModel {
            TravelViewModel(
                travelRepository = get(),
                weatherRepository = get(),
                context = get(),
                database = get(),
            )
        }
    }
