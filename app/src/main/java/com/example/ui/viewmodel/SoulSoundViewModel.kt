package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.SoulSoundAudioEngine
import com.example.data.db.SoulSoundDatabase
import com.example.data.model.FrequencyItem
import com.example.data.model.SoundMode
import com.example.data.repository.SoulSoundRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class Screen(val title: String) {
    HOME("Home"),
    EXPLORE("Explore"),
    SEARCH("Search"),
    LIBRARY("My Library"),
    ADMIN("Studio Admin")
}

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

    // Screens and Navigation
    private val _currentScreen = MutableStateFlow(Screen.HOME)
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

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun openDetail(frequency: FrequencyItem) {
        _selectedFrequency.value = frequency
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

    fun saveCustomFrequency(item: FrequencyItem) {
        viewModelScope.launch {
            repository.saveFrequency(item)
        }
    }

    fun deleteFrequency(id: String) {
        viewModelScope.launch {
            repository.deleteFrequency(id)
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

    override fun onCleared() {
        super.onCleared()
        audioEngine.release()
    }
}
