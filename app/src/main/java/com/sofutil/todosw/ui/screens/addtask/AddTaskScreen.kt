package com.sofutil.todosw.ui.screens.addtask

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sofutil.todosw.data.Task
import com.sofutil.todosw.data.TaskPriority
import com.sofutil.todosw.ui.theme.*
import com.sofutil.todosw.viewmodel.TaskViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskScreen(
    viewModel: TaskViewModel,
    taskId: Int? = null,
    onNavigateBack: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(TaskPriority.MEDIUM) }
    var deadline by remember { mutableStateOf<Long?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    
    val isEditMode = taskId != null
    
    // Load task data if editing
    LaunchedEffect(taskId) {
        if (taskId != null) {
            viewModel.loadTask(taskId)
        }
    }
    
    val selectedTask by viewModel.selectedTask.collectAsState()
    
    LaunchedEffect(selectedTask) {
        selectedTask?.let { task ->
            if (task.id == taskId) {
                title = task.title
                description = task.description
                priority = task.priority
                deadline = task.deadline
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditMode) "Edit Task" else "Add Task",
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Title Input
                CosmicTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = "Title",
                    placeholder = "Enter task title"
                )
                
                // Description Input
                CosmicTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = "Description",
                    placeholder = "Enter task description",
                    minHeight = 120.dp
                )
                
                // Priority Selector
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Priority",
                        style = MaterialTheme.typography.titleMedium
                    )
                    PrioritySelector(
                        selected = priority,
                        onSelect = { priority = it }
                    )
                }
                
                // Deadline Picker
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Deadline",
                        style = MaterialTheme.typography.titleMedium
                    )
                    DeadlineButton(
                        deadline = deadline,
                        onClick = { showDatePicker = true }
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Save Button
                GlowingButton(
                    text = if (isEditMode) "Update" else "Save",
                    onClick = {
                        if (title.isNotBlank()) {
                            val task = if (isEditMode && taskId != null) {
                                Task(
                                    id = taskId,
                                    title = title,
                                    description = description,
                                    priority = priority,
                                    deadline = deadline
                                )
                            } else {
                                Task(
                                    title = title,
                                    description = description,
                                    priority = priority,
                                    deadline = deadline
                                )
                            }
                            
                            if (isEditMode) {
                                viewModel.updateTask(task)
                            } else {
                                viewModel.addTask(task)
                            }
                            onNavigateBack()
                        }
                    },
                    enabled = title.isNotBlank()
                )
            }
        }
    }
    
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            onDateSelected = { selectedDate ->
                deadline = selectedDate
                showDatePicker = false
            }
        )
    }
}

@Composable
fun CosmicTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    minHeight: androidx.compose.ui.unit.Dp = 56.dp,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium
        )
        
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(text = placeholder)
            },
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = minHeight)
        )
    }
}

@Composable
fun PrioritySelector(
    selected: TaskPriority,
    onSelect: (TaskPriority) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        PriorityChip(
            priority = TaskPriority.HIGH,
            label = "High",
            color = SpherePink,
            isSelected = selected == TaskPriority.HIGH,
            onClick = { onSelect(TaskPriority.HIGH) },
            modifier = Modifier.weight(1f)
        )
        PriorityChip(
            priority = TaskPriority.MEDIUM,
            label = "Medium",
            color = SpherePurple,
            isSelected = selected == TaskPriority.MEDIUM,
            onClick = { onSelect(TaskPriority.MEDIUM) },
            modifier = Modifier.weight(1f)
        )
        PriorityChip(
            priority = TaskPriority.LOW,
            label = "Low",
            color = SphereLightPink,
            isSelected = selected == TaskPriority.LOW,
            onClick = { onSelect(TaskPriority.LOW) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun PriorityChip(
    priority: TaskPriority,
    label: String,
    color: androidx.compose.ui.graphics.Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) color.copy(alpha = 0.3f) else SurfaceDark.copy(alpha = 0.4f),
        label = "bg_color"
    )
    
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) color else OrbitGlow.copy(alpha = 0.3f),
        label = "border_color"
    )
    
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(2.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            color = if (isSelected) TextWhite else TextGray
        )
    }
}

@Composable
fun DeadlineButton(
    deadline: Long?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceDark.copy(alpha = 0.4f))
            .border(1.dp, OrbitGlow.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = "Select Date",
                tint = SpherePurple
            )
            Text(
                text = deadline?.let { dateFormat.format(Date(it)) } ?: "Select deadline (optional)",
                style = MaterialTheme.typography.bodyLarge,
                color = if (deadline != null) TextWhite else TextGray.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun GlowingButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            disabledContainerColor = androidx.compose.ui.graphics.Color.Transparent
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(
                brush = if (enabled) {
                    Brush.horizontalGradient(
                        colors = listOf(SpherePink, SpherePurple)
                    )
                } else {
                    Brush.horizontalGradient(
                        colors = listOf(TextGray.copy(alpha = 0.3f), TextGray.copy(alpha = 0.3f))
                    )
                },
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleLarge,
            color = TextWhite
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    onDismissRequest: () -> Unit,
    onDateSelected: (Long) -> Unit
) {
    // Allow selecting past dates for testing overdue functionality
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis(),
        yearRange = IntRange(2020, 2030) // Allow past dates
    )
    
    AlertDialog(
        onDismissRequest = onDismissRequest
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp)
        ) {
            Column {
                DatePicker(state = datePickerState)
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                ) {
                    TextButton(onClick = onDismissRequest) {
                        Text("Cancel")
                    }
                    TextButton(
                        onClick = {
                            datePickerState.selectedDateMillis?.let { onDateSelected(it) }
                        }
                    ) {
                        Text("OK")
                    }
                }
            }
        }
    }
}

