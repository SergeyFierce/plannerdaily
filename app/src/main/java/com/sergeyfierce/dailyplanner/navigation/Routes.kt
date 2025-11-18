package com.sergeyfierce.dailyplanner.navigation

sealed class Route(val path: String, val label: String) {
    data object Calendar : Route("calendar", "Календарь")
    data object Notes : Route("notes", "Заметки")
    data object Settings : Route("settings", "Настройки")
}
