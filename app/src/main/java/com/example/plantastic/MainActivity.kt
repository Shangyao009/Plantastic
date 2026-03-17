package com.example.plantastic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.plantastic.ui.navigation.NavigationGraph
import com.example.plantastic.ui.theme.PlantasticTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PlantasticTheme {
                NavigationGraph()
            }
        }
    }
}
