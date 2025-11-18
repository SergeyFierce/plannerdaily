package com.sergeyfierce.dailyplanner.feature.calendar

import java.time.LocalDate
import java.time.LocalTime

enum class CalendarMode { CALENDAR, DAY }

enum class TaskType { POINT, INTERVAL }

data class DayTask(
    val id: String,
    val title: String,
    val type: TaskType,
    val startTime: LocalTime,
    val endTime: LocalTime?,
    val isDone: Boolean
)

data class TaskLayout(
    val task: DayTask,
    val column: Int,
    val columnsInGroup: Int
)

data class CalendarUiState(
    val selectedDate: LocalDate,
    val displayMonth: java.time.YearMonth,
    val mode: CalendarMode,
    val tasksForSelectedDate: List<DayTask>,
    val tasksByDate: Map<LocalDate, List<DayTask>>,
    val pixelsPerMinute: Float
)
