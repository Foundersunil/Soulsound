package com.example.ui

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
import com.example.ui.components.MiniPlayer
import com.example.ui.components.TopHeader
import com.example.ui.screens.AdminScreen
import com.example.ui.screens.DetailScreen
import com.example.ui.screens.ExploreScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.SearchScreen
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        if (selectedFrequency != null) {
            val freq = selectedFrequency!!
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
        } else {
            Scaffold(
                containerColor = DarkBg,
                topBar = {
                    TopHeader(
                        onSearchClick = { viewModel.navigateTo(Screen.SEARCH) },
                        onAdminClick = { viewModel.navigateTo(Screen.ADMIN) },
                        isAdminActive = currentScreen == Screen.ADMIN,
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
                            onExploreClick = { viewModel.navigateTo(Screen.EXPLORE) },
                            onGoalSelected = { goal -> viewModel.filterByGoal(goal) },
                            onFrequencyClick = { freq -> viewModel.openDetail(freq) },
                            onPlayClick = { freq -> viewModel.playTrack(freq) },
                            onFavoriteToggle = { freqId -> viewModel.toggleFavorite(freqId) }
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
                            onPlayClick = { freq -> viewModel.playTrack(freq) },
                            onFavoriteToggle = { freqId -> viewModel.toggleFavorite(freqId) }
                        )
                        Screen.SEARCH -> SearchScreen(
                            query = searchQuery,
                            results = searchResults,
                            currentTrack = currentTrack,
                            isPlaying = isPlaying,
                            onQueryChange = { q -> viewModel.setSearchQuery(q) },
                            onFrequencyClick = { freq -> viewModel.openDetail(freq) },
                            onPlayClick = { freq -> viewModel.playTrack(freq) }
                        )
                        Screen.LIBRARY -> LibraryScreen(
                            allFrequencies = allFrequencies,
                            favoriteIds = favoriteIds,
                            history = history,
                            playlists = playlists,
                            currentTrack = currentTrack,
                            isPlaying = isPlaying,
                            onFrequencyClick = { freq -> viewModel.openDetail(freq) },
                            onPlayClick = { freq -> viewModel.playTrack(freq) },
                            onFavoriteToggle = { freqId -> viewModel.toggleFavorite(freqId) },
                            onCreatePlaylist = { title -> viewModel.createPlaylist(title) },
                            onDeletePlaylist = { id -> viewModel.deletePlaylist(id) }
                        )
                        Screen.ADMIN -> AdminScreen(
                            allFrequencies = allFrequencies,
                            isAdminMode = isAdminMode,
                            onToggleAdminMode = { viewModel.toggleAdminMode() },
                            onSaveFrequency = { item -> viewModel.saveCustomFrequency(item) },
                            onDeleteFrequency = { id -> viewModel.deleteFrequency(id) },
                            onRestoreDefaults = { viewModel.restoreStarterLibrary() }
                        )
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
    }
}
