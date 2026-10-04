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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChallengeDayItem
import com.example.data.model.ChallengeItem
import com.example.data.model.FrequencyItem
import com.example.data.model.UserProfileStats
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

@Composable
fun ChallengeScreen(
    activeChallenge: ChallengeItem?,
    allChallenges: List<ChallengeItem>,
    challengeDays: List<ChallengeDayItem>,
    userProfile: UserProfileStats,
    allFrequencies: List<FrequencyItem>,
    onStartCreateChallenge: () -> Unit,
    onStartPractice: (ChallengeItem) -> Unit,
    onCompleteTodayPractice: () -> Unit,
    onUseRecoveryDay: () -> Unit,
    onAbandonChallenge: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showAbandonConfirm by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        // Hero Header
        item {
            ChallengeHeroHeader(
                onStartChallengeClick = onStartCreateChallenge,
                hasActiveChallenge = activeChallenge != null
            )
        }

        // User Level & Soul Points Progression Card
        item {
            UserLevelCard(userProfile = userProfile)
        }

        // Active Challenge OR "Start A Challenge" Section
        if (activeChallenge != null) {
            item {
                ActiveChallengeSection(
                    challenge = activeChallenge,
                    days = challengeDays,
                    onStartPractice = { onStartPractice(activeChallenge) },
                    onCompleteToday = onCompleteTodayPractice,
                    onUseRecoveryDay = onUseRecoveryDay,
                    onAbandonClick = { showAbandonConfirm = true }
                )
            }
        } else {
            item {
                NoActiveChallengeSection(
                    onStartChallengeClick = onStartCreateChallenge,
                    allFrequencies = allFrequencies
                )
            }
        }

        // Consistency Journey Calendar & Stats
        item {
            ConsistencyJourneySection(
                userProfile = userProfile,
                challengeDays = challengeDays,
                activeChallenge = activeChallenge
            )
        }

        // Challenge History
        item {
            ChallengeHistorySection(allChallenges = allChallenges)
        }
    }

    if (showAbandonConfirm) {
        AlertDialog(
            onDismissRequest = { showAbandonConfirm = false },
            title = {
                Text(
                    text = "Pause or Abandon Challenge?",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to end this challenge? Your progress and completed days will be archived in your challenge history.",
                    color = TextMuted,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onAbandonChallenge()
                        showAbandonConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF991B1B))
                ) {
                    Text("End Challenge", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAbandonConfirm = false }) {
                    Text("Keep Going", color = TextPrimary)
                }
            },
            containerColor = DarkCardElevated
        )
    }
}

@Composable
private fun ChallengeHeroHeader(
    onStartChallengeClick: () -> Unit,
    hasActiveChallenge: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF26150A),
                        Color(0xFF141414)
                    )
                )
            )
            .border(1.dp, BorderOrange, RoundedCornerShape(24.dp))
            .padding(22.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(OrangePrimary.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocalFireDepartment,
                    contentDescription = null,
                    tint = OrangePrimary,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "CONSISTENCY & CHALLENGES",
                    color = OrangePrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Build Your Practice.\nOne Day At A Time.",
                color = TextPrimary,
                fontSize = 25.sp,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 31.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "“Don’t just listen once. Build a daily practice.” Choose your goal, choose your frequency, and stay consistent.",
                color = TextMuted,
                fontSize = 13.sp,
                lineHeight = 19.sp
            )

            if (!hasActiveChallenge) {
                Spacer(modifier = Modifier.height(18.dp))
                Button(
                    onClick = onStartChallengeClick,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OrangePrimary,
                        contentColor = Color.Black
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("challenges_start_cta")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Start A Challenge",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun UserLevelCard(userProfile: UserProfileStats) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCardElevated),
        border = BorderStroke(1.dp, BorderSubtle),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(Color(0xFF2B180A), CircleShape)
                            .border(1.dp, GoldAccent, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "L${userProfile.levelNumber}",
                            color = GoldAccent,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = userProfile.levelTitle,
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Level ${userProfile.levelNumber} Practitioner",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Points",
                        tint = GoldAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${userProfile.soulPoints} Soul Points",
                        color = GoldAccent,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // XP Progress Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${userProfile.soulPoints} / ${userProfile.levelMaxXp} XP",
                    color = TextMuted,
                    fontSize = 11.sp
                )
                Text(
                    text = "Next: Level ${userProfile.levelNumber + 1}",
                    color = GoldAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { userProfile.levelProgressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape),
                color = OrangePrimary,
                trackColor = Color(0x33FF6A00)
            )
        }
    }
}

