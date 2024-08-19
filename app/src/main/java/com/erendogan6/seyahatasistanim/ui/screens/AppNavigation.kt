package com.erendogan6.seyahatasistanim.ui.screens

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
    NavHost(navController, startDestination = startDestination, modifier = modifier) {
        composable("travelInfo") {
            travelInfoScreen(navController = navController)
        }
        composable("home") {
            homeScreen(
                onNavigateToWeather = {
                    navController.navigate("weatherDetail?lat=41.0082&lon=28.9784")
                },
                onNavigateToChecklist = { navController.navigate("checklist") },
                onNavigateToLocalInfo = { navController.navigate("localInfo") },
                onNavigateToChatGpt = { navController.navigate("chatGpt") },
            )
        }
        composable("weatherDetail?lat={lat}&lon={lon}") { backStackEntry ->
            val lat = backStackEntry.arguments?.getString("lat")?.toDouble() ?: 0.0
            val lon = backStackEntry.arguments?.getString("lon")?.toDouble() ?: 0.0
            weatherDetailScreen(lat = lat, lon = lon)
        }
        composable("checklist") { checklistScreen() }
        composable("localInfo") { localInfoScreen() }
        composable("chatGpt") { chatGptScreen() }
    }
}
