package com.sergeyfierce.dailyplanner.feature.calendar

import androidx.lifecycle.ViewModel
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import androidx.lifecycle.viewModelScope

class CalendarViewModel : ViewModel() {
    private val tasks = MutableStateFlow(seedTasks())
    private val selectedDate = MutableStateFlow(LocalDate.now())
    private val displayMonth = MutableStateFlow(YearMonth.now())
    private val mode = MutableStateFlow(CalendarMode.CALENDAR)
    private val pixelsPerMinute = MutableStateFlow(1.2f)

    val uiState = combine(
        selectedDate,
        displayMonth,
        mode,
        tasks,
        pixelsPerMinute
    ) { date, month, modeValue, taskMap, ppm ->
        CalendarUiState(
            selectedDate = date,
            displayMonth = month,
            mode = modeValue,
            tasksForSelectedDate = taskMap[date].orEmpty().sortedBy { it.startTime },
            tasksByDate = taskMap,
            pixelsPerMinute = ppm
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000),
        CalendarUiState(
            selectedDate = LocalDate.now(),
            displayMonth = YearMonth.now(),
            mode = CalendarMode.CALENDAR,
            tasksForSelectedDate = emptyList(),
            tasksByDate = emptyMap(),
            pixelsPerMinute = 1.2f
        )
    )

    fun onDateSelected(date: LocalDate) {
        selectedDate.value = date
    }

    fun onMonthChanged(delta: Long) {
        displayMonth.value = displayMonth.value.plusMonths(delta)
    }

    fun setMode(mode: CalendarMode) {
        this.mode.value = mode
    }

    fun setPixelsPerMinute(value: Float) {
        pixelsPerMinute.value = value.coerceIn(0.6f, 3.5f)
    }

    fun toggleDone(taskId: String, date: LocalDate = selectedDate.value) {
        updateTask(date, taskId) { it.copy(isDone = !it.isDone) }
    }

    fun updateTitle(taskId: String, title: String, date: LocalDate = selectedDate.value) {
        updateTask(date, taskId) { it.copy(title = title) }
    }

    fun deleteTask(taskId: String, date: LocalDate = selectedDate.value) {
        tasks.value = tasks.value.toMutableMap().apply {
            val updated = get(date).orEmpty().filterNot { it.id == taskId }
            put(date, updated)
        }
    }

    fun addPointTask(date: LocalDate, time: LocalTime) {
        val newTask = DayTask(
            id = UUID.randomUUID().toString(),
            title = "Новая задача",
            type = TaskType.POINT,
            startTime = time,
            endTime = null,
            isDone = false
        )
        tasks.value = tasks.value.toMutableMap().apply {
            val list = get(date).orEmpty().toMutableList()
            list.add(newTask)
            put(date, list)
        }
        selectedDate.value = date
    }

    fun addIntervalTask(
        date: LocalDate,
        start: LocalTime,
        end: LocalTime,
        title: String,
        isDone: Boolean = false
    ) {
        val task = DayTask(
            id = UUID.randomUUID().toString(),
            title = title,
            type = TaskType.INTERVAL,
            startTime = start,
            endTime = end,
            isDone = isDone
        )
        tasks.value = tasks.value.toMutableMap().apply {
            val list = get(date).orEmpty().toMutableList()
            list.add(task)
            put(date, list)
        }
    }

    private fun updateTask(date: LocalDate, taskId: String, transform: (DayTask) -> DayTask) {
        tasks.value = tasks.value.toMutableMap().apply {
            val updated = get(date).orEmpty().map { task ->
                if (task.id == taskId) transform(task) else task
            }
            put(date, updated)
        }
    }

    private fun seedTasks(): Map<LocalDate, List<DayTask>> {
        val today = LocalDate.now()
        val tomorrow = today.plusDays(1)
        return mapOf(
            today to listOf(
                DayTask(
                    id = UUID.randomUUID().toString(),
                    title = "Проверить почту",
                    type = TaskType.POINT,
                    startTime = LocalTime.of(9, 0),
                    endTime = null,
                    isDone = false
                ),
                DayTask(
                    id = UUID.randomUUID().toString(),
                    title = "Созвон с продуктом",
                    type = TaskType.INTERVAL,
                    startTime = LocalTime.of(11, 30),
                    endTime = LocalTime.of(12, 15),
                    isDone = false
                ),
                DayTask(
                    id = UUID.randomUUID().toString(),
                    title = "Работа над дизайном",
                    type = TaskType.INTERVAL,
                    startTime = LocalTime.of(14, 0),
                    endTime = LocalTime.of(16, 0),
                    isDone = true
                ),
                DayTask(
                    id = UUID.randomUUID().toString(),
                    title = "Пробежка",
                    type = TaskType.POINT,
                    startTime = LocalTime.of(19, 30),
                    endTime = null,
                    isDone = false
                )
            ),
            tomorrow to listOf(
                DayTask(
                    id = UUID.randomUUID().toString(),
                    title = "Ревью задач",
                    type = TaskType.INTERVAL,
                    startTime = LocalTime.of(10, 0),
                    endTime = LocalTime.of(11, 0),
                    isDone = false
                ),
                DayTask(
                    id = UUID.randomUUID().toString(),
                    title = "Спортзал",
                    type = TaskType.INTERVAL,
                    startTime = LocalTime.of(18, 0),
                    endTime = LocalTime.of(19, 0),
                    isDone = false
                )
            )
        )
    }
}
