package com.erendogan6.seyahatasistanim.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.erendogan6.seyahatasistanim.R
import com.erendogan6.seyahatasistanim.data.model.weather.WeatherForecast
import com.erendogan6.seyahatasistanim.extension.toWeatherForecast
import com.erendogan6.seyahatasistanim.ui.viewmodel.TravelViewModel
import com.erendogan6.seyahatasistanim.ui.viewmodel.WeatherViewModel
import com.erendogan6.seyahatasistanim.utils.capitalizeWords
import org.koin.androidx.compose.koinViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun weatherDetailScreen(
    modifier: Modifier = Modifier,
    viewModel: WeatherViewModel = koinViewModel(),
    travelViewModel: TravelViewModel = koinViewModel(),
) {
    val travelInfo by travelViewModel.travelInfo.collectAsState()

    val parsedDate =
        travelInfo?.arrivalDate?.let {
            try {
                LocalDate.parse(it, DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("tr")))
            } catch (e: Exception) {
                LocalDate.now()
            }
        } ?: LocalDate.now()

    val lat = travelInfo?.arrivalLatitude ?: 0.0
    val lon = travelInfo?.arrivalLongitude ?: 0.0
    val arrivalLocation = travelInfo?.arrivalPlace ?: "Bilinmeyen Lokasyon"

    val weatherData by viewModel.weatherData.collectAsState()
    val weatherFromDb by viewModel.weatherFromDb.collectAsState()

    LaunchedEffect(travelInfo) {
        if (travelInfo != null) {
            viewModel.loadWeatherFromDb(parsedDate)
            weatherFromDb?.let {
                if (it.isEmpty()) {
                    viewModel.getWeatherForecast(lat, lon, parsedDate)
                }
            } ?: run {
                viewModel.getWeatherForecast(lat, lon, parsedDate)
            }
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Hava Durumu Detayları",
                style =
                    MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 28.sp,
                    ),
                modifier = Modifier.padding(bottom = 8.dp),
            )

            Text(
                text = arrivalLocation,
                style =
                    MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 20.sp,
                    ),
                modifier = Modifier.padding(bottom = 24.dp),
            )

            if (weatherData != null) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    items(weatherData!!.forecastList) { weatherForecast ->
                        weatherForecastCard(weatherForecast = weatherForecast)
                    }
                }
            } else if (weatherFromDb != null && weatherFromDb!!.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    items(weatherFromDb!!) { weatherEntity ->
                        weatherForecastCard(weatherForecast = weatherEntity.toWeatherForecast())
                    }
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Hava durumu verisi yüklenemedi.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }
        }
    }
}

@Composable
fun weatherForecastCard(weatherForecast: WeatherForecast) {
    val localDateTime =
        LocalDateTime.ofInstant(
            Instant.ofEpochSecond(weatherForecast.dateTime),
            ZoneId.systemDefault(),
        )

    val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy, EEEE", Locale("tr"))
    val formattedDate = localDateTime.format(formatter)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_calendar),
                    contentDescription = "Date Icon",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp),
                )
                Text(
                    text = formattedDate,
                    style =
                        MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 22.sp,
                        ),
                    modifier = Modifier.padding(start = 12.dp),
                )
            }
            Spacer(
                modifier =
                    Modifier
                        .padding(top = 8.dp)
                        .height(1.dp)
                        .fillMaxWidth(),
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "Gündüz:",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontSize = 18.sp),
                )
                Text(
                    text = "${weatherForecast.temp.day}°C",
                    style =
                        MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.SemiBold,
                        ),
                )
            }

            Spacer(
                modifier =
                    Modifier
                        .padding(top = 8.dp, bottom = 16.dp)
                        .background(Color.Black)
                        .height(1.dp)
                        .fillMaxWidth(),
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "Gece:",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontSize = 18.sp),
                )
                Text(
                    text = "${weatherForecast.temp.night}°C",
                    style =
                        MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.SemiBold,
                        ),
                )
            }
            Spacer(
                modifier =
                    Modifier
                        .padding(top = 8.dp, bottom = 16.dp)
                        .background(Color.Black)
                        .height(1.dp)
                        .fillMaxWidth(),
            )

            weatherForecast.weather.firstOrNull()?.description?.capitalizeWords()?.let {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_thermostat),
                        contentDescription = "Weather Icon",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(36.dp),
                    )
                    Text(
                        text = it,
                        style =
                            MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 20.sp,
                            ),
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }
        }
    }
}
