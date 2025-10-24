package com.sofutil.todosw.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sofutil.todosw.data.Task
import com.sofutil.todosw.data.TaskPriority
import com.sofutil.todosw.ui.theme.*

@Composable
fun TaskSphere(
    task: Task,
    modifier: Modifier = Modifier,
    size: Dp = 80.dp,
    onClick: () -> Unit = {}
) {
    // Check if task is overdue
    val isOverdue = task.deadline?.let { it < System.currentTimeMillis() } ?: false
    
    val sphereColor = when {
        isOverdue -> OverdueRed
        task.priority == TaskPriority.HIGH -> SpherePink
        task.priority == TaskPriority.MEDIUM -> SpherePurple
        else -> SphereLightPink
    }
    
    val infiniteTransition = rememberInfiniteTransition(label = "sphere_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )
    
    Column(
        modifier = modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(size),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(this.size.width / 2f, this.size.height / 2f)
                val radius = this.size.minDimension / 2f
                
                // Outer glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            sphereColor.copy(alpha = glowAlpha),
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
                            sphereColor.copy(alpha = 1f),
                            sphereColor.copy(alpha = 0.8f),
                            sphereColor.copy(alpha = 0.6f)
                        ),
                        center = Offset(center.x - radius * 0.3f, center.y - radius * 0.3f),
                        radius = radius
                    ),
                    center = center,
                    radius = radius
                )
                
                // Highlight
                drawCircle(
                    color = Color.White.copy(alpha = 0.3f),
                    center = Offset(center.x - radius * 0.3f, center.y - radius * 0.3f),
                    radius = radius * 0.3f
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = task.title,
            style = MaterialTheme.typography.bodySmall,
            color = if (isOverdue) OverdueRed else TextWhite,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.widthIn(max = size * 1.2f)
        )
    }
}

@Composable
fun CompletedSphereAnimation(
    onAnimationEnd: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 80.dp
) {
    var animationPlayed by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (animationPlayed) 2f else 1f,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "scale"
    )
    
    val alpha by animateFloatAsState(
        targetValue = if (animationPlayed) 0f else 1f,
        animationSpec = tween(500, easing = LinearEasing),
        label = "alpha",
        finishedListener = { onAnimationEnd() }
    )
    
    LaunchedEffect(Unit) {
        animationPlayed = true
    }
    
    Canvas(modifier = modifier.size(size)) {
        val center = Offset(this.size.width / 2f, this.size.height / 2f)
        val radius = this.size.minDimension / 2f * scale
        
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    SphereGold.copy(alpha = alpha),
                    SphereGold.copy(alpha = alpha * 0.5f),
                    Color.Transparent
                ),
                center = center,
                radius = radius
            ),
            center = center,
            radius = radius
        )
    }
}

