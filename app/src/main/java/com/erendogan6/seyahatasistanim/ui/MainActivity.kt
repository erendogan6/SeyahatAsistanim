package com.erendogan6.seyahatasistanim.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.erendogan6.seyahatasistanim.ui.screens.appNavigation
import com.erendogan6.seyahatasistanim.ui.theme.SeyahatAsistanımTheme
import com.erendogan6.seyahatasistanim.ui.viewmodel.TravelViewModel
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SeyahatAsistanımTheme {
                val navController = rememberNavController()
                val startDestination = remember { mutableStateOf("travelInfo") }

                val travelViewModel: TravelViewModel = koinViewModel()

                LaunchedEffect(Unit) {
                    travelViewModel.loadLastTravelInfo()
                    travelViewModel.travelInfo.collect { travelInfo ->
                        if (travelInfo != null) {
                            startDestination.value = "home"
                        }
                    }
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    appNavigation(
                        navController = navController,
                        modifier = Modifier.padding(innerPadding),
                        startDestination = startDestination.value,
                    )
                }
            }
        }
    }
}
