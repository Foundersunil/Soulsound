package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FrequencyItem
import com.example.ui.theme.BorderOrange
import com.example.ui.theme.DarkCardElevated
import com.example.ui.theme.OrangePrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@Composable
fun MiniPlayer(
    currentTrack: FrequencyItem?,
    isPlaying: Boolean,
    progressMs: Long,
    totalDurationMs: Long,
    isFavorite: Boolean,
    amplitude: Float,
    onPlayerClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = currentTrack != null,
        enter = slideInVertically { it } + fadeIn(),
        exit = slideOutVertically { it } + fadeOut(),
        modifier = modifier
    ) {
        if (currentTrack == null) return@AnimatedVisibility

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = DarkCardElevated
            ),
            border = BorderStroke(1.dp, BorderOrange),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp)
                .clip(RoundedCornerShape(16.dp))
                .clickable(onClick = onPlayerClick)
                .testTag("mini_player")
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Small circular resonance badge
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color(0xFF241508), shape = CircleShape)
                    ) {
                        Text(
                            text = if (currentTrack.hz % 1.0f == 0.0f) "${currentTrack.hz.toInt()}" else "${currentTrack.hz}",
                            color = OrangePrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Track title and category/frequency
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = currentTrack.name,
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${currentTrack.displayHz} • ${currentTrack.category}",
                                color = TextMuted,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                            if (isPlaying) {
                                Spacer(modifier = Modifier.width(6.dp))
                                MiniEqualizer(amplitude = amplitude)
                            }
                        }
                    }

                    // Favorite button
                    IconButton(
                        onClick = onFavoriteClick,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) OrangePrimary else TextMuted,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    // Play/Pause button
                    IconButton(
                        onClick = onPlayPauseClick,
                        modifier = Modifier
                            .size(40.dp)
                            .background(OrangePrimary, CircleShape)
                            .testTag("mini_player_play_pause")
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.Black,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // Progress Bar
                val progressFrac = if (totalDurationMs > 0 && totalDurationMs != Long.MAX_VALUE) {
                    (progressMs.toFloat() / totalDurationMs.toFloat()).coerceIn(0f, 1f)
                } else 0f

                LinearProgressIndicator(
                    progress = { progressFrac },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.5.dp),
                    color = OrangePrimary,
                    trackColor = Color(0x33FF6A00)
                )
            }
        }
    }
}

@Composable
private fun MiniEqualizer(amplitude: Float) {
    val infiniteTransition = rememberInfiniteTransition(label = "eq")
    val bar1 by infiniteTransition.animateFloat(
        initialValue = 3f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(tween(400, easing = LinearEasing), RepeatMode.Reverse),
        label = "bar1"
    )
    val bar2 by infiniteTransition.animateFloat(
        initialValue = 8f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(tween(320, easing = LinearEasing), RepeatMode.Reverse),
        label = "bar2"
    )
    val bar3 by infiniteTransition.animateFloat(
        initialValue = 4f,
        targetValue = 11f,
        animationSpec = infiniteRepeatable(tween(480, easing = LinearEasing), RepeatMode.Reverse),
        label = "bar3"
    )

    Row(
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        modifier = Modifier.height(12.dp)
    ) {
        val factor = (amplitude * 1.5f).coerceIn(0.5f, 1.2f)
        Box(
            modifier = Modifier
                .width(2.dp)
                .height((bar1 * factor).dp.coerceAtLeast(2.dp))
                .background(OrangePrimary, CircleShape)
        )
        Box(
            modifier = Modifier
                .width(2.dp)
                .height((bar2 * factor).dp.coerceAtLeast(2.dp))
                .background(OrangePrimary, CircleShape)
        )
        Box(
            modifier = Modifier
                .width(2.dp)
                .height((bar3 * factor).dp.coerceAtLeast(2.dp))
                .background(OrangePrimary, CircleShape)
        )
    }
}
