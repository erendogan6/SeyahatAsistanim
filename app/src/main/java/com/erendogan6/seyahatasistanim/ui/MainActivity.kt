package com.erendogan6.seyahatasistanim.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.erendogan6.seyahatasistanim.ui.screens.appNavigation
import com.erendogan6.seyahatasistanim.ui.screens.lottieLoadingScreen
import com.erendogan6.seyahatasistanim.ui.theme.seyahatAsistanimTheme
import com.erendogan6.seyahatasistanim.ui.viewmodel.TravelViewModel
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            seyahatAsistanimTheme {
                val navController = rememberNavController()
                val travelViewModel: TravelViewModel = koinViewModel()

                var startDestination by remember { mutableStateOf<String?>(null) }
                var isLoading by remember { mutableStateOf(true) }

                LaunchedEffect(Unit) {
                    travelViewModel.loadLastTravelInfo()

                    delay(1500)

                    travelViewModel.travelInfo.collect { travelInfo ->
                        startDestination =
                            if (travelInfo != null) {
                                "home"
                            } else {
                                "travelInfo"
                            }
                        isLoading = false
                    }
                }

                if (isLoading) {
                    lottieLoadingScreen()
                } else {
                    startDestination?.let {
                        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                            appNavigation(
                                navController = navController,
                                modifier = Modifier.padding(innerPadding),
                                startDestination = it,
                            )
                        }
                    }
                }
            }
        }
    }
}
