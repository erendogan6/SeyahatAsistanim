package com.erendogan6.seyahatasistanim.domain.usecase

import com.erendogan6.seyahatasistanim.data.model.dto.weather.WeatherApiResponse
import com.erendogan6.seyahatasistanim.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.Flow

class GetWeatherForecastUseCase(
    private val weatherRepository: WeatherRepository,
) {
    operator fun invoke(
        lat: Double,
        lon: Double,
    ): Flow<WeatherApiResponse> = weatherRepository.getWeatherForecast(lat, lon)
}
