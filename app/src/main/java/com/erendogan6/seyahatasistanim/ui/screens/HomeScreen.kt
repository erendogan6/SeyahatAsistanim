package com.erendogan6.seyahatasistanim.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun homeScreen(
    onNavigateToWeather: () -> Unit,
    onNavigateToChecklist: () -> Unit,
    onNavigateToLocalInfo: () -> Unit,
    onNavigateToChatGpt: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "THY Seyahat Hazırlık Asistanı",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 32.dp),
        )

        Button(
            onClick = onNavigateToWeather,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = "Hava Durumu Detayları")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onNavigateToChecklist,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = "Kontrol Listesi")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onNavigateToLocalInfo,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = "Yerel Bilgiler")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onNavigateToChatGpt,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = "ChatGPT ile Etkileşim")
        }
    }
}
