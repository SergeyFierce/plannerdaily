package com.sergeyfierce.dailyplanner.feature.calendar

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBackIosNew
import androidx.compose.material.icons.outlined.ArrowForwardIos
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material3.AssistChip
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun CalendarScreen(viewModel: CalendarViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    CalendarContent(
        state = state,
        onDateSelected = viewModel::onDateSelected,
        onMonthChanged = viewModel::onMonthChanged,
        onModeChanged = viewModel::setMode,
        onPixelsPerMinuteChanged = viewModel::setPixelsPerMinute,
        onAddPoint = viewModel::addPointTask,
        onDelete = viewModel::deleteTask,
        onToggleDone = viewModel::toggleDone,
        onTitleChanged = viewModel::updateTitle
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun CalendarContent(
    state: CalendarUiState,
    onDateSelected: (LocalDate) -> Unit,
    onMonthChanged: (Long) -> Unit,
    onModeChanged: (CalendarMode) -> Unit,
    onPixelsPerMinuteChanged: (Float) -> Unit,
    onAddPoint: (LocalDate, LocalTime) -> Unit,
    onDelete: (String, LocalDate) -> Unit,
    onToggleDone: (String, LocalDate) -> Unit,
    onTitleChanged: (String, String, LocalDate) -> Unit
) {
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var editingTask by remember { mutableStateOf<Pair<LocalDate, DayTask>?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text(
                    text = state.selectedDate.format(DateTimeFormatter.ofPattern("d MMMM, EEEE", Locale.getDefault())),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            actions = {
                AssistChip(
                    onClick = { showDatePicker = true },
                    leadingIcon = { Icon(Icons.Outlined.DateRange, contentDescription = null) },
                    label = { Text("Выбрать дату") }
                )
            }
        )

        SingleChoiceSegmentedButtonRow(modifier = Modifier.padding(horizontal = 16.dp)) {
            SegmentedButton(
                selected = state.mode == CalendarMode.CALENDAR,
                onClick = { onModeChanged(CalendarMode.CALENDAR) },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                label = { Text("Календарь") }
            )
            SegmentedButton(
                selected = state.mode == CalendarMode.DAY,
                onClick = { onModeChanged(CalendarMode.DAY) },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                label = { Text("День") }
            )
        }

        when (state.mode) {
            CalendarMode.CALENDAR -> MonthCalendar(
                state = state,
                onDateSelected = onDateSelected,
                onMonthChanged = onMonthChanged
            )
            CalendarMode.DAY -> DayTimeline(
                date = state.selectedDate,
                tasks = state.tasksForSelectedDate,
                pixelsPerMinute = state.pixelsPerMinute,
                onAddPoint = onAddPoint,
                onLongPress = { task -> editingTask = state.selectedDate to task },
                onToggleDone = { onToggleDone(it.id, state.selectedDate) },
                onZoomChange = onPixelsPerMinuteChanged
            )
        }
    }

    if (showDatePicker) {
        ModalBottomSheet(
            onDismissRequest = { showDatePicker = false },
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            val pickerState = rememberDatePickerState(initialSelectedDateMillis = state.selectedDate.toEpochDay() * 86_400_000)
            DatePicker(state = pickerState)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = { showDatePicker = false }) { Text("Отмена") }
                Button(onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        onDateSelected(LocalDate.ofEpochDay(millis / 86_400_000))
                    }
                    showDatePicker = false
                }) { Text("Готово") }
            }
        }
    }

    editingTask?.let { (date, task) ->
        var title by remember(task) { mutableStateOf(task.title) }
        var done by remember(task) { mutableStateOf(task.isDone) }
        ModalBottomSheet(onDismissRequest = { editingTask = null }) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Редактирование", fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    label = { Text("Название") }
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 12.dp)
                ) {
                    AssistChip(
                        onClick = { done = !done },
                        label = { Text(if (done) "Выполнено" else "Открыто") }
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = {
                        onTitleChanged(task.id, title, date)
                        if (task.isDone != done) onToggleDone(task.id, date)
                        editingTask = null
                    }) {
                        Text("Сохранить")
                    }
                    TextButton(onClick = {
                        onDelete(task.id, date)
                        editingTask = null
                    }) {
                        Text("Удалить")
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthCalendar(
    state: CalendarUiState,
    onDateSelected: (LocalDate) -> Unit,
    onMonthChanged: (Long) -> Unit
) {
    val weekFields = remember { WeekFields.of(Locale.getDefault()) }
    val firstDayOfMonth = state.displayMonth.atDay(1)
    val daysInMonth = state.displayMonth.lengthOfMonth()
    val startOffset = (firstDayOfMonth.dayOfWeek.value - weekFields.firstDayOfWeek.value + 7) % 7
    val totalCells = startOffset + daysInMonth
    val rows = (totalCells / 7) + if (totalCells % 7 != 0) 1 else 0

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { onMonthChanged(-1) }) {
                Icon(Icons.Outlined.ArrowBackIosNew, contentDescription = "prev")
            }
            Text(
                text = state.displayMonth.format(DateTimeFormatter.ofPattern("LLLL yyyy", Locale.getDefault())),
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { onMonthChanged(1) }) {
                Icon(Icons.Outlined.ArrowForwardIos, contentDescription = "next")
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            DayOfWeek.values().forEach { day ->
                Text(
                    text = day.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                    modifier = Modifier.padding(vertical = 4.dp),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Column {
            var day = 1
            repeat(rows) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    repeat(7) { index ->
                        val currentDay = day
                        if (it == 0 && index < startOffset || currentDay > daysInMonth) {
                            Spacer(modifier = Modifier.weight(1f).padding(4.dp))
                        } else {
                            val date = state.displayMonth.atDay(currentDay)
                            val tasks = state.tasksByDate[date].orEmpty()
                            DayCell(
                                date = date,
                                isSelected = date == state.selectedDate,
                                hasTasks = tasks.isNotEmpty(),
                                onClick = onDateSelected
                            )
                            day++
                        }
                    }
                }
            }
        }

        if (state.tasksForSelectedDate.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                state.tasksForSelectedDate.forEach { task ->
                    TaskRow(task = task)
                }
            }
        }
    }
}

