package com.erendogan6.seyahatasistanim.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun localInfoScreen(modifier: Modifier = Modifier) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
    ) {
        Text(
            text = "Yerel Bilgiler",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp),
        )

        BasicText(
            text =
                """
                - Dil: Türkçe
                - Para Birimi: Türk Lirası
                - Acil Durum Numarası: 112
                - Yerel Yemekler: Kebap, Baklava, Döner
                - Turistik Yerler: Sultanahmet, Kapadokya, Pamukkale
                """.trimIndent(),
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}
