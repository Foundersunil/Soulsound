package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
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
import com.example.ui.theme.DarkBg
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.OrangePrimary
import com.example.ui.theme.OrangeSecondary
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Premium sound-frequency resonance visualization.
 *
 * Provides a multi-layered harmonic animation:
 * - Layer 1: Static outer delicate ring with orbital cardinal reference points
 * - Layer 2: Subtle pulsing resonance wave ring expanding outward
 * - Layer 3: Secondary soft harmonic wave ring with 180° phase offset
 * - Layer 4: Luminous frequency nodes traveling gently along the outer orbital field
 * - Layer 5: Subtle circular oscillating frequency waveform in the outer field
 * - Hero Core: Central frequency circle with dark pristine background and warm gold/orange rim.
 *             Never has lines crossing through the numbers, ensuring perfect clarity.
 */
@Composable
fun ResonanceVisualizer(
    modifier: Modifier = Modifier,
    sizeDp: Dp = 270.dp,
    isPlaying: Boolean = true,
    amplitude: Float = 0.35f,
    content: @Composable () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "resonance")

    // Slow, luxurious expansion pulse
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isPlaying) 3400 else 6800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse"
    )

    // Secondary pulse offset by half a cycle for continuous breathing
    val pulseSecondary = (pulse + 0.5f) % 1f

    // Wave / orbital motion offset
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isPlaying) 4200 else 14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "waveOffset"
    )

    // Gentle breathing glow factor
    val glowBreath by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isPlaying) 1800 else 3600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowBreath"
    )

    Box(
        modifier = modifier.size(sizeDp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(sizeDp)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val maxRadius = (size.minDimension / 2f) * 0.94f
            // Central core radius - sized with generous breathing room
            val coreRadius = maxRadius * 0.54f
            val ampFactor = if (isPlaying) amplitude.coerceIn(0.15f, 0.95f) else 0.05f
            val isPaused = !isPlaying

            // -------------------------------------------------------------
            // LAYER 1: Static Outer Delicate Ring with Orbital Reference Points
            // -------------------------------------------------------------
            drawCircle(
                color = Color.White.copy(alpha = if (isPaused) 0.06f else 0.12f),
                radius = maxRadius,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )
            for (i in 0 until 4) {
                val angle = i * (PI / 2.0)
                val dotX = center.x + (maxRadius * cos(angle)).toFloat()
                val dotY = center.y + (maxRadius * sin(angle)).toFloat()
                drawCircle(
                    color = OrangePrimary.copy(alpha = if (isPaused) 0.15f else 0.35f),
                    radius = 1.8.dp.toPx(),
                    center = Offset(dotX, dotY)
                )
            }

            // -------------------------------------------------------------
            // LAYER 2: Primary Expanding Resonance Wave Ring
            // -------------------------------------------------------------
            val ring1Radius = coreRadius + (maxRadius - coreRadius) * pulse
            val ring1Alpha = ((1f - pulse) * (if (isPaused) 0.14f else 0.45f) * (0.5f + ampFactor * 0.5f)).coerceIn(0f, 0.75f)
            drawCircle(
                color = OrangePrimary.copy(alpha = ring1Alpha),
                radius = ring1Radius,
                center = center,
                style = Stroke(width = (1.5f + (1f - pulse) * 1.5f).dp.toPx())
            )

            // -------------------------------------------------------------
            // LAYER 3: Secondary Soft Resonance Wave Ring (Harmonic Phase)
            // -------------------------------------------------------------
            val ring2Radius = coreRadius + (maxRadius - coreRadius) * pulseSecondary
            val ring2Alpha = ((1f - pulseSecondary) * (if (isPaused) 0.09f else 0.32f) * (0.4f + ampFactor * 0.4f)).coerceIn(0f, 0.6f)
            drawCircle(
                color = GoldAccent.copy(alpha = ring2Alpha),
                radius = ring2Radius,
                center = center,
                style = Stroke(width = (1.2f + (1f - pulseSecondary) * 1.2f).dp.toPx())
            )

            // -------------------------------------------------------------
            // LAYER 5: Subtle Circular Oscillating Frequency Waveform (In Outer Field)
            // Stays strictly in the outer field; never enters the inner circle
            // -------------------------------------------------------------
            val waveOrbitRadius = coreRadius + (maxRadius - coreRadius) * 0.40f
            val wavePath = Path()
            val waveSegments = 72
            val waveAmplitudePx = (if (isPaused) 1.2.dp else (2.5.dp + 3.5.dp * ampFactor)).toPx()
            val waveFreqCycles = 8

            for (s in 0..waveSegments) {
                val theta = s.toFloat() / waveSegments * (2f * PI.toFloat())
                val undulatingRadius = waveOrbitRadius + sin(theta * waveFreqCycles + waveOffset) * waveAmplitudePx
                val wx = center.x + undulatingRadius * cos(theta)
                val wy = center.y + undulatingRadius * sin(theta)
                if (s == 0) wavePath.moveTo(wx, wy) else wavePath.lineTo(wx, wy)
            }
            wavePath.close()

            drawPath(
                path = wavePath,
                color = OrangeSecondary.copy(alpha = if (isPaused) 0.10f else (0.26f + ampFactor * 0.22f)),
                style = Stroke(width = 1.2.dp.toPx())
            )

            // -------------------------------------------------------------
            // LAYER 4: Luminous Frequency Nodes Orbiting in Outer Field
            // -------------------------------------------------------------
            val particleCount = 6
            val particleOrbitRadius = coreRadius + (maxRadius - coreRadius) * 0.70f
            val baseParticleAlpha = if (isPaused) 0.18f else 0.60f

            for (p in 0 until particleCount) {
                val pAngle = waveOffset + (p.toDouble() * 2.0 * PI / particleCount)
                val px = center.x + (particleOrbitRadius * cos(pAngle)).toFloat()
                val py = center.y + (particleOrbitRadius * sin(pAngle)).toFloat()

                if (isPlaying) {
                    drawCircle(
                        color = OrangePrimary.copy(alpha = 0.18f * ampFactor),
                        radius = 4.5.dp.toPx(),
                        center = Offset(px, py)
                    )
                }

                drawCircle(
                    color = if (p % 2 == 0) OrangePrimary.copy(alpha = baseParticleAlpha) else GoldAccent.copy(alpha = baseParticleAlpha),
                    radius = 2.2.dp.toPx(),
                    center = Offset(px, py)
                )
            }

            // -------------------------------------------------------------
            // HERO CORE: Central Frequency Circle with Deep Pristine Background
            // Clean, non-distracting, perfectly framed hero element
            // -------------------------------------------------------------

            // Soft radial ambient glow radiating outward behind the core border
            val glowRadius = coreRadius * (1.20f * (if (isPlaying) glowBreath else 1.0f))
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        OrangePrimary.copy(alpha = if (isPaused) 0.08f else (0.24f + ampFactor * 0.22f)),
                        OrangeSecondary.copy(alpha = if (isPaused) 0.02f else 0.08f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = glowRadius
                ),
                radius = glowRadius,
                center = center
            )

            // Core Solid Dark Radial Gradient (Pristine background behind text)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF16100B),
                        Color(0xFF0F0B08),
                        DarkBg
                    ),
                    center = center,
                    radius = coreRadius
                ),
                radius = coreRadius,
                center = center
            )

            // Innermost Golden/Orange Boundary Ring
            drawCircle(
                color = OrangePrimary.copy(alpha = if (isPaused) 0.60f else 0.92f),
                radius = coreRadius,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )

            // Subtle inner accent rim (3dp inwards) for luxury metallic precision
            drawCircle(
                color = GoldAccent.copy(alpha = if (isPaused) 0.12f else 0.32f),
                radius = coreRadius - 3.dp.toPx(),
                center = center,
                style = Stroke(width = 0.75.dp.toPx())
            )
        }

        // Center overlay slot: perfectly placed inside the pristine innermost core
        Box(
            modifier = Modifier.size(sizeDp),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}
