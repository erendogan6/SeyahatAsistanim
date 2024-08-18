package com.erendogan6.seyahatasistanim.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.erendogan6.seyahatasistanim.ui.viewmodel.WeatherViewModel

@Composable
fun weatherDetailScreen(
    lat: Double,
    lon: Double,
    apiKey: String,
    modifier: Modifier = Modifier,
) {
    val viewModel: WeatherViewModel = viewModel()

    val weatherData by viewModel.weatherData.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.getWeatherForecast(lat, lon, apiKey)
    }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Hava Durumu Detayları",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp),
        )

        // Hava durumu verilerini gösteriyoruz
        weatherData?.let { data ->
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(data.forecastList) { forecast ->
                    Card(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = "Gün: ${forecast.dateTime}", style = MaterialTheme.typography.bodyLarge)
                            Text(text = "Gündüz Sıcaklığı: ${forecast.temp.day}°C", style = MaterialTheme.typography.bodyMedium)
                            Text(text = "Gece Sıcaklığı: ${forecast.temp.night}°C", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                text = "Açıklama: ${forecast.weather.firstOrNull()?.description}",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }
            }
        } ?: run {
            Text(text = "Hava durumu verisi yükleniyor...", modifier = Modifier.padding(16.dp))
        }
    }
}
