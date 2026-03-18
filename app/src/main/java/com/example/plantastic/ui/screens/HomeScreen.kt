package com.example.plantastic.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.plantastic.data.ScanHistoryRepository
import com.example.plantastic.data.model.ScanResult
import com.example.plantastic.ui.components.CameraButton
import com.example.plantastic.ui.components.PlantasticTopBar
import com.example.plantastic.ui.theme.DiseaseRed
import com.example.plantastic.ui.theme.GradientEnd
import com.example.plantastic.ui.theme.GradientStart
import com.example.plantastic.ui.theme.HealthyGreen
import com.example.plantastic.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    onScanClick: () -> Unit,
    onHistoryItemClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val recentScans = ScanHistoryRepository.getScanResults().take(5)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        PlantasticTopBar()

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))

                // Hero Section
                HeroSection(onScanClick = onScanClick)
            }

            if (recentScans.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "Recent Scans",
                        emoji = "\uD83D\uDCC5"
                    )
                }

                items(recentScans) { scan ->
                    RecentScanItem(
                        scanResult = scan,
                        onClick = { onHistoryItemClick(scan.id) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            } else {
                item {
                    EmptyStateCard()
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun HeroSection(onScanClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(GradientStart, GradientEnd)
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "\uD83C\uDF3F",
                    fontSize = 56.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Plant Health Scanner",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "AI-powered plant disease detection",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.85f)
                )

                Spacer(modifier = Modifier.height(20.dp))

                CameraButton(onClick = onScanClick)
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    emoji: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = emoji,
            fontSize = 20.sp
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun RecentScanItem(
    scanResult: ScanResult,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isHealthy = scanResult.disease.name == "Healthy"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Plant thumbnail with status indicator
            Box {
                AsyncImage(
                    model = scanResult.imageUri,
                    contentDescription = "Plant thumbnail",
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )

                // Status badge
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(if (isHealthy) HealthyGreen else DiseaseRed),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isHealthy) "\u2705" else "\uD83D\uDCA5",
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = scanResult.disease.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = formatTimestamp(scanResult.timestamp),
                    fontSize = 12.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Confidence bar
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${(scanResult.disease.confidence * 100).toInt()}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isHealthy) HealthyGreen else DiseaseRed
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color.LightGray.copy(alpha = 0.3f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(scanResult.disease.confidence)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(if (isHealthy) HealthyGreen else DiseaseRed)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyStateCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "\uD83D\uDCF7",
                fontSize = 64.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Ready to scan?",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Take a photo of any plant leaf, flower, fruit, or mushroom to detect diseases",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60 * 1000 -> "Just now"
        diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)} min ago"
        diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)} hours ago"
        diff < 7 * 24 * 60 * 60 * 1000 -> "${diff / (24 * 60 * 60 * 1000)} days ago"
        else -> SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(timestamp))
    }
}
