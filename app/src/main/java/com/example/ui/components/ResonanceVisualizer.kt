package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.OrangePrimary
import com.example.ui.theme.OrangeSecondary
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun ResonanceVisualizer(
    modifier: Modifier = Modifier,
    sizeDp: Dp = 260.dp,
    isPlaying: Boolean = true,
    amplitude: Float = 0.35f,
    content: @Composable () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "resonance")
    
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isPlaying) 2800 else 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse"
    )

    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isPlaying) 2200 else 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "waveOffset"
    )

    Box(
        modifier = modifier.size(sizeDp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val maxRadius = (size.minDimension / 2f) * 0.95f
            val baseRadius = maxRadius * 0.35f
            val ampFactor = if (isPlaying) (amplitude.coerceIn(0.1f, 1f)) else 0.08f

            // Outer resonance rings
            val ringCount = 4
            for (i in 0 until ringCount) {
                val phase = (pulse + (i.toFloat() / ringCount)) % 1f
                val currentRadius = baseRadius + (maxRadius - baseRadius) * phase
                val alpha = ((1f - phase) * 0.65f * (0.4f + ampFactor * 0.6f)).coerceIn(0f, 0.9f)
                
                val ringColor = if (i % 2 == 0) OrangePrimary else GoldAccent

                drawCircle(
                    color = ringColor.copy(alpha = alpha),
                    radius = currentRadius,
                    center = center,
                    style = Stroke(width = (1.5f + (1f - phase) * 2f).dp.toPx())
                )
            }

            // Central radiant glow circle
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        OrangePrimary.copy(alpha = 0.35f + ampFactor * 0.35f),
                        OrangeSecondary.copy(alpha = 0.12f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = baseRadius * 1.5f
                ),
                radius = baseRadius * 1.5f,
                center = center
            )

            // Inner harmonic frequency boundary
            drawCircle(
                color = OrangePrimary.copy(alpha = 0.85f),
                radius = baseRadius,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )

            // Subtle sine wave line across the center
            val wavePath = Path()
            val waveWidth = baseRadius * 1.6f
            val startX = center.x - waveWidth / 2f
            val wavePoints = 40
            val waveHeight = 12.dp.toPx() * (if (isPlaying) ampFactor else 0.2f)

            for (p in 0..wavePoints) {
                val progress = p.toFloat() / wavePoints
                val x = startX + progress * waveWidth
                val y = center.y + sin(progress * 4f * PI.toFloat() + waveOffset) * waveHeight
                if (p == 0) wavePath.moveTo(x, y) else wavePath.lineTo(x, y)
            }

            drawPath(
                path = wavePath,
                color = GoldAccent.copy(alpha = 0.7f),
                style = Stroke(width = 2.dp.toPx())
            )
        }

        // Center overlay slot (e.g. Frequency number text)
        content()
    }
}
