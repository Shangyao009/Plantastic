package com.example.plantastic.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.plantastic.data.model.Treatment
import com.example.plantastic.data.model.TreatmentType
import com.example.plantastic.ui.theme.GradientStart
import com.example.plantastic.ui.theme.TextSecondary

@Composable
fun TreatmentList(
    treatments: List<Treatment>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Treatment Recommendations",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            if (treatments.isNotEmpty()) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "\uD83D\uDCA1",
                    fontSize = 16.sp
                )
            }
        }

        if (treatments.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = GradientStart.copy(alpha = 0.1f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "\u2705",
                        fontSize = 24.sp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "No treatment needed!",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = GradientStart
                        )
                        Text(
                            text = "Your plant is healthy and thriving.",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        } else {
            treatments.forEach { treatment ->
                TreatmentItem(treatment = treatment)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun TreatmentItem(
    treatment: Treatment,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header row with type badge and name
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TreatmentTypeBadge(type = treatment.type)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = treatment.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Description
            Text(
                text = treatment.description,
                fontSize = 13.sp,
                color = TextSecondary,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Application and Frequency as separate rows for better readability
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Application",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )
                    Text(
                        text = treatment.applicationMethod,
                        fontSize = 12.sp,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 16.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "Frequency",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )
                    Text(
                        text = treatment.frequency,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun TreatmentTypeBadge(
    type: TreatmentType,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor, text, emoji) = when (type) {
        TreatmentType.CHEMICAL -> TreatmentTypeStyle(
            backgroundColor = Color(0xFFFF9800).copy(alpha = 0.15f),
            textColor = Color(0xFFE65100),
            text = "Chemical",
            emoji = "\u2692\uFE0F"
        )
        TreatmentType.ORGANIC -> TreatmentTypeStyle(
            backgroundColor = GradientStart.copy(alpha = 0.15f),
            textColor = GradientStart,
            text = "Organic",
            emoji = "\uD83C\uDF3F"
        )
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = emoji,
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            fontSize = 11.sp,
            color = textColor,
            fontWeight = FontWeight.Bold
        )
    }
}

private data class TreatmentTypeStyle(
    val backgroundColor: Color,
    val textColor: Color,
    val text: String,
    val emoji: String
)
