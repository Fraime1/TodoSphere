package com.sofutil.todosw

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.sofutil.todosw.navigation.NavGraph
import com.sofutil.todosw.ui.theme.TodoSphereTheme
import com.sofutil.todosw.viewmodel.TaskViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: TaskViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge display
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        setContent {
            TodoSphereApp(viewModel = viewModel)
        }
    }
}

@Composable
fun TodoSphereApp(viewModel: TaskViewModel) {
    val navController = rememberNavController()
    val currentTheme by viewModel.theme.collectAsState()
    
    TodoSphereTheme(darkTheme = currentTheme == "Dark") {
        NavGraph(
            navController = navController,
            viewModel = viewModel
        )
    }
}
