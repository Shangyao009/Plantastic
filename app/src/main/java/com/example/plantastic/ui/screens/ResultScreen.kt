package com.example.plantastic.ui.screens

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.plantastic.data.ScanHistoryRepository
import com.example.plantastic.data.model.Disease
import com.example.plantastic.data.model.ScanResult
import com.example.plantastic.domain.DiseaseDetector
import com.example.plantastic.domain.PlantAnalysisResult
import com.example.plantastic.ui.components.AffectedAreaOverlay
import com.example.plantastic.ui.components.DiseaseCard
import com.example.plantastic.ui.components.TreatmentList
import com.example.plantastic.ui.components.generateMockAffectedAreas
import com.example.plantastic.ui.theme.GradientEnd
import com.example.plantastic.ui.theme.GradientStart
import com.example.plantastic.ui.theme.HealthyGreen
import com.example.plantastic.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun ResultScreen(
    imageUri: Uri,
    onNavigateBack: () -> Unit,
    onGoHome: () -> Unit,
    onNavigateToChat: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var isLoading by remember { mutableStateOf(true) }
    var scanResult by remember { mutableStateOf<ScanResult?>(null) }
    var plantAnalysisResult by remember { mutableStateOf<PlantAnalysisResult?>(null) }
    var isNotPlant by remember { mutableStateOf(false) }
    var isCheckingPlant by remember { mutableStateOf(true) }
    val context = LocalContext.current
    var hasAnalyzed by remember { mutableStateOf(false) }

    LaunchedEffect(imageUri) {
        // Only analyze once — guard against re-triggering when navigating back from Chat
        if (hasAnalyzed) return@LaunchedEffect
        hasAnalyzed = true

        // First, detect if it's a plant and identify disease using LLM
        val result = DiseaseDetector.detectPlantAndDisease(context, imageUri)
        plantAnalysisResult = result

        if (!result.isPlant) {
            // Not a plant image
            isNotPlant = true
            isCheckingPlant = false
            isLoading = false
        } else {
            // It's a plant, proceed with the result
            isCheckingPlant = false
            delay(500) // Brief delay for UX

            val detectedDisease = result.disease ?: DiseaseDetector.detectDiseaseMock(imageUri)

            scanResult = ScanResult(
                id = UUID.randomUUID().toString(),
                imageUri = imageUri,
                timestamp = System.currentTimeMillis(),
                disease = detectedDisease
            )

            // Save to history
            scanResult?.let { ScanHistoryRepository.addScanResult(it) }

            isLoading = false
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when {
            isCheckingPlant || isLoading -> LoadingContent()
            isNotPlant -> NotPlantContent(
                plantDescription = plantAnalysisResult?.plantDescription,
                onGoBack = onNavigateBack,
                onScanAgain = onGoHome
            )
            scanResult != null -> ResultContent(
                scanResult = scanResult!!,
                plantType = plantAnalysisResult?.plantType,
                plantDescription = plantAnalysisResult?.plantDescription,
                onNavigateBack = onNavigateBack,
                onGoHome = onGoHome,
                onNavigateToChat = onNavigateToChat
            )
        }
    }
}

@Composable
private fun NotPlantContent(
    plantDescription: String?,
    onGoBack: () -> Unit,
    onScanAgain: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "\uD83D\uDE35",
            fontSize = 64.sp
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Not a Plant",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFF44336)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (plantDescription != null)
                "The image appears to show: $plantDescription\n\nPlease capture an image of a plant, leaf, flower, fruit, vegetable, mushroom, or other plant-related subject."
            else
                "The image doesn't appear to be a plant-related subject.\n\nPlease capture an image of a plant, leaf, flower, fruit, vegetable, mushroom, or other plant-related subject.",
            fontSize = 14.sp,
            color = TextSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onGoBack,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Try Again")
        }
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onScanAgain,
            colors = ButtonDefaults.buttonColors(containerColor = GradientStart),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Scan Another")
        }
    }
}

@Composable
private fun LoadingContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(GradientStart.copy(alpha = 0.15f), GradientEnd.copy(alpha = 0.15f))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = GradientStart,
                modifier = Modifier.size(60.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Analyzing Plant...",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Our AI is examining the leaf for signs of disease",
            fontSize = 14.sp,
            color = TextSecondary
        )
    }
}

