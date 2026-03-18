package com.example.plantastic.ui.screens

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
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
import android.media.ExifInterface
import java.io.File
import java.io.FileOutputStream
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

    // Square size as a fraction of screen width
    val displayMetrics = context.resources.displayMetrics
    val screenWidthPx = displayMetrics.widthPixels
    val squareSizePx = (screenWidthPx * 0.75f).toInt()

    Box(modifier = Modifier.fillMaxSize()) {
        // ── Camera preview ────────────────────────────────────────────────
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

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            imageCapture
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, ContextCompat.getMainExecutor(context))
            }
        )

        // ── Dark overlay with punched-out square ──────────────────────────
        //
        // The key fix: use drawIntoCanvas + saveLayer so that BlendMode.Clear
        // actually erases pixels from the overlay layer rather than painting
        // transparent on top of the camera feed (which does nothing visible).
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasW = size.width
            val canvasH = size.height
            val sqSize  = canvasW * 0.75f
            val left    = (canvasW - sqSize) / 2f
            val top     = (canvasH - sqSize) / 2f

            drawIntoCanvas { canvas ->
                // 1. Save a new layer covering the whole canvas
                canvas.nativeCanvas.saveLayer(
                    android.graphics.RectF(0f, 0f, canvasW, canvasH),
                    null
                )

                // 2. Draw the dark overlay across the entire canvas
                drawRect(color = Color.Black.copy(alpha = 0.55f))

                // 3. Punch a transparent hole in the centre using BlendMode.Clear
                drawRect(
                    color = Color.Black,        // colour is irrelevant with Clear
                    topLeft = Offset(left, top),
                    size    = Size(sqSize, sqSize),
                    blendMode = BlendMode.Clear
                )

                // 4. Restore the layer so it composites over the camera preview
                canvas.nativeCanvas.restore()
            }

            // ── Corner brackets ───────────────────────────────────────────
            val cornerLen   = sqSize * 0.12f
            val strokeWidth = 4.dp.toPx()
            val right  = left + sqSize
            val bottom = top  + sqSize

            listOf(
                // top-left
                Offset(left, top + cornerLen) to Offset(left, top),
                Offset(left, top) to Offset(left + cornerLen, top),
                // top-right
                Offset(right - cornerLen, top) to Offset(right, top),
                Offset(right, top) to Offset(right, top + cornerLen),
                // bottom-left
                Offset(left, bottom - cornerLen) to Offset(left, bottom),
                Offset(left, bottom) to Offset(left + cornerLen, bottom),
                // bottom-right
                Offset(right - cornerLen, bottom) to Offset(right, bottom),
                Offset(right, bottom - cornerLen) to Offset(right, bottom)
            ).forEach { (start, end) ->
                drawLine(
                    color       = Color.White,
                    start       = start,
                    end         = end,
                    strokeWidth = strokeWidth
                )
            }
        }

        // ── Back button ───────────────────────────────────────────────────
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

        // ── Instruction text ──────────────────────────────────────────────
        Text(
            text = "Scan your plant",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 140.dp)
        )

        // ── Shutter button ────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
        ) {
            IconButton(
                onClick = {
                    imageCapture?.let { capture ->
                        takePhoto(
                            context         = context,
                            imageCapture    = capture,
                            squareSizePx    = squareSizePx,
                            executor        = ContextCompat.getMainExecutor(context),
                            onImageCaptured = onImageCaptured,
                            onError         = { /* Handle error */ }
                        )
                    }
                },
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF4CAF50)), // Green shutter button
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "\uD83D\uDCF7", fontSize = 32.sp)
                    }
                }
            }
        }
    }
}

// ── Permission screens (unchanged) ────────────────────────────────────────────

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
        Text(text = "\uD83D\uDCF7", fontSize = 64.sp)
        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "Camera Permission Required", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "To scan plants, we need access to your camera to take photos of leaves.",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onRequestPermission,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
        ) { Text("Grant Permission") }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onNavigateBack) { Text("Go Back") }
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
        Text(text = "\uD83D\uDE1E", fontSize = 64.sp)
        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "Camera Permission Denied", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Please enable camera permission in your device settings to use this feature.",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onNavigateBack) { Text("Go Back") }
    }
}

// ── Photo capture + crop ──────────────────────────────────────────────────────

/**
 * Takes a full photo then crops it to the square region that matches the
 * viewfinder overlay, so [onImageCaptured] receives only the plant area.
 */
private fun takePhoto(
    context: Context,
    imageCapture: ImageCapture,
    squareSizePx: Int,          // screen-space side length of the viewfinder square
    executor: Executor,
    onImageCaptured: (Uri) -> Unit,
    onError: (ImageCaptureException) -> Unit
) {
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
        .format(System.currentTimeMillis())

    val photoFile = File(context.cacheDir, "${timestamp}_full.jpg")
    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    imageCapture.takePicture(
        outputOptions,
        executor,
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                // Crop the saved bitmap to only the square region
                val croppedUri = cropToSquare(
                    context      = context,
                    sourceFile   = photoFile,
                    squareSizePx = squareSizePx,
                    timestamp    = timestamp
                )
                onImageCaptured(croppedUri)
            }

            override fun onError(exception: ImageCaptureException) = onError(exception)
        }
    )
}

/**
 * Decodes [sourceFile], crops a centred square whose side length in the
 * image coordinates corresponds to [squareSizePx] on-screen, saves the
 * result and returns its [Uri].
 *
 * The viewfinder square occupies 75 % of the screen width and is centred,
 * so we apply the same ratio to the captured image dimensions.
 */
private fun cropToSquare(
    context: Context,
    sourceFile: File,
    squareSizePx: Int,
    timestamp: String
): Uri {
    val original = BitmapFactory.decodeFile(sourceFile.absolutePath)

    // Read EXIF orientation and apply rotation if needed
    val rotated = try {
        val exif = ExifInterface(sourceFile.absolutePath)
        val orientation = exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )
        val matrix = android.graphics.Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.preScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.preScale(1f, -1f)
            else -> null
        }?.let { m ->
            Bitmap.createBitmap(original, 0, 0, original.width, original.height, matrix, true)
        } ?: original
    } catch (e: Exception) {
        original
    }

    // The overlay square is 75 % of screen width, centred.
    // Map that fraction to the bitmap's shorter dimension.
    val bitmapMin = minOf(rotated.width, rotated.height)
    val cropSize  = (bitmapMin * 0.75f).toInt()

    // Centre the crop in the bitmap
    val cropX = (rotated.width  - cropSize) / 2
    val cropY = (rotated.height - cropSize) / 2

    val cropped = Bitmap.createBitmap(rotated, cropX, cropY, cropSize, cropSize)
    if (rotated != original) rotated.recycle()
    original.recycle()

    // Save the cropped image
    val croppedFile = File(context.cacheDir, "${timestamp}_cropped.jpg")
    FileOutputStream(croppedFile).use { out ->
        cropped.compress(Bitmap.CompressFormat.JPEG, 95, out)
    }
    cropped.recycle()

    return Uri.fromFile(croppedFile)
}