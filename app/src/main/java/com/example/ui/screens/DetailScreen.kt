package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FrequencyItem
import com.example.data.model.SoundMode
import com.example.ui.components.ResonanceVisualizer
import com.example.ui.theme.BorderOrange
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkCardElevated
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.OrangePrimary
import com.example.ui.theme.OrangeSecondary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSubtle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    frequency: FrequencyItem,
    allFrequencies: List<FrequencyItem>,
    isPlaying: Boolean,
    progressMs: Long,
    totalDurationMs: Long,
    currentVolume: Float,
    soundMode: SoundMode,
    sleepTimerSeconds: Int?,
    amplitude: Float,
    isFavorite: Boolean,
    onBackClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onVolumeChange: (Float) -> Unit,
    onSoundModeChange: (SoundMode) -> Unit,
    onDurationSelected: (Int) -> Unit,
    onSleepTimerSelected: (Int?) -> Unit,
    onFavoriteToggle: () -> Unit,
    onSwitchFrequency: (FrequencyItem) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBackClick() }

    var selectedDurationMinutes by remember(frequency.id, totalDurationMs) {
        mutableIntStateOf(
            (totalDurationMs / (60 * 1000L)).toInt().takeIf { it > 0 } ?: 2
        )
    }
    var customDurationMinutes by remember { mutableStateOf<Int?>(null) }
    var showCustomDurationDialog by remember { mutableStateOf(false) }
    var customInputText by remember { mutableStateOf("") }
    var customInputError by remember { mutableStateOf<String?>(null) }
    var showSleepTimerMenu by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg),
        contentPadding = PaddingValues(bottom = 60.dp)
    ) {
        // Top App Bar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.testTag("detail_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }

                Text(
                    text = "FREQUENCY EXPERIENCE",
                    color = GoldAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )

                IconButton(
                    onClick = onFavoriteToggle,
                    modifier = Modifier.testTag("detail_fav_btn")
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) OrangePrimary else TextMuted
                    )
                }
            }
        }

        // Animated Resonance Visualizer & Frequency Title
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ResonanceVisualizer(
                    sizeDp = 270.dp,
                    isPlaying = isPlaying,
                    amplitude = amplitude
                ) {
                    val hzNumber = if (frequency.hz % 1.0f == 0.0f) "${frequency.hz.toInt()}" else "${frequency.hz}"
                    val numberFontSize = when {
                        hzNumber.length <= 3 -> 34.sp
                        hzNumber.length == 4 -> 30.sp
                        hzNumber.length == 5 -> 26.sp
                        else -> 22.sp
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = hzNumber,
                            color = TextPrimary,
                            fontSize = numberFontSize,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-0.5).sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Hz",
                            color = OrangePrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.5.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = frequency.name,
                    color = TextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${frequency.category} • ${frequency.suggestedContext}",
                    color = TextMuted,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Sound Mode Selector (Pure Tone / Harmonic Drone / Binaural Beats)
        item {
            Spacer(modifier = Modifier.height(18.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "SOUNDSCAPE MODE",
                    color = TextSubtle,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SoundMode.values().forEach { mode ->
                        val isSelected = soundMode == mode
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color(0xFF2A1507) else DarkCard
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) OrangePrimary else BorderSubtle
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .clickable { onSoundModeChange(mode) }
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 4.dp)
                            ) {
                                Text(
                                    text = mode.label,
                                    color = if (isSelected) OrangePrimary else TextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }

        // Duration Selector
        item {
            Spacer(modifier = Modifier.height(18.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "TARGET DURATION",
                    color = TextSubtle,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Target Duration Buttons: 2:00, 5:00, 10:00, 30:00, 60:00, Custom
                // Arranged in two neat, balanced 3-button rows for mobile and desktop screens
                // Row 1: 2:00, 5:00, 10:00
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val row1Presets = listOf(2, 5, 10)
                    row1Presets.forEach { mins ->
                        val isSelected = selectedDurationMinutes == mins && customDurationMinutes == null
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) OrangePrimary else DarkCard
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) OrangePrimary else BorderSubtle
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .clickable {
                                    selectedDurationMinutes = mins
                                    customDurationMinutes = null
                                    onDurationSelected(mins)
                                }
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Text(
                                    text = "$mins:00",
                                    color = if (isSelected) Color.Black else TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Row 2: 30:00, 60:00, Custom
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val row2Presets = listOf(30, 60)
                    row2Presets.forEach { mins ->
                        val isSelected = selectedDurationMinutes == mins && customDurationMinutes == null
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) OrangePrimary else DarkCard
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) OrangePrimary else BorderSubtle
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .clickable {
                                    selectedDurationMinutes = mins
                                    customDurationMinutes = null
                                    onDurationSelected(mins)
                                }
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Text(
                                    text = "$mins:00",
                                    color = if (isSelected) Color.Black else TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    // Custom Button
                    val isCustomSelected = customDurationMinutes != null && selectedDurationMinutes == customDurationMinutes
                    val customLabel = if (isCustomSelected && customDurationMinutes != null) {
                        "Custom • ${customDurationMinutes}m"
                    } else {
                        "Custom"
                    }

                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCustomSelected) OrangePrimary else DarkCard
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (isCustomSelected) OrangePrimary else BorderSubtle
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .clickable {
                                customInputText = customDurationMinutes?.toString() ?: "15"
                                customInputError = null
                                showCustomDurationDialog = true
                            }
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                text = customLabel,
                                color = if (isCustomSelected) Color.Black else TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = if (isCustomSelected) FontWeight.Bold else FontWeight.Medium,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        // Progress Slider & Timestamps
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                val isContinuous = totalDurationMs == Long.MAX_VALUE || selectedDurationMinutes == 0
                val durationValid = !isContinuous && totalDurationMs > 0
                val currentFraction = if (durationValid) {
                    (progressMs.toFloat() / totalDurationMs.toFloat()).coerceIn(0f, 1f)
                } else 0f

                Slider(
                    value = currentFraction,
                    onValueChange = { frac ->
                        if (durationValid) {
                            onSeekTo((frac * totalDurationMs).toLong())
                        }
                    },
                    enabled = durationValid,
                    colors = SliderDefaults.colors(
                        thumbColor = OrangePrimary,
                        activeTrackColor = OrangePrimary,
                        inactiveTrackColor = Color(0x33FF6A00),
                        disabledThumbColor = OrangePrimary.copy(alpha = 0.5f),
                        disabledActiveTrackColor = OrangePrimary.copy(alpha = 0.5f),
                        disabledInactiveTrackColor = Color(0x22FF6A00)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatTimeMs(progressMs),
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                    Text(
                        text = if (isContinuous) "Continuous" else formatTimeMs(totalDurationMs),
                        color = if (isContinuous) OrangePrimary else TextMuted,
                        fontSize = 11.sp,
                        fontWeight = if (isContinuous) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        // Primary Audio Transport Controls
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sleep Timer button
                Box {
                    IconButton(
                        onClick = { showSleepTimerMenu = true },
                        modifier = Modifier
                            .size(46.dp)
                            .background(DarkCard, CircleShape)
                            .border(1.dp, BorderSubtle, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bedtime,
                            contentDescription = "Sleep Timer",
                            tint = if (sleepTimerSeconds != null) OrangePrimary else TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showSleepTimerMenu,
                        onDismissRequest = { showSleepTimerMenu = false }
                    ) {
                        val timerMinutes = listOf(10, 20, 30, 60, null)
                        timerMinutes.forEach { mins ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = if (mins == null) "Off" else "$mins Minutes",
                                        color = TextPrimary
                                    )
                                },
                                onClick = {
                                    onSleepTimerSelected(mins)
                                    showSleepTimerMenu = false
                                }
                            )
                        }
                    }
                }

                // Rewind 10s
                IconButton(
                    onClick = { onSeekTo((progressMs - 10000L).coerceAtLeast(0L)) },
                    modifier = Modifier.size(46.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Replay10,
                        contentDescription = "Rewind 10s",
                        tint = TextPrimary,
                        modifier = Modifier.size(26.dp)
                    )
                }

                // Main Play/Pause Button
                IconButton(
                    onClick = onPlayPauseClick,
                    modifier = Modifier
                        .size(68.dp)
                        .background(OrangePrimary, CircleShape)
                        .testTag("detail_main_play_btn")
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = Color.Black,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Fast Forward 10s
                IconButton(
                    onClick = { onSeekTo(progressMs + 10000L) },
                    modifier = Modifier.size(46.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Forward10,
                        contentDescription = "Forward 10s",
                        tint = TextPrimary,
                        modifier = Modifier.size(26.dp)
                    )
                }

                // Sleep Timer Status or Active indicator
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(DarkCard, CircleShape)
                        .border(1.dp, BorderSubtle, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (sleepTimerSeconds != null) {
                        Text(
                            text = "${sleepTimerSeconds / 60}m",
                            color = OrangePrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Volume Slider
        item {
            Spacer(modifier = Modifier.height(18.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = "Volume",
                    tint = TextMuted,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Slider(
                    value = currentVolume,
                    onValueChange = onVolumeChange,
                    colors = SliderDefaults.colors(
                        thumbColor = OrangeSecondary,
                        activeTrackColor = OrangeSecondary,
                        inactiveTrackColor = Color(0x33FF6A00)
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // About This Frequency
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                border = BorderStroke(1.dp, BorderOrange),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "ABOUT THIS FREQUENCY",
                        color = GoldAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = frequency.longDesc,
                        color = TextPrimary,
                        fontSize = 13.sp,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "INTENDED EXPERIENCE",
                        color = TextSubtle,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        frequency.intendedExperience.forEach { exp ->
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFF26190D), RoundedCornerShape(8.dp))
                                    .border(1.dp, BorderOrange, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = exp,
                                    color = OrangePrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        // Related Frequencies (one-tap switch)
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(
                    text = "RELATED FREQUENCIES",
                    color = GoldAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
                Text(
                    text = "Complementary Harmonic Tones",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                val related = allFrequencies.filter { it.id != frequency.id }.take(3)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    related.forEach { rel ->
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkCardElevated),
                            border = BorderStroke(1.dp, BorderSubtle),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onSwitchFrequency(rel) }
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = rel.displayHz,
                                    color = OrangePrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = rel.name,
                                    color = TextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Custom Duration Dialog
    if (showCustomDurationDialog) {
        AlertDialog(
            onDismissRequest = { showCustomDurationDialog = false },
            title = {
                Text(
                    text = "Custom Duration",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Enter your target session duration in minutes (1 – 180 min):",
                        color = TextMuted,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = customInputText,
                        onValueChange = {
                            val filtered = it.filter { ch -> ch.isDigit() }.take(3)
                            customInputText = filtered
                            customInputError = null
                        },
                        label = { Text("Minutes") },
                        placeholder = { Text("e.g. 45") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        isError = customInputError != null,
                        supportingText = {
                            if (customInputError != null) {
                                Text(customInputError!!, color = Color(0xFFFF5252), fontSize = 11.sp)
                            } else {
                                Text("Minimum 1 min • Maximum 180 min", color = TextSubtle, fontSize = 11.sp)
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OrangePrimary,
                            unfocusedBorderColor = BorderSubtle,
                            errorBorderColor = Color(0xFFFF5252),
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Quick Presets:",
                        color = TextSubtle,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(5, 15, 45, 90, 120).forEach { presetMins ->
                            Card(
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (customInputText == "$presetMins") Color(0xFF2E190A) else DarkCardElevated
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (customInputText == "$presetMins") OrangePrimary else BorderSubtle
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        customInputText = "$presetMins"
                                        customInputError = null
                                    }
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "${presetMins}m",
                                        color = if (customInputText == "$presetMins") OrangePrimary else TextMuted,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val mins = customInputText.trim().toIntOrNull()
                        if (mins == null || mins < 1 || mins > 180) {
                            customInputError = "Please enter a valid duration."
                            return@Button
                        }
                        customDurationMinutes = mins
                        selectedDurationMinutes = mins
                        onDurationSelected(mins)
                        showCustomDurationDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OrangePrimary,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Set Duration", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showCustomDurationDialog = false }
                ) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = DarkCardElevated,
            shape = RoundedCornerShape(18.dp)
        )
    }
}

private fun formatTimeMs(ms: Long): String {
    val totalSeconds = (ms / 1000).toInt()
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}