@Composable
private fun ResultContent(
    scanResult: ScanResult,
    plantType: String?,
    plantDescription: String?,
    onNavigateBack: () -> Unit,
    onGoHome: () -> Unit,
    onNavigateToChat: ((String) -> Unit)?
) {
    val affectedAreas = if (scanResult.disease.name != "Healthy") {
        generateMockAffectedAreas()
    } else {
        emptyList()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Image with overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        ) {
            AffectedAreaOverlay(
                imageUri = scanResult.imageUri,
                affectedAreas = affectedAreas,
                modifier = Modifier.fillMaxSize()
            )

            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.5f))
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Plant Type Badge
            if (plantType != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                GradientStart.copy(alpha = 0.1f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = getPlantTypeEmoji(plantType) + " " + plantType.replaceFirstChar { it.uppercase() },
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF2E7D32)
                        )
                    }
                    if (plantDescription != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = plantDescription,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            // Disease Card
            DiseaseCard(disease = scanResult.disease)

            Spacer(modifier = Modifier.height(24.dp))

            // Treatment List
            TreatmentList(treatments = scanResult.disease.treatments)

            Spacer(modifier = Modifier.height(24.dp))

            // Chat Button
            if (onNavigateToChat != null) {
                ChatWithAiButton(
                    onClick = { onNavigateToChat(scanResult.id) }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Action Buttons
            Button(
                onClick = onGoHome,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GradientStart
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = "Scan Another Plant",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ScanResultDetailScreen(
    scanId: String,
    onNavigateBack: () -> Unit,
    onGoHome: () -> Unit,
    onNavigateToChat: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val scanResult = remember(scanId) {
        ScanHistoryRepository.getScanResultById(scanId)
    }

    if (scanResult == null) {
        // Scan not found - navigate back
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Scan not found",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onGoHome) {
                    Text("Go Home")
                }
            }
        }
        return
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Image with affected areas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                AsyncImage(
                    model = scanResult.imageUri,
                    contentDescription = "Plant image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Back button
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.TopStart)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Timestamp
                Text(
                    text = formatTimestamp(scanResult.timestamp),
                    fontSize = 14.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Disease Card
                DiseaseCard(disease = scanResult.disease)

                Spacer(modifier = Modifier.height(24.dp))

                // Treatment List
                TreatmentList(treatments = scanResult.disease.treatments)

                Spacer(modifier = Modifier.height(24.dp))

                // Chat Button
                if (onNavigateToChat != null) {
                    ChatWithAiButton(
                        onClick = { onNavigateToChat.invoke() }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Action Buttons
                Button(
                    onClick = onGoHome,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GradientStart
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = "Scan Another Plant",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

private fun getPlantTypeEmoji(plantType: String): String {
    return when (plantType.lowercase()) {
        "leaf" -> "\uD83C\uDF3F"
        "flower" -> "\uD83C\uDF38"
        "fruit" -> "\uD83C\uDF53"
        "vegetable" -> "\uD83E\uDD66"
        "root" -> "\uD83E\uDDC0"
        "stem" -> "\uD83C\uDF31"
        "bark" -> "\uD83C\uDF32"
        "seed" -> "\uD83C\uDF31"
        "mushroom" -> "\uD83C\uDF44"
        "fungi" -> "\uD83C\uDF44"
        "grass" -> "\uD83C\uDF3F"
        "succulent" -> "\uD83C\uDF3F"
        "other_plant" -> "\uD83C\uDF3F"
        else -> "\uD83C\uDF3F"
    }
}

@Composable
private fun ChatWithAiButton(onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(GradientStart, GradientEnd)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "\uD83D\uDCAC", fontSize = 22.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Chat with AI",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Ask questions about this plant",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }
            Text(text = "\u276F", fontSize = 20.sp, color = TextSecondary)
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = java.text.SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(timestamp))
}

@Composable
private fun ChatSection(
    imageUri: Uri,
    disease: Disease
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val messages = remember { mutableStateListOf<ChatMessageItem>() }
    var inputText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Color(0xFFF5F5F5),
                RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Text(
            text = "Ask about this plant",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Chat messages
        if (messages.isEmpty()) {
            Text(
                text = "Ask questions like: What treatments do you recommend? How can I prevent this disease?",
                fontSize = 14.sp,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages) { message ->
                    ChatBubble(message = message)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Input field
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Ask a question...") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (inputText.isNotBlank() && !isLoading) {
                            val question = inputText
                            inputText = ""
                            scope.launch {
                                isLoading = true
                                messages.add(ChatMessageItem(question, isUser = true))
                                val answer = DiseaseDetector.chatAboutPlant(
                                    context, imageUri, disease, question
                                )
                                messages.add(ChatMessageItem(answer, isUser = false))
                                isLoading = false
                            }
                        }
                    }
                ),
                singleLine = true,
                shape = RoundedCornerShape(24.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    if (inputText.isNotBlank() && !isLoading) {
                        scope.launch {
                            isLoading = true
                            messages.add(ChatMessageItem(inputText, isUser = true))
                            val answer = DiseaseDetector.chatAboutPlant(
                                context, imageUri, disease, inputText
                            )
                            messages.add(ChatMessageItem(answer, isUser = false))
                            inputText = ""
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .background(Brush.linearGradient(colors = listOf(GradientStart, GradientEnd)), CircleShape)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

private data class ChatMessageItem(val text: String, val isUser: Boolean)

@Composable
private fun ChatBubble(message: ChatMessageItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .background(
                    if (message.isUser) GradientStart else Color.White,
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (message.isUser) 16.dp else 4.dp,
                        bottomEnd = if (message.isUser) 4.dp else 16.dp
                    )
                )
                .padding(12.dp)
        ) {
            Text(
                text = message.text,
                fontSize = 14.sp,
                color = if (message.isUser) Color.White else Color.Black
            )
        }
    }
}
