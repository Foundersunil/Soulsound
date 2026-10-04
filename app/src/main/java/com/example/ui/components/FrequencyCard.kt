package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FrequencyItem
import com.example.ui.theme.BorderOrange
import com.example.ui.theme.DarkCard
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.OrangePrimary
import com.example.ui.theme.OrangeSecondary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSubtle

@Composable
fun FrequencyCard(
    frequency: FrequencyItem,
    isCurrentTrack: Boolean,
    isPlaying: Boolean,
    isFavorite: Boolean,
    onCardClick: () -> Unit,
    onPlayClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "card_pulse")
    val pulseRing by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseRing"
    )

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkCard
        ),
        border = BorderStroke(
            width = if (isCurrentTrack) 1.5.dp else 1.dp,
            color = if (isCurrentTrack) OrangePrimary else BorderOrange
        ),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onCardClick)
            .testTag("frequency_card_${frequency.id}")
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Top action bar (Premium badge + Favorite button)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (frequency.isPremium) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = GoldAccent.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "PREMIUM",
                            color = GoldAccent,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .background(
                                color = Color.White.copy(alpha = 0.06f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = frequency.category.uppercase(),
                            color = TextSubtle,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.8.sp
                        )
                    }
                }

                IconButton(
                    onClick = onFavoriteClick,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("favorite_btn_${frequency.id}")
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) OrangePrimary else TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Visually dominant circular resonance frequency zone
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(130.dp)
            ) {
                // Background subtle resonance rings
                Canvas(modifier = Modifier.size(130.dp)) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val baseRadius = 42.dp.toPx()

                    if (isCurrentTrack && isPlaying) {
                        drawCircle(
                            color = OrangePrimary.copy(alpha = 0.22f),
                            radius = baseRadius * pulseRing,
                            center = center,
                            style = Stroke(width = 1.5.dp.toPx())
                        )
                    }

                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                (if (isCurrentTrack) OrangePrimary else OrangeSecondary).copy(alpha = 0.2f),
                                Color.Transparent
                            ),
                            center = center,
                            radius = baseRadius * 1.3f
                        ),
                        radius = baseRadius * 1.3f,
                        center = center
                    )

                    drawCircle(
                        color = (if (isCurrentTrack) OrangePrimary else OrangeSecondary).copy(alpha = 0.5f),
                        radius = baseRadius,
                        center = center,
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                }

                // Frequency Number - Visually Dominant
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (frequency.hz % 1.0f == 0.0f) "${frequency.hz.toInt()}" else "${frequency.hz}",
                        color = TextPrimary,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "Hz",
                        color = OrangePrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Frequency Name
            Text(
                text = frequency.name,
                color = TextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Category tags: e.g. "Meditation • Relax • Balance"
            val tagsText = frequency.tags.take(3).joinToString(" • ")
            Text(
                text = tagsText,
                color = TextMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Play button with subtle glow
            Button(
                onClick = onPlayClick,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isCurrentTrack && isPlaying) OrangePrimary else Color(0xFF221710),
                    contentColor = if (isCurrentTrack && isPlaying) Color.Black else OrangePrimary
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (isCurrentTrack && isPlaying) OrangeSecondary else BorderOrange
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("play_btn_${frequency.id}")
            ) {
                Icon(
                    imageVector = if (isCurrentTrack && isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isCurrentTrack && isPlaying) "Pause" else "Play",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isCurrentTrack && isPlaying) "Pause" else "Play",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