@Composable
private fun DayCell(date: LocalDate, isSelected: Boolean, hasTasks: Boolean, onClick: (LocalDate) -> Unit) {
    val container = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
    Card(
        modifier = Modifier
            .padding(4.dp)
            .width(44.dp),
        onClick = { onClick(date) },
        colors = CardDefaults.cardColors(containerColor = container)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = date.dayOfMonth.toString())
            if (hasTasks) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(
                        modifier = Modifier
                            .height(4.dp)
                            .width(18.dp)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
            }
        }
    }
}

@Composable
private fun TaskRow(task: DayTask) {
    val alpha = if (task.isDone) 0.6f else 1f
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textDecoration = if (task.isDone) TextDecoration.LineThrough else null,
                    modifier = Modifier.graphicsLayer(alpha = alpha)
                )
                Text(
                    text = timeRangeText(task),
                    modifier = Modifier.graphicsLayer(alpha = alpha),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (task.isDone) {
                Text(text = "✓")
            }
        }
    }
}

@Composable
private fun timeRangeText(task: DayTask): String {
    val formatter = DateTimeFormatter.ofPattern("HH:mm")
    return when (task.type) {
        TaskType.POINT -> task.startTime.format(formatter)
        TaskType.INTERVAL -> "${task.startTime.format(formatter)} — ${task.endTime?.format(formatter) ?: ""}"
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DayTimeline(
    date: LocalDate,
    tasks: List<DayTask>,
    pixelsPerMinute: Float,
    onAddPoint: (LocalDate, LocalTime) -> Unit,
    onLongPress: (DayTask) -> Unit,
    onToggleDone: (DayTask) -> Unit,
    onZoomChange: (Float) -> Unit
) {
    val density = LocalDensity.current
    val heightPerMinute = pixelsPerMinute.dp
    val totalHeight = 24 * 60 * heightPerMinute
    val scrollState = rememberScrollState()
    val layouts = remember(tasks, pixelsPerMinute) { layoutTasks(tasks) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Таймлайн", fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Зум")
                Slider(
                    value = pixelsPerMinute,
                    onValueChange = onZoomChange,
                    valueRange = 0.6f..3.5f,
                    steps = 6,
                    modifier = Modifier.width(160.dp)
                )
            }
        }

        Box(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxSize()
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(totalHeight)
                    .verticalScroll(scrollState)
                    .pointerInput(tasks, pixelsPerMinute) {
                        detectTapGestures { offset ->
                            val minute = (offset.y / density.density / heightPerMinute.value).roundToInt().coerceIn(0, 1439)
                            val time = LocalTime.of(minute / 60, minute % 60)
                            onAddPoint(date, time)
                        }
                    }
            ) {
                HourLines(heightPerMinute = heightPerMinute)
                val maxColumns = layouts.maxOfOrNull { it.columnsInGroup } ?: 1
                val columnWidth = (maxWidth - 12.dp) / maxColumns
                layouts.forEach { layout ->
                    TaskBlock(
                        layout = layout,
                        heightPerMinute = heightPerMinute,
                        columnWidth = columnWidth,
                        onLongPress = onLongPress,
                        onToggleDone = onToggleDone
                    )
                }
            }
        }
    }
}

