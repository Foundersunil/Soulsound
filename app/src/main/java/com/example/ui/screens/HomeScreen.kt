package com.example.ui.screens

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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChallengeItem
import com.example.data.model.FrequencyItem
import com.example.ui.components.FrequencyCard
import com.example.ui.theme.BorderOrange
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkCardElevated
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.OrangePrimary
import com.example.ui.theme.OrangeSecondary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSubtle

@Composable
fun HomeScreen(
    featuredFrequencies: List<FrequencyItem>,
    allFrequencies: List<FrequencyItem>,
    currentTrack: FrequencyItem?,
    isPlaying: Boolean,
    progressMs: Long = 0L,
    totalDurationMs: Long = 0L,
    favoriteIds: Set<String>,
    activeChallenge: ChallengeItem? = null,
    onExploreClick: () -> Unit,
    onGoalSelected: (String) -> Unit,
    onFrequencyClick: (FrequencyItem) -> Unit,
    onPlayClick: (FrequencyItem) -> Unit,
    onFavoriteToggle: (String) -> Unit,
    onChallengeClick: () -> Unit = {},
    onStartChallengeClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Home Challenge Consistency Widget
        item {
            HomeChallengeWidget(
                activeChallenge = activeChallenge,
                onChallengeClick = onChallengeClick,
                onStartChallengeClick = onStartChallengeClick
            )
        }

        // Quick State Selector: "WHAT DO YOU WANT RIGHT NOW?"
        item {
            GoalSelectorSection(onGoalSelected = onGoalSelected)
        }

        // Continue Listening (only shown when an audio item is actively selected or playing in current session)
        if (currentTrack != null) {
            item {
                ContinueListeningSection(
                    track = currentTrack,
                    isPlaying = isPlaying,
                    progressMs = progressMs,
                    totalDurationMs = totalDurationMs,
                    onPlayClick = { onPlayClick(currentTrack) },
                    onTrackClick = { onFrequencyClick(currentTrack) }
                )
            }
        }

        // Featured Frequencies
        item {
            SectionHeader(
                title = "Featured Frequencies",
                subtitle = "Handcrafted resonance experiences for immediate harmony",
                onSeeAllClick = onExploreClick
            )
        }

        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                items(featuredFrequencies.ifEmpty { allFrequencies.take(4) }, key = { it.id }) { item ->
                    FrequencyCard(
                        frequency = item,
                        isCurrentTrack = currentTrack?.id == item.id,
                        isPlaying = isPlaying && currentTrack?.id == item.id,
                        isFavorite = favoriteIds.contains(item.id),
                        onCardClick = { onFrequencyClick(item) },
                        onPlayClick = { onPlayClick(item) },
                        onFavoriteClick = { onFavoriteToggle(item.id) },
                        modifier = Modifier.width(220.dp)
                    )
                }
            }
        }

        // Smart Discovery section: "Not Sure What To Listen To?"
        item {
            SmartDiscoveryBanner(onGoalSelected = onGoalSelected)
        }

        // Popular Frequencies Grid
        item {
            SectionHeader(
                title = "Popular Frequencies",
                subtitle = "Most explored healing and sound meditation frequencies",
                onSeeAllClick = onExploreClick
            )
        }

        item {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                allFrequencies.take(3).forEach { item ->
                    CompactTrackRow(
                        frequency = item,
                        isCurrent = currentTrack?.id == item.id,
                        isPlaying = isPlaying && currentTrack?.id == item.id,
                        onRowClick = { onFrequencyClick(item) },
                        onPlayClick = { onPlayClick(item) }
                    )
                }
            }
        }

        // Why SoulSound
        item {
            Spacer(modifier = Modifier.height(28.dp))
            WhySoulSoundSection()
        }

        // Footer
        item {
            FooterSection()
        }
    }
}


