package com.example.voltduration

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.voltduration.ui.navigation.AppNavHost
import com.example.voltduration.ui.theme.VoltDurationTheme
import com.example.voltduration.ui.viewmodel.VoltDurationViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: VoltDurationViewModel = viewModel()
            val settings by viewModel.settings.collectAsState()

            VoltDurationTheme(
                themeMode = settings.themeMode,
                dynamicColor = settings.dynamicColor
            ) {
                AppNavHost(viewModel = viewModel)
            }
        }
    }
}