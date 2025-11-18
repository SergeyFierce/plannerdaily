package com.sergeyfierce.dailyplanner.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.sergeyfierce.dailyplanner.feature.calendar.CalendarScreen
import com.sergeyfierce.dailyplanner.feature.notes.NotesScreen
import com.sergeyfierce.dailyplanner.feature.settings.SettingsScreen

@Composable
fun PlannerNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    onThemeChanged: (Boolean) -> Unit,
    darkThemeEnabled: Boolean,
    useDynamicColor: Boolean,
    onDynamicChanged: (Boolean) -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Route.Calendar.path,
        modifier = modifier
    ) {
        composable(Route.Calendar.path) {
            CalendarScreen()
        }
        composable(Route.Notes.path) {
            NotesScreen()
        }
        composable(Route.Settings.path) {
            SettingsScreen(
                isDarkTheme = darkThemeEnabled,
                onThemeToggle = onThemeChanged,
                useDynamicColor = useDynamicColor,
                onDynamicToggle = onDynamicChanged
            )
        }
    }
}
