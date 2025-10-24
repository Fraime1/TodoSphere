package com.sofutil.todosw.ui.screens.profile

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sofutil.todosw.data.TaskStats
import com.sofutil.todosw.ui.theme.*
import com.sofutil.todosw.viewmodel.TaskViewModel
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: TaskViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToAbout: () -> Unit
) {
    val context = LocalContext.current
    val stats by viewModel.stats.collectAsState()
    val currentTheme by viewModel.theme.collectAsState()
    var showThemeDialog by remember { mutableStateOf(false) }
    
    // Theme Selection Dialog
    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentTheme = currentTheme,
            onDismiss = { showThemeDialog = false },
            onThemeSelected = { theme ->
                viewModel.setTheme(theme)
                showThemeDialog = false
            }
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Profile",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Avatar with orbits
            ProfileAvatar()
            
            // User stats
            ProfileStatsCard(stats = stats)
            
            // Settings section
            Text(
                text = "Settings",
                style = MaterialTheme.typography.titleLarge
            )
            
            SettingsSection(
                currentTheme = currentTheme,
                onThemeClick = { showThemeDialog = true },
                onAboutClick = onNavigateToAbout,
                onPolicyClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://todosphhere.com/privacy-policy.html"))
                    context.startActivity(intent)
                }
            )
        }
    }
}

@Composable
fun ProfileAvatar(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "orbit_rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
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
        Canvas(modifier = Modifier.size(180.dp)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val avatarRadius = 40f
            
            // Draw center sphere (avatar)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(SpherePink, SpherePurple),
                    center = center,
                    radius = avatarRadius
                ),
                center = center,
                radius = avatarRadius
            )
            
            // Draw orbits
            listOf(60f, 80f).forEach { orbitRadius ->
                drawCircle(
                    color = OrbitGlow,
                    radius = orbitRadius,
                    center = center,
                    style = Stroke(width = 1.5f),
                    alpha = 0.3f
                )
            }
            
            // Draw orbiting spheres
            val angleRad = Math.toRadians(rotation.toDouble())
            listOf(
                Triple(60f, 8f, SpherePink),
                Triple(80f, 6f, SpherePurple),
                Triple(60f, 8f, SphereGold)
            ).forEachIndexed { index, (orbitRadius, sphereRadius, color) ->
                val angle = angleRad + (index * 2 * PI / 3)
                val x = center.x + (orbitRadius * cos(angle)).toFloat()
                val y = center.y + (orbitRadius * sin(angle)).toFloat()
                
                drawCircle(
                    color = color,
                    radius = sphereRadius,
                    center = Offset(x, y)
                )
            }
        }
    }
}

@Composable
fun ProfileStatsCard(
    stats: TaskStats,
    modifier: Modifier = Modifier
) {
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
            Text(
                text = "Your TodoSphere",
                style = MaterialTheme.typography.titleLarge
            )
            
            Divider(color = MaterialTheme.colorScheme.outline)
            
            ProfileStatRow(
                label = "Total Tasks Created",
                value = stats.totalTasks.toString()
            )
            
            ProfileStatRow(
                label = "Tasks Completed",
                value = stats.completedTasks.toString()
            )
            
            ProfileStatRow(
                label = "Active Tasks",
                value = stats.activeTasks.toString()
            )
            
            if (stats.totalTasks > 0) {
                val avgDaily = stats.completedTasks / 7 // Simple calculation
                ProfileStatRow(
                    label = "Average Daily Tasks",
                    value = avgDaily.toString()
                )
            }
        }
    }
}

@Composable
fun ProfileStatRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun SettingsSection(
    currentTheme: String,
    onThemeClick: () -> Unit,
    onAboutClick: () -> Unit,
    onPolicyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Theme selector
        SettingsCard(
            title = "Theme",
            subtitle = currentTheme,
            onClick = onThemeClick
        )
        
        // About section
        SettingsCard(
            title = "About TodoSphere",
            subtitle = "Version 1.0",
            onClick = onAboutClick
        )

        SettingsCard(
            title = "Policy",
            subtitle = "Tap to view",
            onClick = onPolicyClick
        )
    }
}

@Composable
fun SettingsCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Navigate",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun ThemeSelectionDialog(
    currentTheme: String,
    onDismiss: () -> Unit,
    onThemeSelected: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Select Theme",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ThemeOption(
                    name = "Dark",
                    isSelected = currentTheme == "Dark",
                    onClick = { onThemeSelected("Dark") }
                )
                ThemeOption(
                    name = "Light",
                    isSelected = currentTheme == "Light",
                    onClick = { onThemeSelected("Light") }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ThemeOption(
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isSelected) 
                    MaterialTheme.colorScheme.onPrimaryContainer 
                else 
                    MaterialTheme.colorScheme.onSurface
            )
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

