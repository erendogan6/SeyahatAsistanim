package com.erendogan6.seyahatasistanim.domain.usecase

import com.erendogan6.seyahatasistanim.data.model.dto.weather.City
import com.erendogan6.seyahatasistanim.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.Flow

class GetCitySuggestionsUseCase(
    private val weatherRepository: WeatherRepository,
) {
    operator fun invoke(query: String): Flow<List<City>> = weatherRepository.getCitySuggestions(query)
}
