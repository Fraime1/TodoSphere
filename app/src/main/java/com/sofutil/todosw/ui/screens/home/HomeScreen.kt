package com.sofutil.todosw.ui.screens.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sofutil.todosw.data.Task
import com.sofutil.todosw.data.TaskPriority
import com.sofutil.todosw.ui.components.TaskSphere
import com.sofutil.todosw.ui.components.CompletedSphereAnimation
import com.sofutil.todosw.ui.theme.*
import com.sofutil.todosw.viewmodel.TaskViewModel
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: TaskViewModel,
    onNavigateToAddTask: () -> Unit,
    onNavigateToTaskDetail: (Int) -> Unit,
    onNavigateToStatistics: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val tasks by viewModel.activeTasks.collectAsState()
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    
    var completingTaskId by remember { mutableStateOf<Int?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "My TodoSphere",
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                actions = {
                    IconButton(onClick = onNavigateToStatistics) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = "Statistics",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddTask,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Task"
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            
            if (tasks.isEmpty()) {
                EmptyState(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                )
            } else {
                OrbitView(
                    tasks = tasks,
                    screenWidth = screenWidth,
                    screenHeight = screenHeight,
                    onTaskClick = onNavigateToTaskDetail,
                    onTaskSwipe = { task ->
                        completingTaskId = task.id
                        viewModel.completeTask(task)
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                )
            }
            
            // Show completion animation
            completingTaskId?.let { taskId ->
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CompletedSphereAnimation(
                        onAnimationEnd = {
                            completingTaskId = null
                        },
                        size = 120.dp
                    )
                }
            }
        }
    }
}

@Composable
fun OrbitView(
    tasks: List<Task>,
    screenWidth: androidx.compose.ui.unit.Dp,
    screenHeight: androidx.compose.ui.unit.Dp,
    onTaskClick: (Int) -> Unit,
    onTaskSwipe: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    // Group tasks by priority
    val highPriorityTasks = tasks.filter { it.priority == TaskPriority.HIGH }
    val mediumPriorityTasks = tasks.filter { it.priority == TaskPriority.MEDIUM }
    val lowPriorityTasks = tasks.filter { it.priority == TaskPriority.LOW }
    
    val centerX = screenWidth / 2
    val centerY = screenHeight / 2
    
    // Define orbit radii
    val highOrbitRadius = 100.dp
    val mediumOrbitRadius = 180.dp
    val lowOrbitRadius = 260.dp
    
    // Animation for orbital rotation
    val infiniteTransition = rememberInfiniteTransition(label = "orbit_rotation")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(30000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    Box(modifier = modifier) {
        // Draw orbits and core (with transparency to show stars behind)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            
            // Draw orbits FIRST (transparent)
            listOf(
                highOrbitRadius.toPx(),
                mediumOrbitRadius.toPx(),
                lowOrbitRadius.toPx()
            ).forEach { radius ->
                drawCircle(
                    color = OrbitGlow,
                    radius = radius,
                    center = center,
                    style = Stroke(width = 2f),
                    alpha = 0.3f
                )
            }
            
            // Draw core sphere (semi-transparent)
            drawCircle(
                color = SpherePurple,
                radius = 40f,
                center = center,
                alpha = 0.7f
            )
            drawCircle(
                color = SpherePink,
                radius = 35f,
                center = center,
                alpha = 0.5f
            )
        }
        
        // Position tasks on orbits with rotation
        PositionTasksOnOrbit(
            tasks = highPriorityTasks,
            centerX = centerX,
            centerY = centerY,
            radius = highOrbitRadius,
            rotationOffset = rotationAngle,
            onTaskClick = onTaskClick,
            onTaskSwipe = onTaskSwipe
        )
        
        PositionTasksOnOrbit(
            tasks = mediumPriorityTasks,
            centerX = centerX,
            centerY = centerY,
            radius = mediumOrbitRadius,
            rotationOffset = rotationAngle,
            onTaskClick = onTaskClick,
            onTaskSwipe = onTaskSwipe
        )
        
        PositionTasksOnOrbit(
            tasks = lowPriorityTasks,
            centerX = centerX,
            centerY = centerY,
            radius = lowOrbitRadius,
            rotationOffset = rotationAngle,
            onTaskClick = onTaskClick,
            onTaskSwipe = onTaskSwipe
        )
    }
}

@Composable
fun BoxScope.PositionTasksOnOrbit(
    tasks: List<Task>,
    centerX: androidx.compose.ui.unit.Dp,
    centerY: androidx.compose.ui.unit.Dp,
    radius: androidx.compose.ui.unit.Dp,
    rotationOffset: Float,
    onTaskClick: (Int) -> Unit,
    onTaskSwipe: (Task) -> Unit
) {
    tasks.forEachIndexed { index, task ->
        // Calculate angle with rotation animation
        val baseAngle = (2 * PI * index / tasks.size.coerceAtLeast(1)).toFloat()
        val rotationRadians = Math.toRadians(rotationOffset.toDouble()).toFloat()
        val angle = baseAngle + rotationRadians
        
        val x = centerX + (radius.value * cos(angle)).dp - 40.dp
        val y = centerY + (radius.value * sin(angle)).dp - 40.dp
        
        Box(
            modifier = Modifier
                .offset(x = x, y = y)
                .pointerInput(task.id) {
                    detectTapGestures(
                        onTap = { 
                            onTaskClick(task.id)
                        },
                        onLongPress = { 
                            onTaskSwipe(task)
                        }
                    )
                }
        ) {
            TaskSphere(
                task = task,
                size = when (task.priority) {
                    TaskPriority.HIGH -> 80.dp
                    TaskPriority.MEDIUM -> 70.dp
                    TaskPriority.LOW -> 60.dp
                },
                onClick = { onTaskClick(task.id) }
            )
        }
    }
}

@Composable
fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "No tasks yet",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Tap + to add your first sphere",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

