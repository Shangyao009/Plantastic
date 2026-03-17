package com.example.plantastic.domain

import android.net.Uri
import com.example.plantastic.data.model.Disease
import com.example.plantastic.data.PlantDatabase

object DiseaseDetector {

    /**
     * Mock disease detection for MVP.
     * In a production app, this would connect to an ML model or API.
     */
    fun detectDisease(imageUri: Uri): Disease {
        // Simulate random disease detection for demo purposes
        // In production, this would analyze the actual image
        val diseases = PlantDatabase.sampleDiseases

        // Simple random selection with weighted probability for demo
        val random = kotlin.random.Random.nextFloat()

        return when {
            random < 0.25f -> diseases.first { it.id == "powdery_mildew" }
            random < 0.5f -> diseases.first { it.id == "leaf_spot" }
            random < 0.75f -> diseases.first { it.id == "rust" }
            else -> diseases.first { it.id == "healthy" }
        }
    }

    /**
     * Returns a list of detected diseases with their confidence scores.
     * For MVP, returns the top result only.
     */
    fun detectMultipleDiseases(imageUri: Uri): List<Disease> {
        val primaryDisease = detectDisease(imageUri)
        return listOf(primaryDisease)
    }
}
