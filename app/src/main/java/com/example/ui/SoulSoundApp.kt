package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.FrequencyItem
import com.example.ui.components.BottomNav
import com.example.ui.components.CreateChallengeWizard
import com.example.ui.components.MiniPlayer
import com.example.ui.components.TopHeader
import com.example.ui.screens.AdminLoginScreen
import com.example.ui.screens.AdminScreen
import com.example.ui.screens.ChallengeScreen
import com.example.ui.screens.DetailScreen
import com.example.ui.screens.ExploreScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.UserLoginScreen
import com.example.ui.screens.UserSignUpScreen
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkCardElevated
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.OrangePrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.viewmodel.Screen
import com.example.ui.viewmodel.SoulSoundViewModel

@Composable
fun SoulSoundApp(
    viewModel: SoulSoundViewModel = viewModel()
) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val selectedFrequency by viewModel.selectedFrequency.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val sortOption by viewModel.sortOption.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val isAdminMode by viewModel.isAdminMode.collectAsStateWithLifecycle()

    val allFrequencies by viewModel.allFrequencies.collectAsStateWithLifecycle()
    val featuredFrequencies by viewModel.featuredFrequencies.collectAsStateWithLifecycle()
    val favoriteIds by viewModel.favoriteIds.collectAsStateWithLifecycle()
    val history by viewModel.recentHistory.collectAsStateWithLifecycle()
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()
    val filteredExplore by viewModel.filteredExploreFrequencies.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()

    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val currentTrack by viewModel.currentTrack.collectAsStateWithLifecycle()
    val currentSoundMode by viewModel.currentSoundMode.collectAsStateWithLifecycle()
    val volume by viewModel.volume.collectAsStateWithLifecycle()
    val progressMs by viewModel.progressMs.collectAsStateWithLifecycle()
    val totalDurationMs by viewModel.totalDurationMs.collectAsStateWithLifecycle()
    val sleepTimerSeconds by viewModel.sleepTimerSeconds.collectAsStateWithLifecycle()
    val liveAmplitude by viewModel.liveAmplitude.collectAsStateWithLifecycle()

    val showPremiumDialog by viewModel.showPremiumDialog.collectAsStateWithLifecycle()
    val premiumTargetTrack by viewModel.premiumTargetTrack.collectAsStateWithLifecycle()

    val activeChallenge by viewModel.activeChallenge.collectAsStateWithLifecycle()
    val allChallenges by viewModel.allChallenges.collectAsStateWithLifecycle()
    val challengeDays by viewModel.challengeDays.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val showCreateChallengeWizard by viewModel.showCreateChallengeWizard.collectAsStateWithLifecycle()
    val activeMilestoneCelebration by viewModel.activeMilestoneCelebration.collectAsStateWithLifecycle()
    val showChallengeCompletedCelebration by viewModel.showChallengeCompletedCelebration.collectAsStateWithLifecycle()
    val recoveryStatusMessage by viewModel.recoveryStatusMessage.collectAsStateWithLifecycle()

    val consistencyData by viewModel.consistencyData.collectAsStateWithLifecycle()
    val weeklyActivity by viewModel.weeklyActivity.collectAsStateWithLifecycle()
    val achievements by viewModel.achievements.collectAsStateWithLifecycle()
    val profileFavorites by viewModel.profileFavorites.collectAsStateWithLifecycle()
    val profileRecentlyPlayed by viewModel.profileRecentlyPlayed.collectAsStateWithLifecycle()
    val profileAnalytics by viewModel.profileAnalytics.collectAsStateWithLifecycle()

    val isAdminAuthenticated by viewModel.isAdminAuthenticated.collectAsStateWithLifecycle()
    val currentAdminEmail by viewModel.currentAdminEmail.collectAsStateWithLifecycle()
    val allFrequenciesAdmin by viewModel.allFrequenciesAdmin.collectAsStateWithLifecycle()
    val adminAuditLogs by viewModel.adminAuditLogs.collectAsStateWithLifecycle()
    val adminLoginError by viewModel.adminLoginError.collectAsStateWithLifecycle()
    val adminLoginLoading by viewModel.adminLoginLoading.collectAsStateWithLifecycle()

    val userAuthState by viewModel.userAuthState.collectAsStateWithLifecycle()
    val userAuthError by viewModel.userAuthError.collectAsStateWithLifecycle()
    val userAuthLoading by viewModel.userAuthLoading.collectAsStateWithLifecycle()
    val userPasswordResetSuccess by viewModel.userPasswordResetSuccess.collectAsStateWithLifecycle()

    if (currentScreen == Screen.AUTH_SIGN_UP) {
        BackHandler {
            viewModel.navigateToLogin()
        }
    } else if (currentScreen == Screen.ADMIN_LOGIN || currentScreen == Screen.ADMIN || currentScreen == Screen.SEARCH) {
        BackHandler {
            viewModel.navigateTo(Screen.HOME)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        if (selectedFrequency != null) {
            val freq = selectedFrequency!!
            BackHandler { viewModel.closeDetail() }
            DetailScreen(
                frequency = freq,
                allFrequencies = allFrequencies,
                isPlaying = isPlaying && currentTrack?.id == freq.id,
                progressMs = if (currentTrack?.id == freq.id) progressMs else 0L,
                totalDurationMs = if (currentTrack?.id == freq.id) totalDurationMs else (freq.durationMinutes * 60 * 1000L),
                currentVolume = volume,
                soundMode = currentSoundMode,
                sleepTimerSeconds = sleepTimerSeconds,
                amplitude = liveAmplitude,
                isFavorite = favoriteIds.contains(freq.id),
                onBackClick = { viewModel.closeDetail() },
                onPlayPauseClick = {
                    if (currentTrack?.id == freq.id) {
                        viewModel.togglePlayPause()
                    } else {
                        viewModel.playTrack(freq)
                    }
                },
                onSeekTo = { viewModel.seekTo(it) },
                onVolumeChange = { viewModel.setVolume(it) },
                onSoundModeChange = { viewModel.setSoundMode(it) },
                onDurationSelected = { viewModel.setTargetDuration(it) },
                onSleepTimerSelected = { viewModel.setSleepTimer(it) },
                onFavoriteToggle = { viewModel.toggleFavorite(freq.id) },
                onSwitchFrequency = { newFreq ->
                    viewModel.openDetail(newFreq)
                    viewModel.playTrack(newFreq)
                },
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.statusBars)
            )
        } else if (currentScreen == Screen.AUTH_LOGIN) {
            UserLoginScreen(
                onLoginSubmit = { email, password -> viewModel.logInUser(email, password) },
                onNavigateToSignUp = { viewModel.navigateToSignUp() },
                onContinueAsGuest = { viewModel.continueAsGuest() },
                onForgotPasswordSubmit = { email -> viewModel.sendPasswordReset(email) },
                errorMessage = userAuthError,
                passwordResetMessage = userPasswordResetSuccess,
                isLoading = userAuthLoading,
                isFirebaseConnected = viewModel.isFirebaseConnected,
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.statusBars)
            )
        } else if (currentScreen == Screen.AUTH_SIGN_UP) {
            UserSignUpScreen(
                onSignUpSubmit = { email, password, confirmPassword -> viewModel.signUpUser(email, password, confirmPassword) },
                onNavigateToLogin = { viewModel.navigateToLogin() },
                onContinueAsGuest = { viewModel.continueAsGuest() },
                errorMessage = userAuthError,
                isLoading = userAuthLoading,
                onCalculateStrength = { pass -> viewModel.calculatePasswordStrength(pass) },
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.statusBars)
            )
        } else {
            Scaffold(
                containerColor = DarkBg,
                topBar = {
                    TopHeader(
                        onSearchClick = { viewModel.navigateTo(Screen.SEARCH) },
                        onAdminAccess = { viewModel.attemptOpenAdmin() },
                        modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
                    )
                },
                bottomBar = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Persistent Mini Player above bottom nav
                        MiniPlayer(
                            currentTrack = currentTrack,
                            isPlaying = isPlaying,
                            progressMs = progressMs,
                            totalDurationMs = totalDurationMs,
                            isFavorite = currentTrack?.let { favoriteIds.contains(it.id) } ?: false,
                            amplitude = liveAmplitude,
                            onPlayerClick = { currentTrack?.let { viewModel.openDetail(it) } },
                            onPlayPauseClick = { viewModel.togglePlayPause() },
                            onFavoriteClick = { currentTrack?.let { viewModel.toggleFavorite(it.id) } }
                        )

                        // Mobile Bottom Navigation
                        BottomNav(
                            currentScreen = currentScreen,
                            onNavigate = { viewModel.navigateTo(it) }
                        )
                    }
                }
            ) { innerPadding ->
                AnimatedContent(
                    targetState = currentScreen,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "screen_transition",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) { targetScreen ->
                    when (targetScreen) {
                        Screen.HOME -> HomeScreen(
                            featuredFrequencies = featuredFrequencies,
                            allFrequencies = allFrequencies,
                            currentTrack = currentTrack,
                            isPlaying = isPlaying,
                            favoriteIds = favoriteIds,
                            activeChallenge = activeChallenge,
                            onExploreClick = { viewModel.navigateTo(Screen.EXPLORE) },
                            onGoalSelected = { goal -> viewModel.filterByGoal(goal) },
                            onFrequencyClick = { freq -> viewModel.openDetail(freq) },
                            onPlayClick = { freq -> viewModel.playOrToggleTrack(freq) },
                            onFavoriteToggle = { freqId -> viewModel.toggleFavorite(freqId) },
                            onChallengeClick = { viewModel.navigateTo(Screen.CHALLENGES) },
                            onStartChallengeClick = { viewModel.startCreateChallengeFlow() }
                        )
                        Screen.CHALLENGES -> ChallengeScreen(
                            activeChallenge = activeChallenge,
                            allChallenges = allChallenges,
                            challengeDays = challengeDays,
                            userProfile = userProfile,
                            allFrequencies = allFrequencies,
                            onStartCreateChallenge = { viewModel.startCreateChallengeFlow() },
                            onStartPractice = { challenge -> viewModel.startChallengePractice(challenge) },
                            onCompleteTodayPractice = { viewModel.completeTodayPractice() },
                            onUseRecoveryDay = { viewModel.useRecoveryDay() },
                            onAbandonChallenge = { viewModel.abandonActiveChallenge() }
                        )
                        Screen.EXPLORE -> ExploreScreen(
                            frequencies = filteredExplore,
                            selectedCategory = selectedCategory,
                            selectedSortOption = sortOption,
                            currentTrack = currentTrack,
                            isPlaying = isPlaying,
                            favoriteIds = favoriteIds,
                            onCategorySelected = { cat -> viewModel.selectCategory(cat) },
                            onSortSelected = { sort -> viewModel.setSortOption(sort) },
                            onFrequencyClick = { freq -> viewModel.openDetail(freq) },
                            onPlayClick = { freq -> viewModel.playOrToggleTrack(freq) },
                            onFavoriteToggle = { freqId -> viewModel.toggleFavorite(freqId) }
                        )
                        Screen.SEARCH -> SearchScreen(
                            query = searchQuery,
                            results = searchResults,
                            currentTrack = currentTrack,
                            isPlaying = isPlaying,
                            onQueryChange = { q -> viewModel.setSearchQuery(q) },
                            onFrequencyClick = { freq -> viewModel.openDetail(freq) },
                            onPlayClick = { freq -> viewModel.playOrToggleTrack(freq) }
                        )
                        Screen.LIBRARY -> LibraryScreen(
                            allFrequencies = allFrequencies,
                            favoriteIds = favoriteIds,
                            history = history,
                            playlists = playlists,
                            currentTrack = currentTrack,
                            isPlaying = isPlaying,
                            userProfile = userProfile,
                            allChallenges = allChallenges,
                            onFrequencyClick = { freq -> viewModel.openDetail(freq) },
                            onPlayClick = { freq -> viewModel.playOrToggleTrack(freq) },
                            onFavoriteToggle = { freqId -> viewModel.toggleFavorite(freqId) },
                            onCreatePlaylist = { title -> viewModel.createPlaylist(title) },
                            onDeletePlaylist = { id -> viewModel.deletePlaylist(id) }
                        )
                        Screen.PROFILE -> ProfileScreen(
                            userProfile = userProfile,
                            consistencyData = consistencyData,
                            analytics = profileAnalytics,
                            activeChallenge = activeChallenge,
                            weeklyActivity = weeklyActivity,
                            achievements = achievements,
                            favorites = profileFavorites,
                            recentlyPlayed = profileRecentlyPlayed,
                            currentTrack = currentTrack,
                            isPlaying = isPlaying,
                            isUserLoggedIn = userAuthState.isLoggedIn,
                            onNavigateToLogin = { viewModel.navigateToLogin() },
                            onNavigateToSignUp = { viewModel.navigateToSignUp() },
                            onPlayClick = { freq -> viewModel.playOrToggleTrack(freq) },
                            onFrequencyClick = { freq -> viewModel.openDetail(freq) },
                            onViewAllFavorites = { viewModel.navigateTo(Screen.LIBRARY) },
                            onContinueChallenge = {
                                val currentCh = activeChallenge
                                if (currentCh != null) {
                                    viewModel.startChallengePractice(currentCh)
                                } else {
                                    viewModel.navigateTo(Screen.CHALLENGES)
                                }
                            },
                            onStartChallenge = { viewModel.startCreateChallengeFlow() },
                            onStartFirstSession = {
                                val track = allFrequencies.firstOrNull()
                                if (track != null) {
                                    viewModel.playOrToggleTrack(track)
                                } else {
                                    viewModel.navigateTo(Screen.EXPLORE)
                                }
                            },
                            onExploreFrequencies = { viewModel.navigateTo(Screen.EXPLORE) },
                            onGoalSelected = { goal ->
                                viewModel.updateCurrentGoal(goal)
                            },
                            onUpdateProfile = { name, email, goals, reminderTime, reminderEnabled, avatarUri ->
                                viewModel.updateUserProfile(name, email, goals, reminderTime, reminderEnabled, avatarUri)
                            },
                            onLogout = { viewModel.logOutUser() }
                        )
                        Screen.AUTH_LOGIN -> UserLoginScreen(
                            onLoginSubmit = { email, password -> viewModel.logInUser(email, password) },
                            onNavigateToSignUp = { viewModel.navigateToSignUp() },
                            onContinueAsGuest = { viewModel.continueAsGuest() },
                            onForgotPasswordSubmit = { email -> viewModel.sendPasswordReset(email) },
                            errorMessage = userAuthError,
                            passwordResetMessage = userPasswordResetSuccess,
                            isLoading = userAuthLoading,
                            isFirebaseConnected = viewModel.isFirebaseConnected
                        )
                        Screen.AUTH_SIGN_UP -> UserSignUpScreen(
                            onSignUpSubmit = { email, password, confirmPassword -> viewModel.signUpUser(email, password, confirmPassword) },
                            onNavigateToLogin = { viewModel.navigateToLogin() },
                            onContinueAsGuest = { viewModel.continueAsGuest() },
                            errorMessage = userAuthError,
                            isLoading = userAuthLoading,
                            onCalculateStrength = { pass -> viewModel.calculatePasswordStrength(pass) }
                        )
                        Screen.ADMIN_LOGIN -> AdminLoginScreen(
                            onLoginSubmit = { email, password -> viewModel.loginAdmin(email, password) },
                            onBackToApp = { viewModel.navigateTo(Screen.HOME) },
                            errorMessage = adminLoginError,
                            isLoading = adminLoginLoading
                        )
                        Screen.ADMIN -> {
                            if (!isAdminAuthenticated) {
                                AdminLoginScreen(
                                    onLoginSubmit = { email, password -> viewModel.loginAdmin(email, password) },
                                    onBackToApp = { viewModel.navigateTo(Screen.HOME) },
                                    errorMessage = adminLoginError,
                                    isLoading = adminLoginLoading
                                )
                            } else {
                                AdminScreen(
                                    allFrequencies = allFrequenciesAdmin,
                                    auditLogs = adminAuditLogs,
                                    adminEmail = currentAdminEmail ?: "admin@soulsound.app",
                                    onSaveFrequency = { item -> viewModel.saveCustomFrequency(item) },
                                    onSoftDeleteFrequency = { id, name -> viewModel.softDeleteFrequency(id, name) },
                                    onRestoreFrequency = { id, name -> viewModel.restoreFrequency(id, name) },
                                    onPermanentDeleteFrequency = { id, name -> viewModel.permanentDeleteFrequency(id, name) },
                                    onRestoreDefaults = { viewModel.restoreStarterLibrary() },
                                    onLogoutAdmin = { viewModel.logoutAdmin() }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Premium Tier Presentation Dialog
        if (showPremiumDialog && premiumTargetTrack != null) {
            val track = premiumTargetTrack!!
            AlertDialog(
                onDismissRequest = { viewModel.dismissPremiumDialog() },
                title = {
                    Text(
                        text = "Premium Frequency Experience",
                        color = GoldAccent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "${track.displayHz} • ${track.name}",
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Unlock the full SoulSound library with unlimited harmonic resonance sessions, ultra-high resolution brainwave entrainment, and custom frequency layering.",
                            color = TextMuted,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Enjoying free preview session now.",
                            color = OrangePrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.dismissPremiumDialog() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = OrangePrimary,
                            contentColor = Color.Black
                        )
                    ) {
                        Text("Get Premium Access", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.dismissPremiumDialog() }) {
                        Text("Continue Preview", color = TextMuted)
                    }
                },
                containerColor = DarkCardElevated
            )
        }

        // Create Challenge Wizard Dialog
        if (showCreateChallengeWizard) {
            CreateChallengeWizard(
                allFrequencies = allFrequencies,
                onDismiss = { viewModel.dismissCreateChallengeWizard() },
                onStartChallenge = { duration, goal, freq, dailyTarget, reminder, enabled ->
                    viewModel.createNewChallenge(duration, goal, freq, dailyTarget, reminder, enabled)
                }
            )
        }

        // Milestone Celebration Dialog
        if (activeMilestoneCelebration != null) {
            val milestone = activeMilestoneCelebration!!
            AlertDialog(
                onDismissRequest = { viewModel.dismissMilestoneCelebration() },
                title = {
                    Text(
                        text = milestone.title,
                        color = GoldAccent,
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp
                    )
                },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "🔥",
                            fontSize = 32.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = milestone.message,
                            color = TextPrimary,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF26180B), RoundedCornerShape(12.dp))
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "+${milestone.pointsAwarded} SOUL POINTS EARNED",
                                color = GoldAccent,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.dismissMilestoneCelebration() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = OrangePrimary,
                            contentColor = Color.Black
                        )
                    ) {
                        Text("Keep Going", fontWeight = FontWeight.Bold)
                    }
                },
                containerColor = DarkCardElevated
            )
        }

        // Challenge Complete Grand Celebration Dialog
        if (showChallengeCompletedCelebration) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissChallengeCompletedCelebration() },
                title = {
                    Text(
                        text = "🎉 CHALLENGE COMPLETE!",
                        color = GoldAccent,
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "🏆", fontSize = 36.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "“You Showed Up For Yourself.”",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "You built a true daily practice. Your consistency and focus have created lasting neuroplastic harmony.",
                            color = TextMuted,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF2B180A), RoundedCornerShape(14.dp))
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = "+300 MASTER BONUS POINTS ⭐",
                                color = GoldAccent,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.dismissChallengeCompletedCelebration()
                            viewModel.startCreateChallengeFlow()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = OrangePrimary,
                            contentColor = Color.Black
                        )
                    ) {
                        Text("Start Next Challenge", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.dismissChallengeCompletedCelebration() }) {
                        Text("View My Journey", color = TextPrimary)
                    }
                },
                containerColor = DarkCardElevated
            )
        }

        // Recovery Day Status Dialog
        if (recoveryStatusMessage != null) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissRecoveryMessage() },
                title = {
                    Text(
                        text = "Recovery Day Shield",
                        color = OrangePrimary,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = recoveryStatusMessage!!,
                        color = TextPrimary,
                        fontSize = 13.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.dismissRecoveryMessage() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = OrangePrimary,
                            contentColor = Color.Black
                        )
                    ) {
                        Text("Got it", fontWeight = FontWeight.Bold)
                    }
                },
                containerColor = DarkCardElevated
            )
        }
    }
}
