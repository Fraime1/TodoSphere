package com.sofutil.todosw.ui.screens.statistics

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
import com.sofutil.todosw.data.TaskStats
import com.sofutil.todosw.ui.theme.*
import com.sofutil.todosw.viewmodel.TaskViewModel
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: TaskViewModel,
    onNavigateBack: () -> Unit
) {
    val stats by viewModel.stats.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Statistics",
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
            
            if (stats.totalTasks == 0) {
                EmptyStatsState(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Bubble chart visualization
                    BubbleChart(stats = stats)
                    
                    // Weekly progress message
                    ProgressMessage(completedThisWeek = stats.completedThisWeek)
                    
                    // Stats cards
                    StatsGrid(stats = stats)
                }
            }
        }
    }
}

@Composable
fun BubbleChart(
    stats: TaskStats,
    modifier: Modifier = Modifier
) {
    // Create bubbles based on actual stats with minimum visualization
    val bubbles = remember(stats) {
        buildList {
            // Show at least a few bubbles for visualization even if counts are low
            val completedCount = maxOf(stats.completedTasks, if (stats.completedTasks > 0) 3 else 0).coerceAtMost(15)
            val activeCount = maxOf(stats.activeTasks, if (stats.activeTasks > 0) 3 else 0).coerceAtMost(15)
            val overdueCount = maxOf(stats.overdueTasks, if (stats.overdueTasks > 0) 2 else 0).coerceAtMost(8)
            
            // Completed tasks (gold)
            repeat(completedCount) {
                add(
                    Bubble(
                        color = SphereGold,
                        x = Random.nextFloat() * 0.9f + 0.05f,
                        y = Random.nextFloat() * 0.7f + 0.15f,
                        radius = Random.nextFloat() * 20f + 20f,
                        speed = Random.nextFloat() * 0.5f + 0.3f
                    )
                )
            }
            // Active tasks (pink/purple)
            repeat(activeCount) {
                add(
                    Bubble(
                        color = if (Random.nextBoolean()) SpherePink else SpherePurple,
                        x = Random.nextFloat() * 0.9f + 0.05f,
                        y = Random.nextFloat() * 0.7f + 0.15f,
                        radius = Random.nextFloat() * 18f + 18f,
                        speed = Random.nextFloat() * 0.5f + 0.3f
                    )
                )
            }
            // Overdue tasks (red)
            repeat(overdueCount) {
                add(
                    Bubble(
                        color = OverdueRed,
                        x = Random.nextFloat() * 0.9f + 0.05f,
                        y = Random.nextFloat() * 0.7f + 0.15f,
                        radius = Random.nextFloat() * 16f + 16f,
                        speed = Random.nextFloat() * 0.5f + 0.3f
                    )
                )
            }
        }
    }
    
    val infiniteTransition = rememberInfiniteTransition(label = "bubble_animation")
    val animationProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "progress"
    )
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceDark.copy(alpha = 0.8f))
    ) {
        if (bubbles.isNotEmpty()) {
            Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                bubbles.forEach { bubble ->
                    // Simple floating animation
                    val yOffset = (animationProgress * bubble.speed * 50f) % 50f
                    val x = bubble.x * size.width
                    val y = (bubble.y * size.height + yOffset) % size.height
                    
                    // Glow
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                bubble.color.copy(alpha = 0.8f),
                                Color.Transparent
                            ),
                            center = Offset(x, y),
                            radius = bubble.radius * 1.5f
                        ),
                        center = Offset(x, y),
                        radius = bubble.radius * 1.5f
                    )
                    
                    // Main bubble
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                bubble.color.copy(alpha = 1f),
                                bubble.color.copy(alpha = 0.8f),
                                bubble.color.copy(alpha = 0.5f)
                            ),
                            center = Offset(x - bubble.radius * 0.3f, y - bubble.radius * 0.3f),
                            radius = bubble.radius
                        ),
                        center = Offset(x, y),
                        radius = bubble.radius
                    )
                    
                    // Highlight
                    drawCircle(
                        color = Color.White.copy(alpha = 0.4f),
                        center = Offset(x - bubble.radius * 0.3f, y - bubble.radius * 0.3f),
                        radius = bubble.radius * 0.3f
                    )
                }
            }
        }
        
        // Legend
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .background(SurfaceDark.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (stats.completedTasks > 0) {
                LegendItem(color = SphereGold, label = "Completed (${stats.completedTasks})")
            }
            if (stats.activeTasks > 0) {
                LegendItem(color = SpherePink, label = "Active (${stats.activeTasks})")
            }
            if (stats.overdueTasks > 0) {
                LegendItem(color = OverdueRed, label = "Overdue (${stats.overdueTasks})")
            }
        }
    }
}

@Composable
fun LegendItem(
    color: Color,
    label: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextGray
        )
    }
}

@Composable
fun ProgressMessage(
    completedThisWeek: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        SpherePink.copy(alpha = 0.3f),
                        SpherePurple.copy(alpha = 0.3f)
                    )
                )
            )
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "You popped $completedThisWeek tasks this week! 🎉",
            style = MaterialTheme.typography.titleMedium,
            color = TextWhite,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun StatsGrid(
    stats: TaskStats,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatCard(
                title = "Total",
                value = stats.totalTasks.toString(),
                color = SpherePurple,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Active",
                value = stats.activeTasks.toString(),
                color = SpherePink,
                modifier = Modifier.weight(1f)
            )
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatCard(
                title = "Completed",
                value = stats.completedTasks.toString(),
                color = SphereGold,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Overdue",
                value = stats.overdueTasks.toString(),
                color = OverdueRed,
                modifier = Modifier.weight(1f)
            )
        }
        
        // Completion rate
        val completionRate = if (stats.totalTasks > 0) {
            (stats.completedTasks * 100 / stats.totalTasks)
        } else 0
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceDark.copy(alpha = 0.6f))
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Completion Rate",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextGray
                )
                Text(
                    text = "$completionRate%",
                    style = MaterialTheme.typography.displayMedium,
                    color = SphereGold
                )
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(120.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceDark.copy(alpha = 0.6f))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = TextGray
            )
            Text(
                text = value,
                style = MaterialTheme.typography.displaySmall,
                color = color
            )
        }
    }
}

@Composable
fun EmptyStatsState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "No statistics yet",
            style = MaterialTheme.typography.headlineMedium,
            color = TextWhite,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Your TodoSphere will grow\nas you complete tasks",
            style = MaterialTheme.typography.bodyLarge,
            color = TextGray,
            textAlign = TextAlign.Center
        )
    }
}

private data class Bubble(
    val color: Color,
    val x: Float,
    val y: Float,
    val radius: Float,
    val speed: Float
)

