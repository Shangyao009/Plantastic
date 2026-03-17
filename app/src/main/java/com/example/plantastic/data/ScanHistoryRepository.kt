package com.example.plantastic.data

import android.net.Uri
import com.example.plantastic.data.model.Disease
import com.example.plantastic.data.model.ScanResult
import com.example.plantastic.data.model.Treatment
import com.example.plantastic.data.model.TreatmentType
import java.util.UUID

object ScanHistoryRepository {

    private val scanResults = mutableListOf<ScanResult>()

    fun addScanResult(result: ScanResult) {
        scanResults.add(0, result)
    }

    fun getScanResults(): List<ScanResult> = scanResults.toList()

    fun getScanResultById(id: String): ScanResult? {
        return scanResults.find { it.id == id }
    }

    fun clearHistory() {
        scanResults.clear()
    }
}

object PlantDatabase {

    val sampleDiseases = listOf(
        Disease(
            id = "powdery_mildew",
            name = "Powdery Mildew",
            description = "A fungal disease that affects a wide range of plants. It appears as white powdery spots on leaves and stems.",
            confidence = 0.98f,
            affectedAreaPercent = 35f,
            treatments = listOf(
                Treatment(
                    id = "chem_1",
                    name = "Neem Oil Spray",
                    description = "Natural oil that disrupts fungal growth",
                    type = TreatmentType.ORGANIC,
                    applicationMethod = "Spray directly on affected areas",
                    frequency = "Every 7-14 days"
                ),
                Treatment(
                    id = "org_1",
                    name = "Baking Soda Solution",
                    description = "Alkaline solution that inhibits fungal growth",
                    type = TreatmentType.ORGANIC,
                    applicationMethod = "Mix 1 tbsp baking soda with 1 gallon water",
                    frequency = "Weekly until resolved"
                ),
                Treatment(
                    id = "chem_2",
                    name = "Sulfur Fungicide",
                    description = "Effective chemical treatment for powdery mildew",
                    type = TreatmentType.CHEMICAL,
                    applicationMethod = "Spray thoroughly on all plant surfaces",
                    frequency = "Every 7 days as needed"
                )
            )
        ),
        Disease(
            id = "leaf_spot",
            name = "Leaf Spot",
            description = "Bacterial or fungal infection causing brown or black spots on leaves.",
            confidence = 0.95f,
            affectedAreaPercent = 25f,
            treatments = listOf(
                Treatment(
                    id = "chem_3",
                    name = "Copper Fungicide",
                    description = "Broad-spectrum fungicide for leaf spot",
                    type = TreatmentType.CHEMICAL,
                    applicationMethod = "Spray on affected leaves",
                    frequency = "Every 7-10 days"
                ),
                Treatment(
                    id = "org_2",
                    name = "Remove Infected Leaves",
                    description = "Physically remove affected foliage",
                    type = TreatmentType.ORGANIC,
                    applicationMethod = "Prune and dispose of infected leaves",
                    frequency = "As needed"
                )
            )
        ),
        Disease(
            id = "rust",
            name = "Rust Disease",
            description = "Fungal disease causing orange or rust-colored pustules on leaves.",
            confidence = 0.92f,
            affectedAreaPercent = 40f,
            treatments = listOf(
                Treatment(
                    id = "chem_4",
                    name = "Myclobutanil",
                    description = "Systemic fungicide for rust diseases",
                    type = TreatmentType.CHEMICAL,
                    applicationMethod = "Spray thoroughly",
                    frequency = "Every 14 days"
                ),
                Treatment(
                    id = "org_3",
                    name = "Milk Spray",
                    description = "Diluted milk solution has antifungal properties",
                    type = TreatmentType.ORGANIC,
                    applicationMethod = "Mix 1 part milk with 9 parts water",
                    frequency = "Weekly"
                )
            )
        ),
        Disease(
            id = "healthy",
            name = "Healthy",
            description = "No disease detected. Your plant looks healthy!",
            confidence = 0.99f,
            affectedAreaPercent = 0f,
            treatments = emptyList()
        )
    )
}
