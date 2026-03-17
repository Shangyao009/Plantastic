package com.example.plantastic.data.model

data class Treatment(
    val id: String,
    val name: String,
    val description: String,
    val type: TreatmentType,
    val applicationMethod: String,
    val frequency: String
)

enum class TreatmentType {
    CHEMICAL,
    ORGANIC
}
