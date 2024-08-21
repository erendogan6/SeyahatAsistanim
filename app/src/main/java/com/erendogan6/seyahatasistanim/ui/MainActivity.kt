package com.erendogan6.seyahatasistanim.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.erendogan6.seyahatasistanim.R
import com.erendogan6.seyahatasistanim.ui.screens.appNavigation
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
                    lottieLoadingScreen(animationResId = R.raw.animation)
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

@Composable
fun lottieLoadingScreen(
    animationResId: Int,
    modifier: Modifier = Modifier,
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(animationResId))
    val progress by animateLottieCompositionAsState(composition)

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        LottieAnimation(
            composition = composition,
            progress = { progress },
        )
    }
}
