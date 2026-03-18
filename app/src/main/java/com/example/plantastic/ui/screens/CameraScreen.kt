package com.example.plantastic.ui.screens

import android.Manifest
import android.content.Context
import android.net.Uri
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.Executor
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen(
    onImageCaptured: (Uri) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        when {
            cameraPermissionState.status.isGranted -> {
                CameraContent(
                    onImageCaptured = onImageCaptured,
                    onNavigateBack = onNavigateBack
                )
            }
            cameraPermissionState.status.shouldShowRationale -> {
                PermissionRationale(
                    onRequestPermission = { cameraPermissionState.launchPermissionRequest() },
                    onNavigateBack = onNavigateBack
                )
            }
            else -> {
                PermissionDenied(onNavigateBack = onNavigateBack)
            }
        }
    }
}

@Composable
private fun CameraContent(
    onImageCaptured: (Uri) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { previewView ->
                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()

                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    imageCapture = ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build()

                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageCapture
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, ContextCompat.getMainExecutor(context))
            }
        )

        // Back button
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.5f))
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }

        // Camera frame - dark overlay with clear center square and visible border
        val screenWidth = LocalContext.current.resources.displayMetrics.widthPixels
        val squareSize = (screenWidth * 0.75f).dp

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val centerX = canvasWidth / 2
                val centerY = canvasHeight / 2
                val squareSizePx = squareSize.toPx()
                val halfSquare = squareSizePx / 2
                val cornerLength = squareSizePx * 0.15f
                val strokeWidth = 4.dp.toPx()

                // Draw semi-transparent dark overlay
                drawRect(
                    color = Color.Black.copy(alpha = 0.5f),
                    size = Size(canvasWidth, canvasHeight)
                )

                // Clear the center square area (make it transparent to show camera)
                drawRect(
                    color = Color.Transparent,
                    topLeft = Offset(centerX - halfSquare, centerY - halfSquare),
                    size = Size(squareSizePx, squareSizePx)
                )

                // Draw corner brackets (viewfinder style)
                val cornerColor = Color.White

                // Top-left corner
                drawLine(
                    color = cornerColor,
                    start = Offset(centerX - halfSquare, centerY - halfSquare + cornerLength),
                    end = Offset(centerX - halfSquare, centerY - halfSquare),
                    strokeWidth = strokeWidth
                )
                drawLine(
                    color = cornerColor,
                    start = Offset(centerX - halfSquare, centerY - halfSquare),
                    end = Offset(centerX - halfSquare + cornerLength, centerY - halfSquare),
                    strokeWidth = strokeWidth
                )

                // Top-right corner
                drawLine(
                    color = cornerColor,
                    start = Offset(centerX + halfSquare - cornerLength, centerY - halfSquare),
                    end = Offset(centerX + halfSquare, centerY - halfSquare),
                    strokeWidth = strokeWidth
                )
                drawLine(
                    color = cornerColor,
                    start = Offset(centerX + halfSquare, centerY - halfSquare),
                    end = Offset(centerX + halfSquare, centerY - halfSquare + cornerLength),
                    strokeWidth = strokeWidth
                )

                // Bottom-left corner
                drawLine(
                    color = cornerColor,
                    start = Offset(centerX - halfSquare, centerY + halfSquare - cornerLength),
                    end = Offset(centerX - halfSquare, centerY + halfSquare),
                    strokeWidth = strokeWidth
                )
                drawLine(
                    color = cornerColor,
                    start = Offset(centerX - halfSquare, centerY + halfSquare),
                    end = Offset(centerX - halfSquare + cornerLength, centerY + halfSquare),
                    strokeWidth = strokeWidth
                )

                // Bottom-right corner
                drawLine(
                    color = cornerColor,
                    start = Offset(centerX + halfSquare - cornerLength, centerY + halfSquare),
                    end = Offset(centerX + halfSquare, centerY + halfSquare),
                    strokeWidth = strokeWidth
                )
                drawLine(
                    color = cornerColor,
                    start = Offset(centerX + halfSquare, centerY + halfSquare - cornerLength),
                    end = Offset(centerX + halfSquare, centerY + halfSquare),
                    strokeWidth = strokeWidth
                )
            }
        }

        // Capture button
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
        ) {
            IconButton(
                onClick = {
                    imageCapture?.let { capture ->
                        takePhoto(
                            context = context,
                            imageCapture = capture,
                            executor = ContextCompat.getMainExecutor(context),
                            onImageCaptured = onImageCaptured,
                            onError = { /* Handle error */ }
                        )
                    }
                },
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color.Transparent)
            ) {
                // White border ring
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    // Inner green circle
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF4CAF50)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "\uD83D\uDCF7",
                            fontSize = 32.sp
                        )
                    }
                }
            }
        }

        // Instruction text at bottom
        Text(
            text = "Scan your plant",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 140.dp))
    }
}

@Composable
private fun PermissionRationale(
    onRequestPermission: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "\uD83D\uDCF7",
            fontSize = 64.sp
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Camera Permission Required",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "To scan plants, we need access to your camera to take photos of leaves.",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onRequestPermission,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4CAF50)
            )
        ) {
            Text("Grant Permission")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onNavigateBack) {
            Text("Go Back")
        }
    }
}

@Composable
private fun PermissionDenied(onNavigateBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "\uD83D\uDE1E",
            fontSize = 64.sp
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Camera Permission Denied",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Please enable camera permission in your device settings to use this feature.",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onNavigateBack) {
            Text("Go Back")
        }
    }
}

private fun takePhoto(
    context: Context,
    imageCapture: ImageCapture,
    executor: Executor,
    onImageCaptured: (Uri) -> Unit,
    onError: (ImageCaptureException) -> Unit
) {
    val photoFile = File(
        context.cacheDir,
        SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
            .format(System.currentTimeMillis()) + ".jpg"
    )

    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    imageCapture.takePicture(
        outputOptions,
        executor,
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                val savedUri = Uri.fromFile(photoFile)
                onImageCaptured(savedUri)
            }

            override fun onError(exception: ImageCaptureException) {
                onError(exception)
            }
        }
    )
}
