package com.example.plantastic.data.model

data class Disease(
    val id: String,
    val name: String,
    val description: String,
    val confidence: Float,
    val affectedAreaPercent: Float,
    val treatments: List<Treatment>
)
