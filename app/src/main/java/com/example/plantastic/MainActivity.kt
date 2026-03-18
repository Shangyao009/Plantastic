package com.example.plantastic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.plantastic.data.SettingsRepository
import com.example.plantastic.data.UserPreferencesRepository
import com.example.plantastic.ui.navigation.NavigationGraph
import com.example.plantastic.ui.theme.PlantasticTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize settings repository
        SettingsRepository.init(this)
        UserPreferencesRepository.init(this)

        enableEdgeToEdge()
        setContent {
            PlantasticTheme {
                NavigationGraph()
            }
        }
    }
}