@Composable
private fun HourLines(heightPerMinute: Dp) {
    Box(modifier = Modifier.fillMaxSize()) {
        repeat(25) { hour ->
            val y = hour * 60 * heightPerMinute
            Row(
                modifier = Modifier
                    .offset(y = y)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "%02d:00".format(hour % 24),
                    modifier = Modifier.width(52.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(
                    modifier = Modifier
                        .height(1.dp)
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TaskBlock(
    layout: TaskLayout,
    heightPerMinute: Dp,
    columnWidth: Dp,
    onLongPress: (DayTask) -> Unit,
    onToggleDone: (DayTask) -> Unit
) {
    val task = layout.task
    val startMinutes = task.startTime.toSecondOfDay() / 60
    val endMinutes = task.endTime?.toSecondOfDay()?.div(60) ?: startMinutes + 15
    val durationMinutes = (endMinutes - startMinutes).coerceAtLeast(12)
    val topOffset = startMinutes * heightPerMinute
    val blockHeight = durationMinutes * heightPerMinute
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .offset(y = topOffset)
    ) {
        Card(
            modifier = Modifier
                .offset(x = columnWidth * layout.column)
                .width(columnWidth - 6.dp)
                .height(blockHeight)
                .combinedClickable(
                    onClick = { onToggleDone(task) },
                    onLongClick = { onLongPress(task) }
                ),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = task.title,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textDecoration = if (task.isDone) TextDecoration.LineThrough else null,
                    modifier = Modifier.graphicsLayer(alpha = if (task.isDone) 0.6f else 1f)
                )
                Text(
                    text = timeRangeText(task),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.graphicsLayer(alpha = if (task.isDone) 0.6f else 1f)
                )
            }
        }
    }
}

private fun layoutTasks(tasks: List<DayTask>): List<TaskLayout> {
    data class Active(val task: DayTask, val column: Int)
    val sorted = tasks.sortedBy { it.startTime }
    val active = mutableListOf<Active>()
    val assignments = mutableMapOf<String, Int>()
    val columnsForTask = mutableMapOf<String, Int>()

    fun endMinute(task: DayTask): Int {
        val end = task.endTime ?: task.startTime.plusMinutes(15)
        return end.toSecondOfDay() / 60
    }

    sorted.forEach { task ->
        val startMinute = task.startTime.toSecondOfDay() / 60
        active.removeAll { endMinute(it.task) <= startMinute }
        val usedColumns = active.map { it.column }.toSet()
        val column = generateSequence(0) { it + 1 }.first { it !in usedColumns }
        active.add(Active(task, column))
        assignments[task.id] = column
        val overlap = active.size
        active.forEach { activeTask ->
            columnsForTask[activeTask.task.id] = maxOf(columnsForTask[activeTask.task.id] ?: 1, overlap)
        }
    }

    return tasks.map { task ->
        TaskLayout(
            task = task,
            column = assignments[task.id] ?: 0,
            columnsInGroup = columnsForTask[task.id] ?: 1
        )
    }
}
