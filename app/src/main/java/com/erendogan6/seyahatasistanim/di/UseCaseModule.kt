package com.erendogan6.seyahatasistanim.di

import com.erendogan6.seyahatasistanim.domain.usecase.AddChecklistItemUseCase
import com.erendogan6.seyahatasistanim.domain.usecase.DeleteChecklistItemUseCase
import com.erendogan6.seyahatasistanim.domain.usecase.GetAllChatMessagesUseCase
import com.erendogan6.seyahatasistanim.domain.usecase.GetCitySuggestionsUseCase
import com.erendogan6.seyahatasistanim.domain.usecase.GetLastTravelInfoUseCase
import com.erendogan6.seyahatasistanim.domain.usecase.GetLocalInfoUseCase
import com.erendogan6.seyahatasistanim.domain.usecase.GetSuggestionsUseCase
import com.erendogan6.seyahatasistanim.domain.usecase.GetWeatherDataUseCase
import com.erendogan6.seyahatasistanim.domain.usecase.GetWeatherForecastUseCase
import com.erendogan6.seyahatasistanim.domain.usecase.LoadChecklistItemsUseCase
import com.erendogan6.seyahatasistanim.domain.usecase.SaveChatMessageUseCase
import com.erendogan6.seyahatasistanim.domain.usecase.SaveChecklistItemsUseCase
import com.erendogan6.seyahatasistanim.domain.usecase.SaveLocalInfoUseCase
import com.erendogan6.seyahatasistanim.domain.usecase.SaveTravelInfoUseCase
import com.erendogan6.seyahatasistanim.domain.usecase.SaveWeatherDataUseCase
import com.erendogan6.seyahatasistanim.domain.usecase.ToggleItemCompletionUseCase
import org.koin.dsl.module

val useCaseModule =
    module {
        single { GetLocalInfoUseCase(localInfoRepository = get()) }
        single { SaveLocalInfoUseCase(localInfoRepository = get()) }
        single { LoadChecklistItemsUseCase(checklistRepository = get()) }
        single { AddChecklistItemUseCase(checklistRepository = get()) }
        single { DeleteChecklistItemUseCase(checklistRepository = get()) }
        single { ToggleItemCompletionUseCase(checklistRepository = get()) }
        single { SaveChecklistItemsUseCase(checklistRepository = get()) }
        single { SaveTravelInfoUseCase(travelRepository = get()) }
        single { GetLastTravelInfoUseCase(travelRepository = get()) }
        single { GetSuggestionsUseCase(chatGptRepository = get()) }
        single { SaveChatMessageUseCase(chatGptRepository = get()) }
        single { GetAllChatMessagesUseCase(chatGptRepository = get()) }
        single { GetWeatherForecastUseCase(weatherRepository = get()) }
        single { SaveWeatherDataUseCase(weatherRepository = get()) }
        single { GetWeatherDataUseCase(weatherRepository = get()) }
        single { GetCitySuggestionsUseCase(weatherRepository = get()) }
    }
