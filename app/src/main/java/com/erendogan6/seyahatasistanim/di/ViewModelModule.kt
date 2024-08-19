package com.erendogan6.seyahatasistanim.di

import com.erendogan6.seyahatasistanim.ui.viewmodel.ChatGptViewModel
import com.erendogan6.seyahatasistanim.ui.viewmodel.WeatherViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule =
    module {
        viewModel { ChatGptViewModel(get()) }
        viewModel { WeatherViewModel(get()) }
    }
