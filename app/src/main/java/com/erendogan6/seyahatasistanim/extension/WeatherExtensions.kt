package com.erendogan6.seyahatasistanim.extension

import com.erendogan6.seyahatasistanim.data.model.weather.Temperature
import com.erendogan6.seyahatasistanim.data.model.weather.Weather
import com.erendogan6.seyahatasistanim.data.model.weather.WeatherApiResponse
import com.erendogan6.seyahatasistanim.data.model.weather.WeatherEntity
import com.erendogan6.seyahatasistanim.data.model.weather.WeatherForecast
import java.time.Instant
import java.time.ZoneId

fun WeatherApiResponse.toEntityList(): List<WeatherEntity> =
    this.forecastList.map { forecast ->
        WeatherEntity(
            id = generateIdForEntity(forecast.dateTime, city.latitude, city.longitude),
            date = Instant.ofEpochSecond(forecast.dateTime).atZone(ZoneId.systemDefault()).toLocalDate(),
            temperatureDay = forecast.temp.day,
            temperatureNight = forecast.temp.night,
            description = forecast.weather.firstOrNull()?.description ?: "No description",
            latitude = this.city.latitude,
            longitude = this.city.longitude,
        )
    }

private fun generateIdForEntity(
    dateTime: Long,
    latitude: Double,
    longitude: Double,
): String = "weather_${dateTime}_${latitude}_$longitude"

fun WeatherEntity.toWeatherForecast(): WeatherForecast =
    WeatherForecast(
        dateTime = this.date.atStartOfDay(ZoneId.systemDefault()).toEpochSecond(),
        temp = Temperature(day = this.temperatureDay, night = this.temperatureNight),
        weather = listOf(Weather(description = this.description)),
    )
