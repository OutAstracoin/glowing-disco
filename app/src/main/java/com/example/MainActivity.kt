package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.PoserScreen
import com.example.ui.theme.MagicPoserTheme
import com.example.viewmodel.PoserViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MagicPoserTheme {
                val poserViewModel: PoserViewModel = viewModel()
                val uiState by poserViewModel.uiState.collectAsStateWithLifecycle()
                PoserScreen(
                    uiState = uiState,
                    viewModel = poserViewModel
                )
            }
        }
    }
}
