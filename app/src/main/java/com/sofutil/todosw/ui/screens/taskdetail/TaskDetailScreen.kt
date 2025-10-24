package com.sofutil.todosw.ui.screens.taskdetail

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
fun TaskDetailScreen(
    taskId: Int,
    viewModel: TaskViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(taskId) {
        viewModel.loadTask(taskId)
    }
    
    val task by viewModel.selectedTask.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Task Details",
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
                ),
                actions = {
                    IconButton(onClick = onNavigateToEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            
            task?.let { currentTask ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Large sphere visualization
                    TaskSphereVisualization(task = currentTask)
                    
                    // Task info card
                    TaskInfoCard(task = currentTask)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Complete button
                    CompleteButton(
                        onClick = {
                            viewModel.completeTask(currentTask)
                            onNavigateBack()
                        }
                    )
                }
            } ?: run {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = SpherePink)
                }
            }
        }
    }
    
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text("Delete Task")
            },
            text = {
                Text("Are you sure you want to delete this task?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        task?.let { viewModel.deleteTask(it) }
                        onNavigateBack()
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun TaskSphereVisualization(
    task: Task,
    modifier: Modifier = Modifier
) {
    val sphereColor = when (task.priority) {
        TaskPriority.HIGH -> SpherePink
        TaskPriority.MEDIUM -> SpherePurple
        TaskPriority.LOW -> SphereLightPink
    }
    
    val infiniteTransition = rememberInfiniteTransition(label = "sphere_rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(150.dp)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = size.minDimension / 2f
            
            // Outer glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        sphereColor.copy(alpha = 0.6f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = radius * 1.5f
                ),
                center = center,
                radius = radius * 1.5f
            )
            
            // Main sphere
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        sphereColor,
                        sphereColor.copy(alpha = 0.8f),
                        sphereColor.copy(alpha = 0.6f)
                    ),
                    center = center,
                    radius = radius
                ),
                center = center,
                radius = radius
            )
            
            // Highlight
            drawCircle(
                color = Color.White.copy(alpha = 0.4f),
                center = Offset(center.x - radius * 0.3f, center.y - radius * 0.3f),
                radius = radius * 0.3f
            )
        }
    }
}

@Composable
fun TaskInfoCard(
    task: Task,
    modifier: Modifier = Modifier
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
        // Title
        Text(
            text = task.title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Divider(color = MaterialTheme.colorScheme.outline)
        
        // Description
        if (task.description.isNotBlank()) {
            InfoRow(label = "Description", value = task.description)
        }
        
        // Priority
        InfoRow(
            label = "Priority",
            value = task.priority.name.lowercase().replaceFirstChar { it.uppercase() }
        )
        
        // Deadline
        task.deadline?.let { deadline ->
            val isOverdue = deadline < System.currentTimeMillis()
            InfoRow(
                label = "Deadline",
                value = dateFormat.format(Date(deadline)),
                valueColor = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
        }
        
        // Created
        InfoRow(
            label = "Created",
            value = dateFormat.format(Date(task.createdAt))
        )
        }
    }
}

@Composable
fun InfoRow(
    label: String,
    value: String,
    valueColor: Color? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = valueColor ?: MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun CompleteButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(SphereGold, SuccessGreen)
                ),
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = CosmicDeepPurple
            )
            Text(
                text = "Complete Task",
                style = MaterialTheme.typography.titleLarge,
                color = CosmicDeepPurple
            )
        }
    }
}

