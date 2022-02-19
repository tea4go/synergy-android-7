package org.synergy

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.synergy.ui.screens.home.HomeScreen
import org.synergy.ui.screens.settings.SettingsScreen
import org.synergy.utils.LocalToolbarState

@Composable
fun MainContent(
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    val scaffoldState = rememberScaffoldState()

    Scaffold(
        modifier = modifier,
        scaffoldState = scaffoldState,
        topBar = { MainTopBar() },
        content = {
            MainNavHost(
                modifier = Modifier.padding(it),
                navController = navController,
            )
        }
    )
}

@Composable
private fun MainTopBar(
    modifier: Modifier = Modifier,
) {
    val title by LocalToolbarState.current.title
    val actions by LocalToolbarState.current.actions

    TopAppBar(
        modifier = modifier,
        title = { Text(text = title) },
        actions = { actions?.invoke(this) }
    )
}

@Composable
private fun MainNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController,
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = "home",
    ) {
        composable("home") {
            HomeScreen(
                openSettings = { navController.navigateToSettings() }
            )
        }
        composable("settings") {
            SettingsScreen()
        }
    }
}

private fun NavHostController.navigateToSettings() = this.navigate("settings") {
    launchSingleTop = true
}
