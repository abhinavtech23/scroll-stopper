package com.feedshield.android.presentation.analytics.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.feedshield.android.data.model.DailyStats
import com.feedshield.android.presentation.theme.*

@Composable
fun AppBreakdownCard(
    weeklyStats: List<DailyStats>,
    modifier: Modifier = Modifier
) {
    val totalIg = weeklyStats.sumOf { it.instagramBlocks }
    val totalYt = weeklyStats.sumOf { it.youtubeBlocks }
    val total = (totalIg + totalYt).coerceAtLeast(1)

    val igPercent = (totalIg.toFloat() / total.toFloat() * 100).toInt()
    val ytPercent = (totalYt.toFloat() / total.toFloat() * 100).toInt()

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBlack),
        border = BorderStroke(1.dp, BorderSubtle)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Platform Breakdown",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = PureWhite
            )

            // Instagram Row
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Instagram Reels",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }
                    Text(
                        text = "$totalIg blocks ($igPercent%)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )
                }

                LinearProgressIndicator(
                    progress = { if (totalIg + totalYt == 0) 0f else (totalIg.toFloat() / total.toFloat()) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    color = PureWhite,
                    trackColor = SurfaceElevated,
                )
            }

            // YouTube Row
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.PlayCircle,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "YouTube Shorts",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }
                    Text(
                        text = "$totalYt blocks ($ytPercent%)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )
                }

                LinearProgressIndicator(
                    progress = { if (totalIg + totalYt == 0) 0f else (totalYt.toFloat() / total.toFloat()) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    color = TextMuted,
                    trackColor = SurfaceElevated,
                )
            }
        }
    }
}
