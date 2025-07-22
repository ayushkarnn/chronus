package ayush.chronos.presentation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import ayush.chronos.presentation.auth.LoginScreen
import ayush.chronos.presentation.home.HomeScreen

object Destinations {
    const val LOGIN = "login"
    const val HOME = "home"
}

@Composable
fun ChronosNavGraph(
    navController: NavHostController,
    initialRoute: String
) {
    NavHost(
        navController = navController,
        startDestination = initialRoute
    ) {
        composable(Destinations.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Destinations.HOME) {
                        popUpTo(Destinations.LOGIN) { inclusive = true }
                    }
                }
            )
        }
        composable(Destinations.HOME) {
            HomeScreen()
        }
    }
}
