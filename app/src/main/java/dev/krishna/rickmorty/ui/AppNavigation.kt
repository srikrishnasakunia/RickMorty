package dev.krishna.rickmorty.ui

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dev.krishna.rickmorty.ui.screen.CharacterDetailsScreen
import dev.krishna.rickmorty.ui.screen.MainScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "main") {
        composable("main") {
            MainScreen(
                viewModel = hiltViewModel(),
                onCharacterClicked = { characterId ->
                    navController.navigate("details/$characterId")
                }
            )
        }
        composable(
            route = "details/{characterId}",
            arguments = listOf(navArgument("characterId") { type = NavType.IntType })
        ) {
            CharacterDetailsScreen(viewModel = hiltViewModel())
        }
    }
}
