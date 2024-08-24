package com.erendogan6.seyahatasistanim.di

import com.erendogan6.seyahatasistanim.presentation.viewmodel.ChatGptViewModel
import com.erendogan6.seyahatasistanim.presentation.viewmodel.TravelViewModel
import com.erendogan6.seyahatasistanim.presentation.viewmodel.WeatherViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule =
    module {
        viewModel {
            ChatGptViewModel(
                context = get(),
                getLocalInfoUseCase = get(),
                saveLocalInfoUseCase = get(),
                loadChecklistItemsUseCase = get(),
                addChecklistItemUseCase = get(),
                deleteChecklistItemUseCase = get(),
                toggleItemCompletionUseCase = get(),
                saveChecklistItemsUseCase = get(),
                getAllChatMessagesUseCase = get(),
                saveChatMessageUseCase = get(),
                getSuggestionsUseCase = get(),
            )
        }
        viewModel {
            WeatherViewModel(
                context = get(),
                getWeatherDataUseCase = get(),
                getWeatherForecastUseCase = get(),
                saveWeatherDataUseCase = get(),
            )
        }

        viewModel {
            TravelViewModel(
                saveTravelInfoUseCase = get(),
                getLastTravelInfoUseCase = get(),
                getCitySuggestionsUseCase = get(),
                context = get(),
                database = get(),
            )
        }
    }
