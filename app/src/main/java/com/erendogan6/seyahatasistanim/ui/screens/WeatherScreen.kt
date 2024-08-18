package com.erendogan6.seyahatasistanim.ui.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.erendogan6.seyahatasistanim.ui.viewmodel.WeatherViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun weatherScreen(
    lat: Double,
    lon: Double,
    apiKey: String,
    modifier: Modifier = Modifier,
) {
    val weatherViewModel: WeatherViewModel = viewModel()

    // StateFlow'u toplayarak UI'da göster
    val weatherData by weatherViewModel.weatherData.collectAsState()

    // ViewModel'den veri çekmek için çağrıyı başlat
    weatherViewModel.getWeatherForecast(lat, lon, apiKey)

    // Verileri ekranda göster
    weatherData?.let { data ->
        LazyColumn(modifier = modifier.fillMaxWidth()) {
            items(data.forecastList) { forecast ->
                val date = Date(forecast.dateTime * 1000) // Unix timestamp'i milisaniyeye çevir
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val formattedDate = sdf.format(date)

                Text(
                    text = "Date: $formattedDate, Temp: ${forecast.temp.day}°C",
                    modifier = Modifier.padding(8.dp),
                )
            }
        }
    } ?: run {
        // Eğer veri yoksa, yükleniyor veya hata mesajı göster
        Text(text = "Loading weather data...", modifier = Modifier.padding(16.dp))
    }
}
