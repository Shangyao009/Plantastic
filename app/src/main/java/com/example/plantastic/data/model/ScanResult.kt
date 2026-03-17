package com.example.plantastic.data.model

import android.net.Uri

data class ScanResult(
    val id: String,
    val imageUri: Uri,
    val timestamp: Long,
    val disease: Disease
)
