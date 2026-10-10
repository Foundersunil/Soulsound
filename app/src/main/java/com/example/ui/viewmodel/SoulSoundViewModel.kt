package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.SoulSoundAudioEngine
import com.example.auth.AdminAuthManager
import com.example.auth.AdminAuthResult
import com.example.auth.PasswordStrength
import com.example.auth.UserAuthManager
import com.example.auth.UserAuthResult
import com.example.auth.UserAuthState
import com.example.data.db.SoulSoundDatabase
import com.example.data.model.AchievementBadge
import com.example.data.model.AdminAuditLog
import com.example.data.model.ChallengeDayItem
import com.example.data.model.ChallengeItem
import com.example.data.model.FrequencyItem
import com.example.data.model.MilestoneCelebration
import com.example.data.model.SoundMode
import com.example.data.model.UserProfileStats
import com.example.data.model.WeeklyDayActivity
import com.example.data.repository.SoulSoundRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class Screen(val title: String) {
    HOME("Home"),
    CHALLENGES("Challenges"),
    EXPLORE("Explore"),
    SEARCH("Search"),
    LIBRARY("My Library"),
    PROFILE("Profile"),
    AUTH_LOGIN("Log In"),
    AUTH_SIGN_UP("Create Account"),
    ADMIN_LOGIN("Admin Portal"),
    ADMIN("Studio Admin")
}

data class ConsistencyScoreData(
    val score: Int,
    val tier: String,
    val motivationalMessage: String
)

data class ProfileAnalytics(
    val totalListeningFormatted: String,
    val meditationFormatted: String,
    val totalSessions: Int,
    val mostPlayedFreq: String,
    val mostPlayedCategory: String,
    val longestSession: String,
    val mostActiveDay: String
)

enum class SortOption(val label: String) {
    POPULAR("Popular"),
    RECENT("Recently Added"),
    A_TO_Z("A – Z"),
    FREQ_LOW_HIGH("Frequency Low → High"),
    FREQ_HIGH_LOW("Frequency High → Low")
}

class SoulSoundViewModel(application: Application) : AndroidViewModel(application) {

    private val db = SoulSoundDatabase.getInstance(application)
    private val repository = SoulSoundRepository(db.soulSoundDao(), viewModelScope)
    val audioEngine = SoulSoundAudioEngine(viewModelScope)

    // Normal User Authentication (Firebase Auth)
    val userAuthManager = UserAuthManager(application, viewModelScope)
    val userAuthState: StateFlow<UserAuthState> = userAuthManager.userAuthState
    val userAuthError: StateFlow<String?> = userAuthManager.authError
    val userAuthLoading: StateFlow<Boolean> = userAuthManager.authLoading
    val userPasswordResetSuccess: StateFlow<String?> = userAuthManager.passwordResetSuccess
    val isFirebaseConnected: Boolean = userAuthManager.isFirebaseConnected

    // Screens and Navigation
    private val _currentScreen = MutableStateFlow(
        if (userAuthManager.userAuthState.value.isLoggedIn) Screen.HOME else Screen.AUTH_LOGIN
    )
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _selectedFrequency = MutableStateFlow<FrequencyItem?>(null)
    val selectedFrequency: StateFlow<FrequencyItem?> = _selectedFrequency.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortOption = MutableStateFlow(SortOption.POPULAR)
    val sortOption: StateFlow<SortOption> = _sortOption.asStateFlow()

    private val _selectedGoal = MutableStateFlow<String?>(null)
    val selectedGoal: StateFlow<String?> = _selectedGoal.asStateFlow()

    private val _isAdminMode = MutableStateFlow(false)
    val isAdminMode: StateFlow<Boolean> = _isAdminMode.asStateFlow()

    // Admin Authentication & Role Management
    val adminAuthManager = AdminAuthManager(application)
    val isAdminAuthenticated: StateFlow<Boolean> = adminAuthManager.isAdminAuthenticated
    val currentAdminEmail: StateFlow<String?> = adminAuthManager.currentAdminEmail
    val adminRole: StateFlow<String?> = adminAuthManager.adminRole

    val allFrequenciesAdmin: StateFlow<List<FrequencyItem>> = repository.allFrequenciesAdmin
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val adminAuditLogs: StateFlow<List<AdminAuditLog>> = repository.allAuditLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _adminLoginError = MutableStateFlow<String?>(null)
    val adminLoginError: StateFlow<String?> = _adminLoginError.asStateFlow()

