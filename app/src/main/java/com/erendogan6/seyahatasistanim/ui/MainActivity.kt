package com.erendogan6.seyahatasistanim.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.erendogan6.seyahatasistanim.BuildConfig
import com.erendogan6.seyahatasistanim.ui.screens.weatherScreen
import com.erendogan6.seyahatasistanim.ui.theme.SeyahatAsistanımTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SeyahatAsistanımTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    weatherScreen(
                        lat = 35.0,
                        lon = 139.0,
                        apiKey = BuildConfig.OPENWEATHER_API_KEY,
                        modifier = Modifier.padding(innerPadding),
                    )
                }
            }
        }
    }
}
