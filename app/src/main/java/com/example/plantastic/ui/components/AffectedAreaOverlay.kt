package com.example.plantastic.ui.components

import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSize
import coil.compose.AsyncImage
import kotlin.random.Random

data class AffectedArea(
    val centerX: Float,
    val centerY: Float,
    val radius: Float
)

@Composable
fun AffectedAreaOverlay(
    imageUri: Uri,
    affectedAreas: List<AffectedArea>,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        AsyncImage(
            model = imageUri,
            contentDescription = "Plant image",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        if (affectedAreas.isNotEmpty()) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .onSize { size ->
                        // This ensures the canvas is properly sized
                    }
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height

                affectedAreas.forEach { area ->
                    // Scale the normalized coordinates to actual canvas size
                    val centerX = area.centerX * canvasWidth
                    val centerY = area.centerY * canvasHeight
                    val radius = area.radius * minOf(canvasWidth, canvasHeight)

                    // Draw semi-transparent overlay
                    drawCircle(
                        color = Color(0x80FF0000),
                        radius = radius,
                        center = Offset(centerX, centerY),
                        blendMode = BlendMode.Multiply
                    )

                    // Draw border
                    drawCircle(
                        color = Color(0xFFFF0000),
                        radius = radius,
                        center = Offset(centerX, centerY),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
                    )
                }
            }
        }
    }
}

@Composable
fun generateMockAffectedAreas(): List<AffectedArea> {
    return listOf(
        AffectedArea(
            centerX = 0.3f + Random.nextFloat() * 0.4f,
            centerY = 0.3f + Random.nextFloat() * 0.4f,
            radius = 0.1f + Random.nextFloat() * 0.15f
        ),
        AffectedArea(
            centerX = 0.2f + Random.nextFloat() * 0.6f,
            centerY = 0.2f + Random.nextFloat() * 0.6f,
            radius = 0.08f + Random.nextFloat() * 0.1f
        )
    )
}