    private val _adminLoginLoading = MutableStateFlow(false)
    val adminLoginLoading: StateFlow<Boolean> = _adminLoginLoading.asStateFlow()

    private val _showPremiumDialog = MutableStateFlow(false)
    val showPremiumDialog: StateFlow<Boolean> = _showPremiumDialog.asStateFlow()

    private val _premiumTargetTrack = MutableStateFlow<FrequencyItem?>(null)
    val premiumTargetTrack: StateFlow<FrequencyItem?> = _premiumTargetTrack.asStateFlow()

    // Data streams from Repository
    val allFrequencies: StateFlow<List<FrequencyItem>> = repository.allFrequencies
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val featuredFrequencies: StateFlow<List<FrequencyItem>> = repository.featuredFrequencies
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteIds: StateFlow<Set<String>> = repository.favoriteIds
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val recentHistory = repository.recentHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val playlists = repository.allPlaylists
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Challenge Streams
    val activeChallenge: StateFlow<ChallengeItem?> = repository.activeChallenge
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allChallenges: StateFlow<List<ChallengeItem>> = repository.allChallenges
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userProfile: StateFlow<UserProfileStats> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserProfileStats())

    private val _challengeDays = MutableStateFlow<List<ChallengeDayItem>>(emptyList())
    val challengeDays: StateFlow<List<ChallengeDayItem>> = _challengeDays.asStateFlow()

    private val _activeMilestoneCelebration = MutableStateFlow<MilestoneCelebration?>(null)
    val activeMilestoneCelebration: StateFlow<MilestoneCelebration?> = _activeMilestoneCelebration.asStateFlow()

    private val _showChallengeCompletedCelebration = MutableStateFlow(false)
    val showChallengeCompletedCelebration: StateFlow<Boolean> = _showChallengeCompletedCelebration.asStateFlow()

    private val _showCreateChallengeWizard = MutableStateFlow(false)
    val showCreateChallengeWizard: StateFlow<Boolean> = _showCreateChallengeWizard.asStateFlow()

    private val _recoveryStatusMessage = MutableStateFlow<String?>(null)
    val recoveryStatusMessage: StateFlow<String?> = _recoveryStatusMessage.asStateFlow()

    init {
        viewModelScope.launch {
            userAuthManager.userAuthState.collectLatest { authState ->
                if (authState.isLoggedIn && !authState.uid.isNullOrBlank()) {
                    repository.setActiveUser(authState.uid, authState.email, authState.displayName)
                } else {
                    repository.clearActiveUser()
                }
            }
        }

        viewModelScope.launch {
            repository.activeChallenge.collectLatest { challenge ->
                if (challenge != null) {
                    repository.getDaysForChallenge(challenge.id).collectLatest { days ->
                        _challengeDays.value = days
                    }
                } else {
                    _challengeDays.value = emptyList()
                }
            }
        }
    }

    // Audio Engine Reactive State
    val isPlaying = audioEngine.isPlaying
    val currentTrack = audioEngine.currentTrack
    val currentSoundMode = audioEngine.currentSoundMode
    val volume = audioEngine.volume
    val progressMs = audioEngine.progressMs
    val totalDurationMs = audioEngine.totalDurationMs
    val sleepTimerSeconds = audioEngine.sleepTimerSeconds
    val liveAmplitude = audioEngine.liveAmplitude

    // Filtered & Sorted Frequencies for Explore Screen
    val filteredExploreFrequencies = combine(
        allFrequencies,
        _selectedCategory,
        _sortOption
    ) { list, category, sort ->
        var result = if (category == "All") {
            list
        } else {
            list.filter { it.category.equals(category, ignoreCase = true) || it.tags.any { t -> t.equals(category, ignoreCase = true) } }
        }

        when (sort) {
            SortOption.POPULAR -> result.sortedByDescending { if (it.isFeatured) 1 else 0 }
            SortOption.RECENT -> result.sortedByDescending { it.createdAt }
            SortOption.A_TO_Z -> result.sortedBy { it.name }
            SortOption.FREQ_LOW_HIGH -> result.sortedBy { it.hz }
            SortOption.FREQ_HIGH_LOW -> result.sortedByDescending { it.hz }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Global Search Results
    val searchResults = combine(
        allFrequencies,
        _searchQuery
    ) { list, query ->
        val q = query.trim().lowercase()
        if (q.isEmpty()) return@combine emptyList<FrequencyItem>()

        val hzQuery = q.replace("hz", "").trim().toFloatOrNull()

        list.filter { item ->
            // Search by Frequency number
            (hzQuery != null && (item.hz == hzQuery || item.displayHz.lowercase().contains(q))) ||
            item.displayHz.lowercase().contains(q) ||
            item.name.lowercase().contains(q) ||
            item.shortDesc.lowercase().contains(q) ||
            item.longDesc.lowercase().contains(q) ||
            item.category.lowercase().contains(q) ||
            item.tags.any { it.lowercase().contains(q) } ||
            item.intendedExperience.any { it.lowercase().contains(q) } ||
            // Goal keyword mapping
            (q.contains("money") && item.tags.any { it.equals("Money", true) || it.equals("Abundance", true) }) ||
            (q.contains("sleep") && (item.category.equals("Sleep", true) || item.tags.any { it.equals("Sleep", true) })) ||
            (q.contains("focus") && (item.category.equals("Focus", true) || item.tags.any { it.equals("Focus", true) })) ||
            (q.contains("meditat") && (item.category.equals("Meditation", true) || item.tags.any { it.equals("Meditation", true) }))
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Profile Consistency Score Flow
    val consistencyData = combine(
        userProfile,
        activeChallenge,
        recentHistory
    ) { profile, activeCh, history ->
        val streakPts = (profile.currentStreak.coerceAtMost(14) / 14f) * 35f
        val daysPts = (profile.totalDaysCompleted.coerceAtMost(30) / 30f) * 25f
        val challengePts = if (activeCh != null && activeCh.durationDays > 0) {
            ((activeCh.completedDays.toFloat() / activeCh.durationDays) * 20f).coerceAtMost(20f)
        } else if (profile.completedChallengesCount > 0) {
            15f
        } else {
            0f
        }
        val practicePts = (profile.totalPracticeMinutes.coerceAtMost(300) / 300f) * 20f
        val isNewUser = profile.totalPracticeMinutes == 0 && profile.totalDaysCompleted == 0 && history.isEmpty()
        val score = if (isNewUser) 0 else (streakPts + daysPts + challengePts + practicePts).toInt().coerceIn(0, 100)

        val tier = when (score) {
            in 81..100 -> "Strong"
            in 61..80 -> "Very Good"
            in 31..60 -> "Good"
            else -> "Getting Started"
        }

        val message = when {
            isNewUser -> "Your journey starts with one session."
            profile.completedChallengesCount > 0 && activeCh == null -> "You completed your challenge. Ready for the next level?"
            profile.currentStreak >= 7 -> "🔥 You're on a strong streak. Keep the rhythm going."
            profile.currentStreak >= 2 || score >= 31 -> "You're building momentum. Keep going."
            else -> "Your frequency journey is waiting for you. Start a session today."
        }

        ConsistencyScoreData(score = score, tier = tier, motivationalMessage = message)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ConsistencyScoreData(0, "Getting Started", "Your journey starts with one session."))

    // Weekly Activity Flow (Past 7 Days Mon-Sun)
    val weeklyActivity = combine(
        recentHistory,
        userProfile
    ) { _, profile ->
        val calendar = java.util.Calendar.getInstance()
        val dayNames = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")
        val currentDayOfWeek = calendar.get(java.util.Calendar.DAY_OF_WEEK)
        val dayIndex = when (currentDayOfWeek) {
            java.util.Calendar.MONDAY -> 0
            java.util.Calendar.TUESDAY -> 1
            java.util.Calendar.WEDNESDAY -> 2
            java.util.Calendar.THURSDAY -> 3
            java.util.Calendar.FRIDAY -> 4
            java.util.Calendar.SATURDAY -> 5
            else -> 6
        }

        dayNames.mapIndexed { index, name ->
            val isToday = index == dayIndex
            val isPast = index <= dayIndex
            val intensity = when {
                !isPast -> 0
                isToday -> if (profile.totalPracticeMinutes > 0) 3 else 0
                index >= dayIndex - profile.currentStreak -> when {
                    profile.currentStreak >= 5 -> 3
                    profile.currentStreak >= 3 -> 2
                    else -> 1
                }
                else -> if (profile.totalDaysCompleted > 0 && (index % 2 == 0)) 1 else 0
            }
            WeeklyDayActivity(
                dayName = name,
                dateMillis = System.currentTimeMillis() - (dayIndex - index) * 86400000L,
                minutesListened = intensity * 15,
                intensity = intensity
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Achievements Flow
    val achievements = combine(
        userProfile,
        recentHistory,
        allChallenges
    ) { profile, history, challenges ->
        listOf(
            AchievementBadge(
                id = "first_session",
                icon = "🌱",
                title = "First Session",
                description = "Completed first listening session",
                requirement = "Complete your first session",
                isUnlocked = profile.totalDaysCompleted >= 1 || profile.totalPracticeMinutes > 0 || history.isNotEmpty(),
                progress = if (profile.totalDaysCompleted >= 1 || profile.totalPracticeMinutes > 0 || history.isNotEmpty()) 1f else 0f
            ),
            AchievementBadge(
                id = "streak_7",
                icon = "🔥",
                title = "7 Day Streak",
                description = "Maintained a 7-day streak",
                requirement = "Maintain a 7-day practice streak",
                isUnlocked = profile.longestStreak >= 7 || profile.currentStreak >= 7,
                progress = (profile.currentStreak.toFloat() / 7f).coerceIn(0f, 1f)
            ),
            AchievementBadge(
                id = "meditation_starter",
                icon = "🧘",
                title = "Meditation Starter",
                description = "Completed 5 meditation sessions",
                requirement = "Complete 5 meditation practices",
                isUnlocked = profile.totalDaysCompleted >= 5 || profile.totalPracticeMinutes >= 150,
                progress = (profile.totalDaysCompleted.toFloat() / 5f).coerceIn(0f, 1f)
            ),
            AchievementBadge(
                id = "sound_explorer",
                icon = "🎧",
                title = "Sound Explorer",
                description = "Listened to 10 different tracks",
                requirement = "Explore 10 unique frequencies or tracks",
                isUnlocked = history.map { it.frequencyId }.distinct().size >= 10 || profile.soulPoints >= 200,
                progress = (history.map { it.frequencyId }.distinct().size.toFloat() / 10f).coerceIn(0f, 1f)
            ),
            AchievementBadge(
                id = "frequency_explorer",
                icon = "⚡",
                title = "Frequency Explorer",
                description = "Explored 5 different frequencies",
                requirement = "Listen to 5 unique resonant frequencies",
                isUnlocked = history.map { it.frequencyId }.distinct().size >= 5 || profile.soulPoints >= 100,
                progress = (history.map { it.frequencyId }.distinct().size.toFloat() / 5f).coerceIn(0f, 1f)
            ),
            AchievementBadge(
                id = "challenge_complete",
                icon = "🏆",
                title = "Challenge Complete",
                description = "Completed a full challenge",
                requirement = "Finish any active consistency challenge",
                isUnlocked = profile.completedChallengesCount >= 1 || challenges.any { it.status == "completed" },
                progress = if (profile.completedChallengesCount >= 1) 1f else 0f
            ),
            AchievementBadge(
                id = "master_30",
                icon = "🌟",
                title = "30 Day Master",
                description = "Completed a 30-day challenge",
                requirement = "Complete a 30-day challenge to unlock",
                isUnlocked = challenges.any { it.status == "completed" && it.durationDays >= 30 } || profile.longestStreak >= 30,
                progress = (profile.longestStreak.toFloat() / 30f).coerceIn(0f, 1f)
            )
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Profile Favorites Preview (top 4)
    val profileFavorites = combine(allFrequencies, favoriteIds) { freqs, favs ->
        freqs.filter { favs.contains(it.id) }.take(4)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Profile Recently Played Preview (top 4)
    val profileRecentlyPlayed = combine(allFrequencies, recentHistory) { freqs, hist ->
        hist.mapNotNull { h -> freqs.find { it.id == h.frequencyId } }.distinctBy { it.id }.take(4)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Profile Listening Analytics
    val profileAnalytics = combine(userProfile, recentHistory, allFrequencies) { profile, history, freqs ->
        val totalMin = profile.totalPracticeMinutes
        val listeningFmt = if (totalMin >= 60) "${totalMin / 60}h ${totalMin % 60}m" else "${totalMin}m"
        val medMin = (totalMin * 0.65).toInt()
        val medFmt = if (medMin >= 60) "${medMin / 60}h ${medMin % 60}m" else "${medMin}m"
        val sessions = maxOf(profile.totalDaysCompleted * 2, history.size, if (totalMin > 0) 1 else 0)

        val mostPlayedFreqId = history.groupBy { it.frequencyId }.maxByOrNull { it.value.size }?.key
        val mostPlayedFreqName = freqs.find { it.id == mostPlayedFreqId }?.let { "${it.hz.toInt()} Hz" } ?: "528 Hz"
        val mostPlayedCat = freqs.find { it.id == mostPlayedFreqId }?.category ?: "Meditation"

        ProfileAnalytics(
            totalListeningFormatted = listeningFmt,
            meditationFormatted = medFmt,
            totalSessions = sessions,
            mostPlayedFreq = mostPlayedFreqName,
            mostPlayedCategory = mostPlayedCat,
            longestSession = if (totalMin > 0) "45 min" else "0 min",
            mostActiveDay = if (totalMin > 0) "Thursday" else "Today"
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProfileAnalytics("0m", "0m", 0, "528 Hz", "Meditation", "0 min", "Today"))

    fun navigateTo(screen: Screen) {
        if (screen == Screen.ADMIN) {
            if (adminAuthManager.hasAdminPrivileges()) {
                _currentScreen.value = Screen.ADMIN
            } else {
                _currentScreen.value = Screen.ADMIN_LOGIN
            }
        } else {
            _currentScreen.value = screen
        }
    }

    fun openDetail(frequency: FrequencyItem) {
        _selectedFrequency.value = frequency
        audioEngine.selectTrack(frequency)
    }

    fun selectTrack(frequency: FrequencyItem) {
        audioEngine.selectTrack(frequency)
    }

    fun stopPlayback() {
        audioEngine.resetSession()
    }

    fun closeDetail() {
        _selectedFrequency.value = null
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setSortOption(sort: SortOption) {
        _sortOption.value = sort
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun filterByGoal(goal: String) {
        _selectedGoal.value = goal
        _selectedCategory.value = when (goal.lowercase()) {
            "focus" -> "Focus"
            "meditate" -> "Meditation"
            "relax" -> "Relaxation"
            "sleep" -> "Sleep"
            "energy" -> "Energy"
            "balance" -> "Balance"
            "money" -> "Money"
            "manifest" -> "Manifestation"
            else -> "All"
        }
        _currentScreen.value = Screen.EXPLORE
    }

    fun playTrack(track: FrequencyItem, targetDurationMinutes: Int? = null) {
        if (track.isPremium) {
            _premiumTargetTrack.value = track
            _showPremiumDialog.value = true
            // Also play for demo preview so the user experiences the tone quality
        }
        audioEngine.playTrack(track, targetDurationMinutes)
        viewModelScope.launch {
            repository.recordListening(
                frequencyId = track.id,
                progressMs = 0L,
                totalDurationMs = track.durationMinutes * 60 * 1000L
            )
        }
    }

    fun playOrToggleTrack(track: FrequencyItem, targetDurationMinutes: Int? = null) {
        if (currentTrack.value?.id == track.id) {
            togglePlayPause()
        } else {
            playTrack(track, targetDurationMinutes)
        }
    }

    fun dismissPremiumDialog() {
        _showPremiumDialog.value = false
        _premiumTargetTrack.value = null
    }

    fun togglePlayPause() {
        audioEngine.togglePlayPause()
    }

    fun seekTo(progressMs: Long) {
        audioEngine.seekTo(progressMs)
    }

    fun setSoundMode(mode: SoundMode) {
        audioEngine.setSoundMode(mode)
    }

    fun setVolume(vol: Float) {
        audioEngine.setVolume(vol)
    }

    fun setTargetDuration(minutes: Int) {
        audioEngine.setDuration(minutes)
    }

    fun setSleepTimer(minutes: Int?) {
        audioEngine.setSleepTimer(minutes)
    }

    fun toggleFavorite(frequencyId: String) {
        viewModelScope.launch {
            val isFav = favoriteIds.value.contains(frequencyId)
            repository.toggleFavorite(frequencyId, isFav)
        }
    }

    fun toggleAdminMode() {
        _isAdminMode.value = !_isAdminMode.value
    }

    fun loginAdmin(emailInput: String, passwordInput: String) {
        _adminLoginLoading.value = true
        _adminLoginError.value = null
        viewModelScope.launch {
            val result = adminAuthManager.authenticate(emailInput, passwordInput)
            _adminLoginLoading.value = false
            when (result) {
                is AdminAuthResult.Success -> {
                    _adminLoginError.value = null
                    _currentScreen.value = Screen.ADMIN
                }
                is AdminAuthResult.Error -> {
                    _adminLoginError.value = result.message
                }
            }
        }
    }

    fun logoutAdmin() {
        adminAuthManager.logout()
        _currentScreen.value = Screen.HOME
    }

    fun attemptOpenAdmin() {
        if (adminAuthManager.hasAdminPrivileges()) {
            _currentScreen.value = Screen.ADMIN
        } else {
            _currentScreen.value = Screen.ADMIN_LOGIN
        }
    }

    fun saveCustomFrequency(item: FrequencyItem) {
        val email = currentAdminEmail.value ?: "admin@soulsound.app"
        viewModelScope.launch {
            repository.saveFrequency(item, email)
        }
    }

    fun softDeleteFrequency(id: String, frequencyName: String = "") {
        val email = currentAdminEmail.value ?: "admin@soulsound.app"
        viewModelScope.launch {
            repository.softDeleteFrequency(id, frequencyName, email)
        }
    }

    fun restoreFrequency(id: String, frequencyName: String = "") {
        val email = currentAdminEmail.value ?: "admin@soulsound.app"
        viewModelScope.launch {
            repository.restoreFrequency(id, frequencyName, email)
        }
    }

    fun permanentDeleteFrequency(id: String, frequencyName: String = "") {
        val email = currentAdminEmail.value ?: "admin@soulsound.app"
        viewModelScope.launch {
            repository.deleteFrequency(id, frequencyName, email)
            if (_selectedFrequency.value?.id == id) {
                _selectedFrequency.value = null
            }
        }
    }

    fun createPlaylist(title: String, description: String = "") {
        viewModelScope.launch {
            repository.createPlaylist(title, description)
        }
    }

    fun deletePlaylist(playlistId: String) {
        viewModelScope.launch {
            repository.deletePlaylist(playlistId)
        }
    }

    fun addTrackToPlaylist(playlistId: String, frequencyId: String) {
        viewModelScope.launch {
            repository.addTrackToPlaylist(playlistId, frequencyId)
        }
    }

    fun restoreStarterLibrary() {
        viewModelScope.launch {
            repository.seedDefaults()
        }
    }

    // Challenge Actions
    fun startCreateChallengeFlow() {
        _showCreateChallengeWizard.value = true
    }

    fun dismissCreateChallengeWizard() {
        _showCreateChallengeWizard.value = false
    }

    fun createNewChallenge(
        durationDays: Int,
        goal: String,
        frequency: FrequencyItem,
        dailyTargetMinutes: Int,
        reminderTime: String = "08:00 PM",
        reminderEnabled: Boolean = true
    ) {
        viewModelScope.launch {
            repository.createChallenge(
                durationDays = durationDays,
                goal = goal,
                frequency = frequency,
                dailyTargetMinutes = dailyTargetMinutes,
                reminderTime = reminderTime,
                reminderEnabled = reminderEnabled
            )
            _showCreateChallengeWizard.value = false
            _currentScreen.value = Screen.CHALLENGES
        }
    }

    fun startChallengePractice(challenge: ChallengeItem) {
        val freq = allFrequencies.value.find { it.id == challenge.frequencyId }
            ?: FrequencyItem(
                id = challenge.frequencyId,
                hz = challenge.frequencyHz,
                name = challenge.frequencyName,
                shortDesc = "Daily Challenge Practice",
                longDesc = "Dedicated frequency session for consistency and mindfulness.",
                category = challenge.category,
                tags = listOf(challenge.goal, "Challenge"),
                intendedExperience = listOf(challenge.goal),
                suggestedContext = "Daily practice routine",
                durationMinutes = challenge.dailyTargetMinutes
            )
        openDetail(freq)
        playTrack(freq, challenge.dailyTargetMinutes)
    }

    fun completeTodayPractice() {
        val challenge = activeChallenge.value ?: return
        viewModelScope.launch {
            val milestone = repository.completeTodayPractice(challenge.id)
            if (milestone != null) {
                _activeMilestoneCelebration.value = milestone
                if (milestone.title.contains("Challenge Complete")) {
                    _showChallengeCompletedCelebration.value = true
                }
            }
        }
    }

    fun useRecoveryDay() {
        val challenge = activeChallenge.value ?: return
        viewModelScope.launch {
            val success = repository.useRecoveryDay(challenge.id)
            if (success) {
                _recoveryStatusMessage.value = "Recovery day applied! Your streak is protected."
            } else {
                _recoveryStatusMessage.value = "No recovery days available for this challenge."
            }
        }
    }

    fun dismissRecoveryMessage() {
        _recoveryStatusMessage.value = null
    }

    fun abandonActiveChallenge() {
        val challenge = activeChallenge.value ?: return
        viewModelScope.launch {
            repository.abandonChallenge(challenge.id)
        }
    }

    fun dismissMilestoneCelebration() {
        _activeMilestoneCelebration.value = null
    }

    fun dismissChallengeCompletedCelebration() {
        _showChallengeCompletedCelebration.value = false
    }

    // Profile Management Actions
    fun updateUserProfile(
        userName: String,
        userEmail: String,
        preferredGoals: List<String>,
        reminderTime: String,
        reminderEnabled: Boolean,
        avatarUri: String? = null
    ) {
        viewModelScope.launch {
            val current = userProfile.value
            val updated = current.copy(
                userName = userName.ifBlank { current.userName },
                userEmail = userEmail.ifBlank { current.userEmail },
                avatarUri = avatarUri ?: current.avatarUri,
                preferredGoalsCsv = preferredGoals.joinToString("|"),
                reminderTime = reminderTime,
                reminderEnabled = reminderEnabled
            )
            repository.updateUserProfile(updated)
        }
    }

    fun updateCurrentGoal(goal: String) {
        viewModelScope.launch {
            val current = userProfile.value
            val goals = current.preferredGoals.toMutableList()
            if (!goals.contains(goal)) {
                goals.add(0, goal)
            } else {
                goals.remove(goal)
                goals.add(0, goal)
            }
            val updated = current.copy(preferredGoalsCsv = goals.joinToString("|"))
            repository.updateUserProfile(updated)
        }
    }

    // Normal User Authentication Actions
    fun signUpUser(email: String, pass: String, confirm: String) {
        viewModelScope.launch {
            val result = userAuthManager.signUp(email, pass, confirm)
            if (result is UserAuthResult.Success) {
                audioEngine.resetSession()
                _selectedFrequency.value = null
                repository.setActiveUser(result.uid, result.email, email.substringBefore('@').replaceFirstChar { it.uppercase() })
                _currentScreen.value = Screen.HOME
            }
        }
    }

    fun logInUser(email: String, pass: String) {
        viewModelScope.launch {
            val result = userAuthManager.logIn(email, pass)
            if (result is UserAuthResult.Success) {
                audioEngine.resetSession()
                _selectedFrequency.value = null
                repository.setActiveUser(result.uid, result.email, email.substringBefore('@').replaceFirstChar { it.uppercase() })
                _currentScreen.value = Screen.HOME
            }
        }
    }

    fun sendPasswordReset(email: String) {
        viewModelScope.launch {
            userAuthManager.sendPasswordReset(email)
        }
    }

    fun logOutUser() {
        audioEngine.resetSession()
        _selectedFrequency.value = null
        userAuthManager.signOut()
        viewModelScope.launch {
            repository.clearActiveUser()
        }
        _currentScreen.value = Screen.AUTH_LOGIN
    }

    fun resetOrLogoutProfile() {
        logOutUser()
    }

    fun continueAsGuest() {
        audioEngine.resetSession()
        _selectedFrequency.value = null
        _currentScreen.value = Screen.HOME
    }

    fun navigateToLogin() {
        userAuthManager.clearErrors()
        _currentScreen.value = Screen.AUTH_LOGIN
    }

    fun navigateToSignUp() {
        userAuthManager.clearErrors()
        _currentScreen.value = Screen.AUTH_SIGN_UP
    }

    fun calculatePasswordStrength(pass: String): PasswordStrength {
        return userAuthManager.calculatePasswordStrength(pass)
    }

    override fun onCleared() {
        super.onCleared()
        audioEngine.release()
    }
}
