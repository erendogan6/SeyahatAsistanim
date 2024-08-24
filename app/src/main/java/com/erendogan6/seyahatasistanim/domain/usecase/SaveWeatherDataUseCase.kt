package com.erendogan6.seyahatasistanim.domain.usecase

import com.erendogan6.seyahatasistanim.data.model.entity.WeatherEntity
import com.erendogan6.seyahatasistanim.domain.repository.WeatherRepository

class SaveWeatherDataUseCase(
    private val weatherRepository: WeatherRepository,
) {
    suspend operator fun invoke(weatherEntities: List<WeatherEntity>) {
        weatherRepository.saveWeatherData(weatherEntities)
    }
}