@Composable
private fun ActiveChallengeSection(
    challenge: ChallengeItem,
    days: List<ChallengeDayItem>,
    onStartPractice: () -> Unit,
    onCompleteToday: () -> Unit,
    onUseRecoveryDay: () -> Unit,
    onAbandonClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "MY ACTIVE CHALLENGE",
                color = GoldAccent,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )

            Text(
                text = "Pause / End",
                color = TextSubtle,
                fontSize = 11.sp,
                modifier = Modifier.clickable(onClick = onAbandonClick)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Main Challenge Card
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCardElevated),
            border = BorderStroke(1.5.dp, BorderOrange),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${challenge.durationDays}-DAY ${challenge.goal.uppercase()} CHALLENGE",
                            color = TextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Day ${challenge.currentDay} of ${challenge.durationDays}",
                            color = OrangePrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(
                                color = if (challenge.isTodayCompleted) Color(0xFF14532D) else Color(0xFF2E190A),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .border(
                                1.dp,
                                if (challenge.isTodayCompleted) Color(0xFF22C55E) else OrangePrimary,
                                RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = if (challenge.isTodayCompleted) "TODAY COMPLETE ✓" else "PRACTICE PENDING",
                            color = if (challenge.isTodayCompleted) Color(0xFF86EFAC) else OrangePrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Progress Bar
                LinearProgressIndicator(
                    progress = { challenge.progressPercentage / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape),
                    color = OrangePrimary,
                    trackColor = Color(0x33FF6A00)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${challenge.progressPercentage}% Completed",
                        color = TextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${challenge.remainingDays} Days Remaining",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Stats Chips Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatPill(
                        label = "Streak",
                        value = "🔥 ${challenge.streak}d",
                        modifier = Modifier.weight(1f)
                    )
                    StatPill(
                        label = "Longest",
                        value = "${challenge.longestStreak}d",
                        modifier = Modifier.weight(1f)
                    )
                    StatPill(
                        label = "Completed",
                        value = "${challenge.completedDays}d",
                        modifier = Modifier.weight(1f)
                    )
                    StatPill(
                        label = "Points",
                        value = "⭐ ${challenge.rewardPoints}",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Today's Practice Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    border = BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .background(Color(0xFF26180B), CircleShape)
                                    .border(1.dp, OrangePrimary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (challenge.frequencyHz % 1.0f == 0.0f) "${challenge.frequencyHz.toInt()}" else "${challenge.frequencyHz}",
                                    color = OrangePrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = challenge.frequencyName,
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Today's Target: ${challenge.dailyTargetMinutes} Minutes",
                                    color = GoldAccent,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        IconButton(
                            onClick = onStartPractice,
                            modifier = Modifier
                                .size(42.dp)
                                .background(OrangePrimary, CircleShape)
                                .testTag("challenge_today_play_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play Today's Practice",
                                tint = Color.Black,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action buttons: "START TODAY'S PRACTICE" & "COMPLETE PRACTICE"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onStartPractice,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = OrangePrimary,
                            contentColor = Color.Black
                        ),
                        modifier = Modifier
                            .weight(1.3f)
                            .height(48.dp)
                            .testTag("start_today_practice_cta")
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Start Practice",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (!challenge.isTodayCompleted) {
                        OutlinedButton(
                            onClick = onCompleteToday,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldAccent),
                            border = BorderStroke(1.dp, GoldAccent),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("mark_today_complete_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Mark Done",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Recovery Day section
                if (challenge.recoveryDaysAvailable > 0) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1E1710), RoundedCornerShape(12.dp))
                            .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🛡️", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${challenge.recoveryDaysAvailable} Recovery Day Available",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }

                        Text(
                            text = "Apply Recovery",
                            color = OrangePrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable(onClick = onUseRecoveryDay)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Day-by-Day Journey Visualization
        Text(
            text = "DAILY JOURNEY",
            color = GoldAccent,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp
        )
        Spacer(modifier = Modifier.height(10.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(days, key = { it.dayNumber }) { day ->
                DayBadge(day = day)
            }
        }
    }
}

@Composable
private fun StatPill(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                color = TextMuted,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun DayBadge(day: ChallengeDayItem) {
    val bg = when {
        day.isCompleted -> Color(0xFF28180A)
        day.isCurrent -> OrangePrimary
        day.isMissed -> Color(0xFF261010)
        else -> DarkCard
    }

    val borderColor = when {
        day.isCompleted -> GoldAccent
        day.isCurrent -> OrangePrimary
        day.isMissed -> Color(0xFFEF4444)
        else -> BorderSubtle
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = bg),
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier.width(62.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "DAY",
                color = if (day.isCurrent) Color.Black else TextMuted,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = String.format("%02d", day.dayNumber),
                color = if (day.isCurrent) Color.Black else TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = when {
                    day.isCompleted -> "✓"
                    day.isCurrent -> "🔥"
                    day.isMissed -> "✗"
                    else -> "•"
                },
                color = when {
                    day.isCompleted -> GoldAccent
                    day.isCurrent -> Color.Black
                    day.isMissed -> Color(0xFFEF4444)
                    else -> TextSubtle
                },
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun NoActiveChallengeSection(
    onStartChallengeClick: () -> Unit,
    allFrequencies: List<FrequencyItem>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCardElevated),
            border = BorderStroke(1.dp, BorderOrange),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🎯",
                    fontSize = 36.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Ready To Start Your Journey?",
                    color = TextPrimary,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Commit to daily listening to train your brainwaves, quiet mental chatter, and experience tangible consistency.",
                    color = TextMuted,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = onStartChallengeClick,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OrangePrimary,
                        contentColor = Color.Black
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("no_active_start_challenge_btn")
                ) {
                    Text(
                        text = "START A CHALLENGE",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ConsistencyJourneySection(
    userProfile: UserProfileStats,
    challengeDays: List<ChallengeDayItem>,
    activeChallenge: ChallengeItem?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Text(
            text = "MY CONSISTENCY JOURNEY",
            color = GoldAccent,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp
        )
        Text(
            text = "Habit & Practice Records",
            color = TextPrimary,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Visual Consistency Calendar Row
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            border = BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Recent Consistency Rhythm",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(10.dp))

                // 7-day consistency markers (O O X O O O O style)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val daysOfWeek = listOf("M", "T", "W", "T", "F", "S", "S")
                    val completions = listOf(true, true, true, false, true, true, true)

                    daysOfWeek.forEachIndexed { idx, dayName ->
                        val isDone = completions.getOrElse(idx) { true }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = dayName, color = TextMuted, fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(
                                        if (isDone) OrangePrimary else Color(0xFF261919),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isDone) "✓" else "•",
                                    color = if (isDone) Color.Black else TextSubtle,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Stats grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            JourneyStatCard(
                label = "Total Practice",
                value = "${userProfile.totalPracticeMinutes}m",
                modifier = Modifier.weight(1f)
            )
            JourneyStatCard(
                label = "Days Completed",
                value = "${userProfile.totalDaysCompleted}d",
                modifier = Modifier.weight(1f)
            )
            JourneyStatCard(
                label = "Challenges Done",
                value = "${userProfile.completedChallengesCount}",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun JourneyStatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCardElevated),
        border = BorderStroke(1.dp, BorderSubtle),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                color = OrangePrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                color = TextMuted,
                fontSize = 10.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ChallengeHistorySection(allChallenges: List<ChallengeItem>) {
    val completedOrPast = allChallenges.filter { it.status != "active" }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Text(
            text = "CHALLENGE HISTORY",
            color = GoldAccent,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp
        )
        Text(
            text = "Past Consistency Milestones",
            color = TextPrimary,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (completedOrPast.isEmpty()) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                border = BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No Completed Challenges Yet",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Complete your first consistency challenge to earn your legacy badges.",
                        color = TextMuted,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            completedOrPast.forEach { challenge ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    border = BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${challenge.durationDays}-Day ${challenge.goal}",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${challenge.completedDays}/${challenge.durationDays} Days • Streak: ${challenge.longestStreak}d",
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                        }

                        Text(
                            text = "+${challenge.rewardPoints} XP",
                            color = GoldAccent,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
