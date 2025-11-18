package com.sergeyfierce.dailyplanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EventNote
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.ViewDay
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sergeyfierce.dailyplanner.navigation.PlannerNavGraph
import com.sergeyfierce.dailyplanner.navigation.Route
import com.sergeyfierce.dailyplanner.ui.theme.DailyPlannerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { DailyPlannerApp() }
    }
}

@Composable
fun DailyPlannerApp() {
    val navController = rememberNavController()
    var isDarkTheme by rememberSaveable { mutableStateOf(isSystemInDarkTheme()) }
    var useDynamicColor by rememberSaveable { mutableStateOf(true) }

    DailyPlannerTheme(darkTheme = isDarkTheme, useDynamicColor = useDynamicColor) {
        Scaffold(
            bottomBar = {
                PlannerBottomBar(navController = navController)
            }
        ) { innerPadding ->
            PlannerNavGraph(
                navController = navController,
                modifier = Modifier.padding(innerPadding),
                onThemeChanged = { isDarkTheme = it },
                darkThemeEnabled = isDarkTheme,
                useDynamicColor = useDynamicColor,
                onDynamicChanged = { useDynamicColor = it }
            )
        }
    }
}

@Composable
private fun PlannerBottomBar(navController: androidx.navigation.NavHostController) {
    val items = listOf(
        Route.Calendar to Icons.Outlined.ViewDay,
        Route.Notes to Icons.Outlined.EventNote,
        Route.Settings to Icons.Outlined.Settings
    )
    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination
        items.forEach { (route, icon) ->
            val selected = currentDestination?.route == route.path
            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(route.path) {
                        launchSingleTop = true
                        restoreState = true
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                    }
                },
                icon = { Icon(icon, contentDescription = route.label) },
                label = { Text(route.label) }
            )
        }
    }
}
