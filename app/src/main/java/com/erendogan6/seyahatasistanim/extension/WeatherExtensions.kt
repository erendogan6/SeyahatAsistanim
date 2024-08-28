package com.erendogan6.seyahatasistanim.extension

import com.erendogan6.seyahatasistanim.data.model.dto.weather.Temperature
import com.erendogan6.seyahatasistanim.data.model.dto.weather.Weather
import com.erendogan6.seyahatasistanim.data.model.dto.weather.WeatherApiResponse
import com.erendogan6.seyahatasistanim.data.model.dto.weather.WeatherForecast
import com.erendogan6.seyahatasistanim.data.model.entity.WeatherEntity
import java.time.Instant
import java.time.ZoneId

fun WeatherApiResponse.toEntityList(): List<WeatherEntity> =
    this.forecastList.map { forecast ->
        WeatherEntity(
            id = generateIdForEntity(forecast.dateTime),
            date = Instant.ofEpochSecond(forecast.dateTime).atZone(ZoneId.systemDefault()).toLocalDate(),
            temperatureDay = forecast.temp.day,
            temperatureNight = forecast.temp.night,
            description = forecast.weather.firstOrNull()?.description ?: "No description",
        )
    }

private fun generateIdForEntity(dateTime: Long): String = "weather_$dateTime"

fun WeatherEntity.toWeatherForecast(): WeatherForecast =
    WeatherForecast(
        dateTime = this.date.atStartOfDay(ZoneId.systemDefault()).toEpochSecond(),
        temp = Temperature(day = this.temperatureDay, night = this.temperatureNight),
        weather = listOf(Weather(description = this.description)),
    )
