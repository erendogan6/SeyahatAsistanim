package com.erendogan6.seyahatasistanim.domain.usecase

import com.erendogan6.seyahatasistanim.data.model.entity.WeatherEntity
import com.erendogan6.seyahatasistanim.domain.repository.WeatherRepository
import java.time.LocalDate

class GetWeatherDataUseCase(
    private val weatherRepository: WeatherRepository,
) {
    suspend operator fun invoke(travelDate: LocalDate): List<WeatherEntity> = weatherRepository.getWeatherData(travelDate)
}
