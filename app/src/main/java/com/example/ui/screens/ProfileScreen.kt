package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AchievementBadge
import com.example.data.model.ChallengeItem
import com.example.data.model.FrequencyItem
import com.example.data.model.UserProfileStats
import com.example.data.model.WeeklyDayActivity
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
import com.example.ui.viewmodel.ConsistencyScoreData
import com.example.ui.viewmodel.ProfileAnalytics

@Composable
fun ProfileScreen(
    userProfile: UserProfileStats,
    consistencyData: ConsistencyScoreData,
    analytics: ProfileAnalytics,
    activeChallenge: ChallengeItem?,
    weeklyActivity: List<WeeklyDayActivity>,
    achievements: List<AchievementBadge>,
    favorites: List<FrequencyItem>,
    recentlyPlayed: List<FrequencyItem>,
    currentTrack: FrequencyItem?,
    isPlaying: Boolean,
    isUserLoggedIn: Boolean = true,
    onNavigateToLogin: () -> Unit = {},
    onNavigateToSignUp: () -> Unit = {},
    onPlayClick: (FrequencyItem) -> Unit,
    onFrequencyClick: (FrequencyItem) -> Unit,
    onViewAllFavorites: () -> Unit,
    onContinueChallenge: () -> Unit,
    onStartChallenge: () -> Unit,
    onStartFirstSession: () -> Unit,
    onExploreFrequencies: () -> Unit,
    onGoalSelected: (String) -> Unit,
    onUpdateProfile: (name: String, email: String, goals: List<String>, reminderTime: String, reminderEnabled: Boolean, avatarUri: String?) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var activeInfoDialog by remember { mutableStateOf<String?>(null) }
    var showLogoutConfirmation by remember { mutableStateOf(false) }

    // Logged Out State View (Requirement 17)
    if (!isUserLoggedIn) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(DarkBg)
                .padding(28.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(DarkCardElevated),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = OrangePrimary,
                    modifier = Modifier.size(42.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Welcome to SoulSound",
                color = TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Log in to track your listening, challenges and progress.",
                color = TextMuted,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = onNavigateToLogin,
                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary, contentColor = Color.Black),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("profile_logged_out_login_button")
            ) {
                Text("Log In", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onNavigateToSignUp,
                border = BorderStroke(1.dp, BorderOrange),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = OrangePrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("profile_logged_out_signup_button")
            ) {
                Text("Create Account", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
        return
    }

    val isBrandNewUser = userProfile.totalPracticeMinutes == 0 && userProfile.totalDaysCompleted == 0 && recentlyPlayed.isEmpty()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        // 1. Profile Header
        item {
            ProfileHeaderSection(
                userProfile = userProfile,
                onEditProfileClick = { showEditProfileDialog = true }
            )
        }

        // Empty State Banner for New User (Section 21)
        if (isBrandNewUser) {
            item {
                NewUserWelcomeBanner(
                    onStartFirstSession = onStartFirstSession,
                    onExploreClick = onExploreFrequencies
                )
            }
        }

        // 2. Consistency Score (Section 6 & 7)
        item {
            ConsistencyScoreSection(
                consistencyData = consistencyData
            )
        }

        // 3. Today's Progress (Section 8)
        item {
            TodayProgressSection(
                userProfile = userProfile,
                activeChallenge = activeChallenge,
                analytics = analytics,
                onContinueClick = {
                    if (activeChallenge != null) onContinueChallenge() else onStartFirstSession()
                }
            )
        }

        // 4. Active Challenge (Section 9)
        item {
            MyChallengesSection(
                activeChallenge = activeChallenge,
                completedCount = userProfile.completedChallengesCount,
                onContinueChallenge = onContinueChallenge,
                onStartChallenge = onStartChallenge
            )
        }

        // 5. Listening Statistics (Section 5 & 13)
        item {
            ListeningAnalyticsSection(
                analytics = analytics,
                currentStreak = userProfile.currentStreak
            )
        }

        // 6. Weekly Activity (Section 14)
        item {
            WeeklyActivitySection(
                weeklyActivity = weeklyActivity
            )
        }

        // 7. Soul Points + Level (Section 10 & 11)
        item {
            SoulPointsAndLevelSection(
                userProfile = userProfile
            )
        }

        // 8. Achievements (Section 12)
        item {
            AchievementsSection(
                achievements = achievements
            )
        }

        // 9. My Goals (Section 15)
        item {
            MyGoalsSection(
                preferredGoals = userProfile.preferredGoals,
                onGoalSelected = onGoalSelected
            )
        }

        // 10. Favorites Preview (Section 16)
        item {
            FavoritesPreviewSection(
                favorites = favorites,
                onFrequencyClick = onFrequencyClick,
                onViewAllFavorites = onViewAllFavorites
            )
        }

        // 11. Recently Played (Section 17)
        item {
            RecentlyPlayedSection(
                recentlyPlayed = recentlyPlayed,
                currentTrack = currentTrack,
                isPlaying = isPlaying,
                onPlayClick = onPlayClick,
                onFrequencyClick = onFrequencyClick
            )
        }

        // 12. Account Settings (Section 19)
        item {
            AccountSettingsSection(
                onEditProfileClick = { showEditProfileDialog = true },
                onSettingClick = { settingTitle -> activeInfoDialog = settingTitle },
                onLogoutClick = { showLogoutConfirmation = true }
            )
        }
    }

    // Edit Profile Dialog (Section 18)
    if (showEditProfileDialog) {
        EditProfileDialog(
            userProfile = userProfile,
            onDismiss = { showEditProfileDialog = false },
            onSave = { name, email, goals, reminderTime, reminderEnabled, avatarUri ->
                onUpdateProfile(name, email, goals, reminderTime, reminderEnabled, avatarUri)
                showEditProfileDialog = false
            }
        )
    }

    // Information Dialogs (Privacy, Terms, Notifications, Help)
    if (activeInfoDialog != null) {
        InfoDialog(
            title = activeInfoDialog!!,
            onDismiss = { activeInfoDialog = null }
        )
    }

    // Logout Confirmation Dialog (Requirement 19)
    if (showLogoutConfirmation) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirmation = false },
            title = {
                Text(
                    text = "Log out of SoulSound?",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "You can log back in anytime with your email and password.",
                    color = TextMuted,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutConfirmation = false
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary, contentColor = Color.Black)
                ) {
                    Text("Log Out", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirmation = false }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = DarkCardElevated,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

// 1. Profile Header
@Composable
private fun ProfileHeaderSection(
    userProfile: UserProfileStats,
    onEditProfileClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        border = BorderStroke(1.dp, BorderOrange),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // User Avatar with glowing resonant border
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(OrangePrimary.copy(alpha = 0.35f), Color(0xFF1E130B))
                                )
                            )
                            .border(1.5.dp, OrangePrimary, CircleShape)
                    ) {
                        Text(
                            text = if (userProfile.avatarUri != null && userProfile.avatarUri.isNotBlank()) userProfile.avatarUri else "🧘",
                            fontSize = 32.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Hi, ${userProfile.userName}",
                                color = TextPrimary,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(text = " 👋", fontSize = 18.sp)
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = "Your SoulSound Journey",
                            color = GoldAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.5.sp
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = userProfile.userEmail,
                            color = TextMuted,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Edit Profile Button
                IconButton(
                    onClick = onEditProfileClick,
                    modifier = Modifier
                        .size(38.dp)
                        .background(Color(0xFF221710), CircleShape)
                        .border(1.dp, BorderOrange, CircleShape)
                        .testTag("edit_profile_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Profile",
                        tint = OrangePrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Level pill & Primary Goal
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(OrangePrimary.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "LEVEL ${userProfile.levelNumber} • ${userProfile.levelTitle.uppercase()}",
                        color = OrangePrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .background(Color(0xFF201B14), RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "GOAL: ${(userProfile.preferredGoals.firstOrNull() ?: "Meditation").uppercase()}",
                        color = GoldAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

// Section 21: New User Welcome State
@Composable
private fun NewUserWelcomeBanner(
    onStartFirstSession: () -> Unit,
    onExploreClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCardElevated),
        border = BorderStroke(1.dp, BorderOrange),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "WELCOME TO SOULSOUND",
                color = GoldAccent,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Your SoulSound Journey Starts Here.",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "0 min listening • 0 meditation sessions • 0 challenges • 0 Soul Points",
                color = TextMuted,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onStartFirstSession,
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary, contentColor = Color.Black),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                ) {
                    Text("Start First Session", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = onExploreClick,
                    border = BorderStroke(1.dp, BorderOrange),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                ) {
                    Text("Explore Frequencies", color = TextPrimary, fontSize = 12.sp)
                }
            }
        }
    }
}

// 2. Consistency Score Section
@Composable
private fun ConsistencyScoreSection(
    consistencyData: ConsistencyScoreData
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ring_glow")
    val pulseRing by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseRing"
    )

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        border = BorderStroke(1.dp, BorderOrange),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .testTag("profile_consistency_score_card")
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp)
        ) {
            Text(
                text = "SOULSOUND CONSISTENCY",
                color = GoldAccent,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Circular progress ring
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(136.dp)
            ) {
                val scoreFraction = (consistencyData.score / 100f).coerceIn(0f, 1f)

                Canvas(modifier = Modifier.size(136.dp)) {
                    val strokeW = 10.dp.toPx()
                    val radius = (size.minDimension - strokeW) / 2f
                    val center = Offset(size.width / 2f, size.height / 2f)

                    // Track background
                    drawCircle(
                        color = Color(0xFF221710),
                        radius = radius,
                        center = center,
                        style = Stroke(width = strokeW)
                    )

                    // Progress Arc
                    drawArc(
                        brush = Brush.sweepGradient(
                            colors = listOf(
                                OrangeSecondary,
                                OrangePrimary,
                                GoldAccent,
                                OrangePrimary
                            )
                        ),
                        startAngle = -90f,
                        sweepAngle = 360f * scoreFraction,
                        useCenter = false,
                        style = Stroke(width = strokeW, cap = StrokeCap.Round)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${consistencyData.score}",
                        color = TextPrimary,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "/ 100",
                        color = TextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Prominent status pill: 🔥 STRONG
            Box(
                modifier = Modifier
                    .background(
                        color = when (consistencyData.tier) {
                            "Strong" -> OrangePrimary.copy(alpha = 0.2f)
                            "Very Good" -> GoldAccent.copy(alpha = 0.2f)
                            "Good" -> OrangeSecondary.copy(alpha = 0.15f)
                            else -> Color(0xFF261D16)
                        },
                        shape = RoundedCornerShape(12.dp)
                    )
                    .border(
                        1.dp,
                        when (consistencyData.tier) {
                            "Strong" -> OrangePrimary
                            "Very Good" -> GoldAccent
                            else -> BorderOrange
                        },
                        RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = when (consistencyData.tier) {
                        "Strong" -> "🔥 STRONG"
                        "Very Good" -> "✨ VERY GOOD"
                        "Good" -> "🌿 GOOD"
                        else -> "🌱 GETTING STARTED"
                    },
                    color = when (consistencyData.tier) {
                        "Strong" -> OrangePrimary
                        "Very Good" -> GoldAccent
                        else -> TextPrimary
                    },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "You're building a consistent practice.",
                color = TextSubtle,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Motivational Personal Message (Section 7)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1B140E))
                    .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = consistencyData.motivationalMessage,
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// 3. Today's Progress Section (Section 8)
@Composable
private fun TodayProgressSection(
    userProfile: UserProfileStats,
    activeChallenge: ChallengeItem?,
    analytics: ProfileAnalytics,
    onContinueClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        border = BorderStroke(1.dp, BorderSubtle),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TODAY",
                    color = GoldAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
                Text(
                    text = if (activeChallenge != null) "Challenge Active" else "Daily Flow",
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TodayStatPill(
                    icon = Icons.Default.Waves,
                    label = "Listening",
                    value = "${if (userProfile.totalPracticeMinutes > 0) "32" else "0"} min",
                    modifier = Modifier.weight(1f)
                )
                TodayStatPill(
                    icon = Icons.Default.SelfImprovement,
                    label = "Meditation",
                    value = "${if (userProfile.totalPracticeMinutes > 0) "20" else "0"} min",
                    modifier = Modifier.weight(1f)
                )
                TodayStatPill(
                    icon = Icons.Default.Bolt,
                    label = "Challenge",
                    value = if (activeChallenge != null) "Day ${activeChallenge.currentDay}/${activeChallenge.durationDays}" else "—",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Progress Bar
            val challengeFrac = if (activeChallenge != null && activeChallenge.durationDays > 0) {
                (activeChallenge.completedDays.toFloat() / activeChallenge.durationDays).coerceIn(0f, 1f)
            } else if (userProfile.totalPracticeMinutes > 0) 0.65f else 0f

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (activeChallenge != null) "Day ${activeChallenge.currentDay} Target Progress" else "Daily Listening Rhythm",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "${(challengeFrac * 100).toInt()}%",
                        color = OrangePrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                LinearProgressIndicator(
                    progress = { challengeFrac },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = OrangePrimary,
                    trackColor = Color(0xFF261A10)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onContinueClick,
                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary, contentColor = Color.Black),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("continue_today_practice_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Continue Today's Practice",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

@Composable
private fun TodayStatPill(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCardElevated),
        border = BorderStroke(1.dp, BorderSubtle),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = OrangePrimary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
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

// 4. My Challenges Section (Section 9)
@Composable
private fun MyChallengesSection(
    activeChallenge: ChallengeItem?,
    completedCount: Int,
    onContinueChallenge: () -> Unit,
    onStartChallenge: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Text(
            text = "My Challenges",
            color = TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (activeChallenge != null) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCardElevated),
                border = BorderStroke(1.dp, BorderOrange),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${activeChallenge.durationDays}-Day ${activeChallenge.goal} Challenge",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🔥 ${activeChallenge.streak} Day Streak", color = OrangePrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Day ${activeChallenge.currentDay} / ${activeChallenge.durationDays} • ${activeChallenge.progressPercentage}% Complete",
                        color = TextMuted,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { (activeChallenge.progressPercentage / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = OrangePrimary,
                        trackColor = Color(0xFF261A10)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Completed Challenges: $completedCount",
                            color = GoldAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Button(
                            onClick = onContinueChallenge,
                            colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary, contentColor = Color.Black),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text("Continue Challenge", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                border = BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Ready to build a consistent practice?",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Choose your goal, choose your sound, and build your daily habit.",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = onStartChallenge,
                        colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary, contentColor = Color.Black),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                    ) {
                        Text("Start a Challenge", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// 5. Listening Statistics Section (Section 5 & 13)
@Composable
private fun ListeningAnalyticsSection(
    analytics: ProfileAnalytics,
    currentStreak: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Text(
            text = "My Listening",
            color = TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 2x2 Grid of primary statistics
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricCard(
                title = "TOTAL LISTENING",
                value = analytics.totalListeningFormatted,
                subtitle = "Sound library time",
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "MEDITATION TIME",
                value = analytics.meditationFormatted,
                subtitle = "Mindful immersion",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricCard(
                title = "TOTAL SESSIONS",
                value = "${analytics.totalSessions}",
                subtitle = "Completed sessions",
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "CURRENT STREAK",
                value = "🔥 $currentStreak Days",
                subtitle = "Active streak",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Highlights row: Most played frequency & Most used category
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            border = BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "MOST PLAYED FREQUENCY", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = analytics.mostPlayedFreq, color = OrangePrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }

                Box(
                    modifier = Modifier
                        .height(30.dp)
                        .width(1.dp)
                        .background(BorderSubtle)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "MOST USED CATEGORY", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = analytics.mostPlayedCategory, color = GoldAccent, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        border = BorderStroke(1.dp, BorderSubtle),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                color = TextMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                color = TextPrimary,
                fontSize = 19.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = TextSubtle,
                fontSize = 11.sp
            )
        }
    }
}

// 6. Weekly Activity Section (Section 14)
@Composable
private fun WeeklyActivitySection(
    weeklyActivity: List<WeeklyDayActivity>
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        border = BorderStroke(1.dp, BorderSubtle),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Weekly Activity",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Consistency Rhythm",
                    color = GoldAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 7 Days: MON TUE WED THU FRI SAT SUN
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                weeklyActivity.forEach { day ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 2.dp)
                    ) {
                        Text(
                            text = day.dayName,
                            color = TextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Circular dot with orange intensity: 0=subtle gray, 1=dark orange, 2=orange, 3=bright orange
                        val dotColor = when (day.intensity) {
                            3 -> OrangePrimary
                            2 -> OrangeSecondary
                            1 -> Color(0xFF6B3310)
                            else -> Color(0xFF242220)
                        }

                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(dotColor)
                                .border(
                                    1.dp,
                                    if (day.intensity > 0) dotColor else BorderSubtle,
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (day.intensity > 0) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(Color.White.copy(alpha = 0.8f), CircleShape)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "● Dark: 1-15m   ● Orange: 15-30m   ● Bright: 30m+",
                color = TextSubtle,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// 7. Soul Points + Level Section (Section 10 & 11)
@Composable
private fun SoulPointsAndLevelSection(
    userProfile: UserProfileStats
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        border = BorderStroke(1.dp, BorderOrange),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "LEVEL ${userProfile.levelNumber}",
                    color = GoldAccent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
                Text(
                    text = "⭐ ${userProfile.soulPoints} SOUL POINTS",
                    color = OrangePrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = userProfile.levelTitle,
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Progress to next level
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Progress to next level",
                    color = TextMuted,
                    fontSize = 12.sp
                )
                Text(
                    text = "${userProfile.soulPoints} / ${userProfile.levelMaxXp} XP",
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { userProfile.levelProgressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = OrangePrimary,
                trackColor = Color(0xFF261A10)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Level roadmap pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("L1 Beg", "L2 Exp", "L3 Lis", "L4 Pra", "L5 Mas").forEachIndexed { idx, label ->
                    val isCurrent = idx + 1 == userProfile.levelNumber
                    val isPassed = idx + 1 < userProfile.levelNumber
                    Text(
                        text = label,
                        color = when {
                            isCurrent -> OrangePrimary
                            isPassed -> GoldAccent
                            else -> TextSubtle
                        },
                        fontSize = 10.sp,
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

// 8. Achievements Section (Section 12)
@Composable
private fun AchievementsSection(
    achievements: List<AchievementBadge>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Achievements",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${achievements.count { it.isUnlocked }} / ${achievements.size} Unlocked",
                color = GoldAccent,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        achievements.forEach { badge ->
            AchievementRowCard(badge = badge)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun AchievementRowCard(
    badge: AchievementBadge
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (badge.isUnlocked) DarkCardElevated else Color(0xFF131313)
        ),
        border = BorderStroke(
            1.dp,
            if (badge.isUnlocked) BorderOrange else Color.White.copy(alpha = 0.05f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (badge.isUnlocked) OrangePrimary.copy(alpha = 0.2f) else Color(0xFF1E1E1E))
                    .border(
                        1.dp,
                        if (badge.isUnlocked) OrangePrimary else Color.Transparent,
                        CircleShape
                    )
            ) {
                if (badge.isUnlocked) {
                    Text(text = badge.icon, fontSize = 20.sp)
                } else {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = TextSubtle,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = badge.title,
                    color = if (badge.isUnlocked) TextPrimary else TextMuted,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (badge.isUnlocked) badge.description else badge.requirement,
                    color = if (badge.isUnlocked) TextSubtle else TextSubtle.copy(alpha = 0.7f),
                    fontSize = 11.sp
                )
            }

            if (badge.isUnlocked) {
                Box(
                    modifier = Modifier
                        .background(GoldAccent.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(text = "UNLOCKED", color = GoldAccent, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Text(text = "LOCKED", color = TextSubtle, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// 9. My Goals Section (Section 15)
@Composable
private fun MyGoalsSection(
    preferredGoals: List<String>,
    onGoalSelected: (String) -> Unit
) {
    val allGoals = listOf("Focus", "Meditation", "Relaxation", "Sleep", "Energy", "Balance", "Money", "Manifestation")

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        border = BorderStroke(1.dp, BorderSubtle),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "MY GOALS",
                color = GoldAccent,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Current Practice Intention",
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Selectable Goal Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                allGoals.forEach { goal ->
                    val isSelected = preferredGoals.contains(goal)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) OrangePrimary else DarkCardElevated)
                            .border(1.dp, if (isSelected) OrangeSecondary else BorderSubtle, RoundedCornerShape(12.dp))
                            .clickable { onGoalSelected(goal) }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = goal,
                            color = if (isSelected) Color.Black else TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

// 10. Favorites Preview Section (Section 16)
@Composable
private fun FavoritesPreviewSection(
    favorites: List<FrequencyItem>,
    onFrequencyClick: (FrequencyItem) -> Unit,
    onViewAllFavorites: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Your Favorites",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = onViewAllFavorites) {
                Text("View All Favorites →", color = OrangePrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (favorites.isEmpty()) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                border = BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = TextSubtle,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No favorites saved yet",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Tap the heart on any frequency to access it quickly here.",
                        color = TextSubtle,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                favorites.take(4).forEach { item ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkCard),
                        border = BorderStroke(1.dp, BorderOrange),
                        modifier = Modifier
                            .width(160.dp)
                            .clickable { onFrequencyClick(item) }
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(text = item.displayHz, color = OrangePrimary, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = item.name, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(text = item.category, color = TextMuted, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

// 11. Recently Played Section (Section 17)
@Composable
private fun RecentlyPlayedSection(
    recentlyPlayed: List<FrequencyItem>,
    currentTrack: FrequencyItem?,
    isPlaying: Boolean,
    onPlayClick: (FrequencyItem) -> Unit,
    onFrequencyClick: (FrequencyItem) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Text(
            text = "Recently Played",
            color = TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (recentlyPlayed.isEmpty()) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                border = BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Your listening history will appear here.",
                    color = TextMuted,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(20.dp),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                recentlyPlayed.take(4).forEach { item ->
                    val isThisPlaying = isPlaying && currentTrack?.id == item.id
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkCard),
                        border = BorderStroke(1.dp, if (currentTrack?.id == item.id) OrangePrimary else BorderSubtle),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onFrequencyClick(item) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
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
                                        .size(42.dp)
                                        .background(Color(0xFF23160B), CircleShape)
                                        .border(1.dp, OrangePrimary.copy(alpha = 0.4f), CircleShape)
                                ) {
                                    Text(text = item.displayHz, color = OrangePrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(text = item.name, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                    Text(text = "${item.category} • ${item.durationMinutes} min", color = TextMuted, fontSize = 11.sp)
                                }
                            }

                            IconButton(
                                onClick = { onPlayClick(item) },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(if (isThisPlaying) OrangePrimary else Color(0xFF26190E), CircleShape)
                            ) {
                                Icon(
                                    imageVector = if (isThisPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (isThisPlaying) "Pause" else "Play",
                                    tint = if (isThisPlaying) Color.Black else OrangePrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// 12. Account Settings Section (Section 19)
@Composable
private fun AccountSettingsSection(
    onEditProfileClick: () -> Unit,
    onSettingClick: (String) -> Unit,
    onLogoutClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Text(
            text = "Account Settings",
            color = TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            border = BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                SettingRowItem(
                    icon = Icons.Default.Person,
                    title = "Edit Profile",
                    subtitle = "Name, avatar, preferred goals",
                    onClick = onEditProfileClick
                )
                SettingDivider()
                SettingRowItem(
                    icon = Icons.Default.Notifications,
                    title = "Notification Settings",
                    subtitle = "Daily practice reminders",
                    onClick = { onSettingClick("Notification Settings") }
                )
                SettingDivider()
                SettingRowItem(
                    icon = Icons.Default.Alarm,
                    title = "Reminder Settings",
                    subtitle = "Preferred listening schedule",
                    onClick = { onSettingClick("Reminder Settings") }
                )
                SettingDivider()
                SettingRowItem(
                    icon = Icons.Default.Security,
                    title = "Privacy Policy",
                    subtitle = "Private personal sound analytics",
                    onClick = { onSettingClick("Privacy Policy") }
                )
                SettingDivider()
                SettingRowItem(
                    icon = Icons.Default.Policy,
                    title = "Terms of Service",
                    subtitle = "Platform conditions & usage",
                    onClick = { onSettingClick("Terms of Service") }
                )
                SettingDivider()
                SettingRowItem(
                    icon = Icons.AutoMirrored.Filled.HelpOutline,
                    title = "Help & Support",
                    subtitle = "Frequencies, FAQs, audio guide",
                    onClick = { onSettingClick("Help & Support") }
                )
                SettingDivider()
                SettingRowItem(
                    icon = Icons.Default.Logout,
                    title = "Logout",
                    subtitle = "Sign out of your session",
                    tint = OrangePrimary,
                    onClick = onLogoutClick
                )
            }
        }
    }
}

@Composable
private fun SettingRowItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    tint: Color = TextPrimary,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (tint == OrangePrimary) OrangePrimary else OrangeSecondary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(text = title, color = tint, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(text = subtitle, color = TextMuted, fontSize = 11.sp)
            }
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = TextSubtle,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun SettingDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Color.White.copy(alpha = 0.04f))
            .padding(horizontal = 18.dp)
    )
}

// Edit Profile Dialog (Section 18)
@Composable
private fun EditProfileDialog(
    userProfile: UserProfileStats,
    onDismiss: () -> Unit,
    onSave: (name: String, email: String, goals: List<String>, reminderTime: String, reminderEnabled: Boolean, avatarUri: String?) -> Unit
) {
    var name by remember { mutableStateOf(userProfile.userName) }
    var email by remember { mutableStateOf(userProfile.userEmail) }
    var reminderTime by remember { mutableStateOf(userProfile.reminderTime) }
    var reminderEnabled by remember { mutableStateOf(userProfile.reminderEnabled) }
    var selectedAvatar by remember { mutableStateOf(userProfile.avatarUri ?: "🧘") }
    var selectedGoals by remember { mutableStateOf(userProfile.preferredGoals) }

    val avatarOptions = listOf("🧘", "✨", "🌊", "🌿", "⚡", "🔥", "🎧", "⭐")
    val availableGoals = listOf("Meditation", "Focus", "Sleep", "Relaxation", "Energy", "Balance")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Edit SoulSound Profile",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "SELECT AVATAR", color = GoldAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    avatarOptions.forEach { av ->
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(if (selectedAvatar == av) OrangePrimary.copy(alpha = 0.3f) else DarkCard)
                                .border(1.5.dp, if (selectedAvatar == av) OrangePrimary else BorderSubtle, CircleShape)
                                .clickable { selectedAvatar = av }
                        ) {
                            Text(text = av, fontSize = 20.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Your Name") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OrangePrimary,
                        unfocusedBorderColor = BorderSubtle,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OrangePrimary,
                        unfocusedBorderColor = BorderSubtle,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Practice Reminders", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(text = reminderTime, color = TextMuted, fontSize = 11.sp)
                    }
                    Switch(
                        checked = reminderEnabled,
                        onCheckedChange = { reminderEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = OrangePrimary,
                            checkedTrackColor = Color(0xFF381F0E)
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(name, email, selectedGoals, reminderTime, reminderEnabled, selectedAvatar)
                },
                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary, contentColor = Color.Black)
            ) {
                Text("Save Changes", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMuted)
            }
        },
        containerColor = DarkCardElevated,
        shape = RoundedCornerShape(22.dp)
    )
}

@Composable
private fun InfoDialog(
    title: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = title, color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        },
        text = {
            Text(
                text = when (title) {
                    "Privacy Policy" -> "SoulSound values your privacy. Your listening statistics, streak records, favorites, and consistency scores are personal to you and stored privately. We do not monetize or share your listening history."
                    "Terms of Service" -> "SoulSound provides sound therapy and frequency experiences for wellness, focus, meditation, and relaxation. Frequencies are not intended to diagnose, treat, or replace medical consultation."
                    "Notification Settings" -> "Daily practice notifications help you maintain resonance and challenge streaks. You can customize reminders in Profile Settings or mute them anytime."
                    "Reminder Settings" -> "Choose your optimal resonance hour (Morning Coherence or Evening Unwinding) to receive gentle sound check-in prompts."
                    else -> "SoulSound Support is dedicated to your frequency experience. Browse frequencies, explore Solfeggio acoustic notes, or contact support at help@soulsound.app."
                },
                color = TextMuted,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary, contentColor = Color.Black)
            ) {
                Text("Close", fontWeight = FontWeight.Bold)
            }
        },
        containerColor = DarkCardElevated,
        shape = RoundedCornerShape(20.dp)
    )
}
