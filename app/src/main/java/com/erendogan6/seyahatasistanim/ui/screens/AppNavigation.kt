package com.erendogan6.seyahatasistanim.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import java.time.LocalDate

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
        composable(
            "home/{arrivalDate}",
            arguments = listOf(navArgument("arrivalDate") { type = NavType.StringType }),
        ) { backStackEntry ->
            val arrivalDateString = backStackEntry.arguments?.getString("arrivalDate") ?: ""
            homeScreen(
                onNavigateToWeather = {
                    navController.navigate("weatherDetail?lat=41.0082&lon=28.9784&travelDate=$arrivalDateString")
                },
                onNavigateToChecklist = { navController.navigate("checklist") },
                onNavigateToLocalInfo = { navController.navigate("localInfo") },
                onNavigateToChatGpt = { navController.navigate("chatGpt") },
                onNavigateToTravelInfo = { navController.navigate("travelInfo") },
            )
        }
        composable(
            "weatherDetail?lat={lat}&lon={lon}&travelDate={travelDate}",
            arguments =
                listOf(
                    navArgument("lat") { type = NavType.StringType },
                    navArgument("lon") { type = NavType.StringType },
                    navArgument("travelDate") { type = NavType.StringType },
                ),
        ) { backStackEntry ->
            val lat = backStackEntry.arguments?.getString("lat")?.toDouble() ?: 0.0
            val lon = backStackEntry.arguments?.getString("lon")?.toDouble() ?: 0.0
            val travelDateString = backStackEntry.arguments?.getString("travelDate") ?: ""

            val travelDate = LocalDate.parse(travelDateString)

            weatherDetailScreen(lat = lat, lon = lon, travelDate = travelDate)
        }
        composable("checklist") { checklistScreen() }
        composable("localInfo") { localInfoScreen() }
        composable("chatGpt") { chatGptScreen() }
    }
}
