package com.erendogan6.seyahatasistanim.presentation.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun appNavigation(
    navController: NavHostController,
    modifier: Modifier,
    startDestination: String,
) {
    NavHost(
        navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        composable("travelInfo") {
            travelInfoScreen(navController = navController)
        }
        composable("home") {
            homeScreen(
                onNavigateToWeather = { navController.navigate("weatherDetail") },
                onNavigateToChecklist = { navController.navigate("checklist") },
                onNavigateToLocalInfo = { navController.navigate("localInfo") },
                onNavigateToChatGpt = { navController.navigate("chatGpt") },
                onNavigateToTravelInfo = { navController.navigate("travelInfo") },
            )
        }
        composable("weatherDetail") {
            weatherDetailScreen()
        }
        composable("checklist") { checklistScreen() }
        composable("localInfo") { localInfoScreen() }
        composable("chatGpt") { chatGptScreen() }
    }
}