@Composable
private fun HomeChallengeWidget(
    activeChallenge: ChallengeItem?,
    onChallengeClick: () -> Unit,
    onStartChallengeClick: () -> Unit
) {
    if (activeChallenge != null) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCardElevated),
            border = BorderStroke(1.dp, BorderOrange),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp)
                .clickable(onClick = onChallengeClick)
                .testTag("home_active_challenge_widget")
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "YOUR CHALLENGE",
                        color = GoldAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🔥 ${activeChallenge.streak} Day Streak", color = OrangePrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Day ${activeChallenge.currentDay} / ${activeChallenge.durationDays} • ${activeChallenge.goal}",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Today's Practice: ${activeChallenge.frequencyName} (${activeChallenge.frequencyHz.toInt()} Hz) • ${activeChallenge.dailyTargetMinutes} min",
                    color = TextMuted,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onChallengeClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary, contentColor = Color.Black),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                ) {
                    Text("CONTINUE CHALLENGE", fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
                }
            }
        }
    } else {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF19120D)),
            border = BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp)
                .clickable(onClick = onStartChallengeClick)
                .testTag("home_ready_challenge_widget")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Text(text = "🔥", fontSize = 26.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "READY FOR A CHALLENGE?",
                            color = OrangePrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Build a consistent sound & frequency practice.",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Button(
                    onClick = onStartChallengeClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary, contentColor = Color.Black),
                    modifier = Modifier.height(38.dp)
                ) {
                    Text("Start", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun GoalSelectorSection(
    onGoalSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text(
                text = "WHAT DO YOU WANT RIGHT NOW?",
                color = GoldAccent,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )
            Text(
                text = "Choose Your Desired State",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        val goals = listOf(
            GoalItem("FOCUS", Icons.Default.Psychology),
            GoalItem("MEDITATE", Icons.Default.SelfImprovement),
            GoalItem("RELAX", Icons.Default.Spa),
            GoalItem("SLEEP", Icons.Default.NightsStay),
            GoalItem("ENERGY", Icons.Default.Bolt),
            GoalItem("BALANCE", Icons.Default.Waves),
            GoalItem("MONEY", Icons.Default.Star),
            GoalItem("MANIFEST", Icons.Default.Visibility)
        )

        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            goals.forEach { goal ->
                GoalChip(
                    goal = goal,
                    onClick = { onGoalSelected(goal.title) }
                )
            }
        }
    }
}

private data class GoalItem(val title: String, val icon: ImageVector)

@Composable
private fun GoalChip(
    goal: GoalItem,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        border = BorderStroke(1.dp, BorderSubtle),
        modifier = Modifier
            .clickable(onClick = onClick)
            .testTag("goal_chip_${goal.title.lowercase()}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Icon(
                imageVector = goal.icon,
                contentDescription = null,
                tint = OrangePrimary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = goal.title,
                color = TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
private fun ContinueListeningSection(
    track: FrequencyItem,
    isPlaying: Boolean,
    progressMs: Long,
    totalDurationMs: Long,
    onPlayClick: () -> Unit,
    onTrackClick: () -> Unit
) {
    val progressFraction = if (totalDurationMs > 0L && totalDurationMs != Long.MAX_VALUE) {
        (progressMs.toFloat() / totalDurationMs.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val formattedDuration = if (totalDurationMs > 0L && totalDurationMs != Long.MAX_VALUE) {
        val totalSec = totalDurationMs / 1000
        val mins = totalSec / 60
        val secs = totalSec % 60
        String.format("%02d:%02d", mins, secs)
    } else "${track.durationMinutes} min"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .testTag("home_continue_listening_section")
    ) {
        Text(
            text = "CONTINUE LISTENING",
            color = TextMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCardElevated),
            border = BorderStroke(1.dp, BorderOrange),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .clickable(onClick = onTrackClick)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color(0xFF2B180A), CircleShape)
                                .border(1.dp, OrangePrimary.copy(alpha = 0.4f), CircleShape)
                        ) {
                            Text(
                                text = track.displayHz,
                                color = OrangePrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = track.name,
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${track.category} • $formattedDuration",
                                color = TextMuted,
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = onPlayClick,
                        modifier = Modifier
                            .size(42.dp)
                            .background(OrangePrimary, CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.Black,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                LinearProgressIndicator(
                    progress = { progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp),
                    color = OrangePrimary,
                    trackColor = Color(0x33FF6A00)
                )
            }
        }
    }
}

@Composable
private fun SmartDiscoveryBanner(
    onGoalSelected: (String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141210)),
        border = BorderStroke(1.dp, BorderOrange),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "SMART DISCOVERY",
                color = GoldAccent,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Not Sure What To Listen To?",
                color = TextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Tap your current state below to instantly surface tuned acoustic frequencies.",
                color = TextMuted,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            val smartOptions = listOf(
                "I want to focus" to "Focus",
                "I want to relax" to "Relax",
                "I want deeper meditation" to "Meditate",
                "I want better sleep" to "Sleep",
                "I want more energy" to "Energy",
                "I want abundance / money mindset" to "Money",
                "I want manifestation practice" to "Manifest",
                "I want emotional balance" to "Balance"
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                smartOptions.take(4).forEach { (promptText, goal) ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkCard)
                            .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                            .clickable { onGoalSelected(goal) }
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = promptText,
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = OrangePrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactTrackRow(
    frequency: FrequencyItem,
    isCurrent: Boolean,
    isPlaying: Boolean,
    onRowClick: () -> Unit,
    onPlayClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        border = BorderStroke(1.dp, if (isCurrent) OrangePrimary else BorderSubtle),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onRowClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(46.dp)
                        .background(Color(0xFF221509), CircleShape)
                ) {
                    Text(
                        text = frequency.displayHz,
                        color = OrangePrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = frequency.name,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        text = "${frequency.category} • ${frequency.tags.take(2).joinToString(", ")}",
                        color = TextMuted,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }
            }

            IconButton(
                onClick = onPlayClick,
                modifier = Modifier
                    .size(38.dp)
                    .background(if (isPlaying) OrangePrimary else Color(0xFF26190E), CircleShape)
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = if (isPlaying) Color.Black else OrangePrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun WhySoulSoundSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Text(
            text = "WHY SOULSOUND",
            color = GoldAccent,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp
        )
        Text(
            text = "The Modern Frequency Architecture",
            color = TextPrimary,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(14.dp))

        val pillars = listOf(
            Triple("1. Discover", "Explore harmonic frequencies mapped directly to your targeted mental, creative, and physical states.", Icons.Default.Waves),
            Triple("2. Choose", "Select your listening duration, harmonic warmth, and binaural beat configuration.", Icons.Default.Tune),
            Triple("3. Listen", "Real-time acoustic synthesis generates mathematically pure sine tones and resonant warmth.", Icons.Default.GraphicEq),
            Triple("4. Harmonize", "Create personal playlists, favorite foundational tones, and track your daily sessions.", Icons.Default.SelfImprovement)
        )

        pillars.forEach { (title, desc, _) ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                border = BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = title,
                        color = OrangePrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = desc,
                        color = TextMuted,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun FooterSection() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 28.dp, bottom = 16.dp, start = 24.dp, end = 24.dp)
    ) {
        Text(
            text = "SOULSOUND",
            color = OrangePrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 2.sp
        )
        Text(
            text = "Sound & Frequency Experience",
            color = TextMuted,
            fontSize = 11.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "SoulSound provides sound and frequency experiences intended for meditation, relaxation, and mindful listening. Non-diagnostic and non-medical.",
            color = TextSubtle,
            fontSize = 10.sp,
            textAlign = TextAlign.Center,
            lineHeight = 14.sp
        )
    }
}

@Composable
private fun SectionHeader(
    title: String,
    subtitle: String,
    onSeeAllClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                color = TextMuted,
                fontSize = 11.sp
            )
        }

        Text(
            text = "See All",
            color = OrangePrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable(onClick = onSeeAllClick)
        )
    }
}
