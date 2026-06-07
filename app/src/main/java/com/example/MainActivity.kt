package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {
    
    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Supports full edge-to-edge transparent system bars natively
        enableEdgeToEdge()
        
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF020617) // dark background fallback
                ) {
                    val currentScreenState by viewModel.screenState.collectAsState()
                    
                    when (currentScreenState) {
                        GameScreenState.Dashboard -> MainDashboardScreen(viewModel)
                        GameScreenState.TeamSelection -> TeamSelectionScreen(viewModel)
                        GameScreenState.MatchPreparation -> MatchPreparationScreen(viewModel)
                        GameScreenState.Playing -> MatchPlayScreen(viewModel)
                        GameScreenState.Summary -> MatchSummaryScreen(viewModel)
                    }
                }
            }
        }
    }
}
