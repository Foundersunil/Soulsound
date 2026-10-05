package com.example.data.repository

import com.example.data.db.ChallengeDayEntity
import com.example.data.db.ChallengeEntity
import com.example.data.db.FavoriteEntity
import com.example.data.db.FrequencyEntity
import com.example.data.db.ListeningHistoryEntity
import com.example.data.db.PlaylistEntity
import com.example.data.db.PlaylistTrackEntity
import com.example.data.db.SoulSoundDao
import com.example.data.db.UserProfileEntity
import com.example.data.model.ChallengeDayItem
import com.example.data.model.ChallengeItem
import com.example.data.model.FrequencyItem
import com.example.data.model.MilestoneCelebration
import com.example.data.model.UserProfileStats
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class SoulSoundRepository(
    private val dao: SoulSoundDao,
    private val externalScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {

    init {
        externalScope.launch {
            checkAndSeedInitialData()
        }
    }

    val allFrequencies: Flow<List<FrequencyItem>> =
        dao.getAllFrequencies().map { list -> list.map { it.toDomain() } }

    val allFrequenciesAdmin: Flow<List<FrequencyItem>> =
        dao.getAllFrequenciesAdmin().map { list -> list.map { it.toDomain() } }

    val featuredFrequencies: Flow<List<FrequencyItem>> =
        dao.getFeaturedFrequencies().map { list -> list.map { it.toDomain() } }

    val allAuditLogs: Flow<List<com.example.data.model.AdminAuditLog>> =
        dao.getAllAuditLogs().map { list -> list.map { it.toDomain() } }

    val favoriteIds: Flow<Set<String>> =
        dao.getAllFavorites().map { list -> list.map { it.frequencyId }.toSet() }

    val recentHistory: Flow<List<ListeningHistoryEntity>> =
        dao.getRecentHistory()

    val allPlaylists: Flow<List<PlaylistEntity>> =
        dao.getAllPlaylists()

    fun getFrequencyById(id: String): Flow<FrequencyItem?> =
        dao.getFrequencyById(id).map { it?.toDomain() }

    fun isFavorite(id: String): Flow<Boolean> =
        dao.isFavorite(id)

    suspend fun toggleFavorite(frequencyId: String, currentStatus: Boolean) {
        if (currentStatus) {
            dao.deleteFavorite(frequencyId)
        } else {
            dao.insertFavorite(FavoriteEntity(frequencyId = frequencyId))
        }
    }

    suspend fun recordListening(frequencyId: String, progressMs: Long, totalDurationMs: Long) {
        dao.recordHistory(
            ListeningHistoryEntity(
                frequencyId = frequencyId,
                progressMs = progressMs,
                totalDurationMs = totalDurationMs
            )
        )
    }

    suspend fun saveFrequency(item: FrequencyItem, adminEmail: String = "admin@soulsound.app") {
        dao.insertFrequency(FrequencyEntity.fromDomain(item))
        recordAuditLog(
            action = "CREATE_OR_UPDATE",
            resourceType = "FREQUENCY",
            resourceId = item.id,
            resourceName = "${item.displayHz} - ${item.name}",
            adminEmail = adminEmail
        )
    }

    suspend fun softDeleteFrequency(id: String, frequencyName: String = "", adminEmail: String = "admin@soulsound.app") {
        dao.softDeleteFrequency(id = id, deletedAt = System.currentTimeMillis(), deletedBy = adminEmail)
        recordAuditLog(
            action = "SOFT_DELETE",
            resourceType = "FREQUENCY",
            resourceId = id,
            resourceName = frequencyName.ifBlank { id },
            adminEmail = adminEmail
        )
    }

    suspend fun restoreFrequency(id: String, frequencyName: String = "", adminEmail: String = "admin@soulsound.app") {
        dao.restoreFrequency(id)
        recordAuditLog(
            action = "RESTORE",
            resourceType = "FREQUENCY",
            resourceId = id,
            resourceName = frequencyName.ifBlank { id },
            adminEmail = adminEmail
        )
    }

    suspend fun deleteFrequency(id: String, frequencyName: String = "", adminEmail: String = "admin@soulsound.app") {
        dao.deleteFrequency(id)
        recordAuditLog(
            action = "PERMANENT_DELETE",
            resourceType = "FREQUENCY",
            resourceId = id,
            resourceName = frequencyName.ifBlank { id },
            adminEmail = adminEmail
        )
    }

    suspend fun recordAuditLog(
        action: String,
        resourceType: String,
        resourceId: String,
        resourceName: String,
        adminEmail: String = "admin@soulsound.app"
    ) {
        dao.insertAuditLog(
            com.example.data.db.AdminAuditLogEntity(
                logId = "log_${System.currentTimeMillis()}_${java.util.UUID.randomUUID().toString().take(6)}",
                adminUserId = "admin_root",
                adminEmail = adminEmail,
                action = action,
                resourceType = resourceType,
                resourceId = resourceId,
                resourceName = resourceName,
                timestampMillis = System.currentTimeMillis()
            )
        )
    }

    suspend fun createPlaylist(title: String, description: String = ""): String {
        val id = "pl_${System.currentTimeMillis()}"
        dao.insertPlaylist(PlaylistEntity(playlistId = id, title = title, description = description))
        return id
    }

    suspend fun deletePlaylist(playlistId: String) {
        dao.deletePlaylist(playlistId)
    }

    suspend fun addTrackToPlaylist(playlistId: String, frequencyId: String) {
        dao.addTrackToPlaylist(PlaylistTrackEntity(playlistId = playlistId, frequencyId = frequencyId))
    }

    suspend fun removeTrackFromPlaylist(playlistId: String, frequencyId: String) {
        dao.removeTrackFromPlaylist(playlistId, frequencyId)
    }

    fun getTracksForPlaylist(playlistId: String): Flow<List<PlaylistTrackEntity>> {
        return dao.getTracksForPlaylist(playlistId)
    }

    // Challenges
    val activeChallenge: Flow<ChallengeItem?> =
        dao.getActiveChallenge().map { it?.toDomain() }

    val allChallenges: Flow<List<ChallengeItem>> =
        dao.getAllChallenges().map { list -> list.map { it.toDomain() } }

    private val _activeUserId = MutableStateFlow<String?>("soul_user")
    val activeUserId: StateFlow<String?> = _activeUserId.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val userProfile: Flow<UserProfileStats> = _activeUserId.flatMapLatest { uid ->
        if (uid.isNullOrBlank()) {
            flowOf(
                UserProfileStats(
                    userId = "guest",
                    userName = "Guest Traveler",
                    userEmail = "",
                    soulPoints = 0,
                    totalPracticeMinutes = 0,
                    totalDaysCompleted = 0,
                    currentStreak = 0,
                    longestStreak = 0,
                    completedChallengesCount = 0
                )
            )
        } else {
            dao.getUserProfileById(uid).map {
                it?.toDomain() ?: UserProfileStats(
                    userId = uid,
                    userName = "Soul Traveler",
                    userEmail = "",
                    soulPoints = 0,
                    totalPracticeMinutes = 0,
                    totalDaysCompleted = 0,
                    currentStreak = 0,
                    longestStreak = 0,
                    completedChallengesCount = 0
                )
            }
        }
    }

    suspend fun setActiveUser(uid: String?, email: String?, displayName: String?) {
        _activeUserId.value = uid
        if (!uid.isNullOrBlank()) {
            val existing = dao.getUserProfileById(uid).first()
            if (existing == null) {
                val newProfile = UserProfileEntity(
                    userId = uid,
                    userName = displayName ?: email?.substringBefore('@')?.replaceFirstChar { it.uppercase() } ?: "Soul Traveler",
                    userEmail = email ?: "",
                    preferredGoalsCsv = "Meditation|Focus|Sleep",
                    reminderTime = "08:00 PM",
                    reminderEnabled = true,
                    soulPoints = 0,
                    totalPracticeMinutes = 0,
                    totalDaysCompleted = 0,
                    currentStreak = 0,
                    longestStreak = 0,
                    completedChallengesCount = 0
                )
                dao.insertOrUpdateUserProfile(newProfile)
            }
        }
    }

    suspend fun clearActiveUser() {
        _activeUserId.value = null
    }

    suspend fun updateUserProfile(profile: UserProfileStats) {
        val uid = _activeUserId.value ?: profile.userId
        val finalProfile = profile.copy(userId = uid)
        dao.insertOrUpdateUserProfile(com.example.data.db.UserProfileEntity.fromDomain(finalProfile))
    }

    fun getDaysForChallenge(challengeId: String): Flow<List<ChallengeDayItem>> {
        return dao.getDaysForChallenge(challengeId).map { dayEntities ->
            val challenge = dao.getChallengeById(challengeId).first()?.toDomain()
            val currentDay = challenge?.currentDay ?: 1
            dayEntities.map { entity ->
                ChallengeDayItem(
                    dayNumber = entity.dayNumber,
                    targetMinutes = entity.targetMinutes,
                    listenedMinutes = entity.listenedMinutes,
                    isCompleted = entity.isCompleted,
                    completedAtMillis = entity.completedAtMillis,
                    isCurrent = entity.dayNumber == currentDay && !entity.isCompleted,
                    isFuture = entity.dayNumber > currentDay,
                    isMissed = entity.dayNumber < currentDay && !entity.isCompleted
                )
            }
        }
    }

    suspend fun createChallenge(
        durationDays: Int,
        goal: String,
        frequency: FrequencyItem,
        dailyTargetMinutes: Int,
        reminderTime: String = "08:00 PM",
        reminderEnabled: Boolean = true
    ): String {
        val challengeId = "ch_${System.currentTimeMillis()}"
        val now = System.currentTimeMillis()
        val dayMillis = 24L * 60 * 60 * 1000
        val expectedEnd = now + (durationDays.toLong() * dayMillis)

        val challenge = ChallengeEntity(
            challengeId = challengeId,
            durationDays = durationDays,
            goal = goal,
            frequencyId = frequency.id,
            frequencyName = frequency.name,
            frequencyHz = frequency.hz,
            category = frequency.category,
            dailyTargetMinutes = dailyTargetMinutes,
            startDateMillis = now,
            expectedEndDateMillis = expectedEnd,
            currentDay = 1,
            completedDays = 0,
            streak = 0,
            longestStreak = 0,
            recoveryDaysAvailable = 1,
            recoveryDaysUsed = 0,
            status = "active",
            rewardPoints = 0,
            createdAtMillis = now,
            reminderTime = reminderTime,
            reminderEnabled = reminderEnabled,
            isTodayCompleted = false,
            todayListenedMinutes = 0
        )
        dao.insertChallenge(challenge)

        val dayEntities = (1..durationDays).map { dayNum ->
            ChallengeDayEntity(
                dayId = "${challengeId}_day_$dayNum",
                challengeId = challengeId,
                dayNumber = dayNum,
                dateMillis = now + ((dayNum - 1).toLong() * dayMillis),
                targetMinutes = dailyTargetMinutes,
                listenedMinutes = 0,
                isCompleted = false,
                completedAtMillis = null
            )
        }
        dao.insertChallengeDays(dayEntities)
        return challengeId
    }

    suspend fun completeTodayPractice(challengeId: String): MilestoneCelebration? {
        val challengeEntity = dao.getChallengeById(challengeId).first() ?: return null
        val challenge = challengeEntity.toDomain()

        if (challenge.isTodayCompleted) return null

        val now = System.currentTimeMillis()
        val nextCompletedDays = challenge.completedDays + 1
        val nextStreak = challenge.streak + 1
        val nextLongestStreak = maxOf(challenge.longestStreak, nextStreak)

        var pointsEarned = 10
        var milestoneCelebration: MilestoneCelebration? = null

        when (nextStreak) {
            3 -> {
                pointsEarned += 25
                milestoneCelebration = MilestoneCelebration(
                    title = "Great Start!",
                    message = "3 consecutive days of mindful resonance!",
                    pointsAwarded = 25,
                    milestoneDay = 3
                )
            }
            7 -> {
                pointsEarned += 50
                milestoneCelebration = MilestoneCelebration(
                    title = "7-Day Streak! 🔥",
                    message = "One full week of frequency consistency achieved!",
                    pointsAwarded = 50,
                    milestoneDay = 7
                )
            }
            15 -> {
                pointsEarned += 100
                milestoneCelebration = MilestoneCelebration(
                    title = "Halfway Strong! 🌟",
                    message = "15 days completed! Your daily practice is becoming second nature.",
                    pointsAwarded = 100,
                    milestoneDay = 15
                )
            }
            21 -> {
                pointsEarned += 150
                milestoneCelebration = MilestoneCelebration(
                    title = "Habit Builder! 🧠",
                    message = "21 days! Neuroplastic consistency unlocked.",
                    pointsAwarded = 150,
                    milestoneDay = 21
                )
            }
            30 -> {
                pointsEarned += 250
                milestoneCelebration = MilestoneCelebration(
                    title = "30-Day Mastery! 🏆",
                    message = "30 days of dedicated sound meditation excellence.",
                    pointsAwarded = 250,
                    milestoneDay = 30
                )
            }
        }

        val isChallengeCompleted = nextCompletedDays >= challenge.durationDays
        if (isChallengeCompleted) {
            pointsEarned += 300
            milestoneCelebration = MilestoneCelebration(
                title = "🎉 Challenge Complete!",
                message = "You completed all ${challenge.durationDays} days of your practice!",
                pointsAwarded = 300,
                milestoneDay = challenge.durationDays
            )
        }

        val dayId = "${challengeId}_day_${challenge.currentDay}"
        dao.updateDayProgress(
            dayId = dayId,
            listenedMinutes = challenge.dailyTargetMinutes,
            isCompleted = true,
            completedAtMillis = now
        )

        val nextCurrentDay = if (isChallengeCompleted) challenge.currentDay else (challenge.currentDay + 1).coerceAtMost(challenge.durationDays)
        val updatedChallenge = challengeEntity.copy(
            completedDays = nextCompletedDays,
            currentDay = nextCurrentDay,
            streak = nextStreak,
            longestStreak = nextLongestStreak,
            rewardPoints = challenge.rewardPoints + pointsEarned,
            isTodayCompleted = true,
            todayListenedMinutes = challenge.dailyTargetMinutes,
            status = if (isChallengeCompleted) "completed" else "active",
            completedAtMillis = if (isChallengeCompleted) now else null
        )
        dao.updateChallenge(updatedChallenge)

        val currentUid = _activeUserId.value ?: "soul_user"
        val currentProfile = dao.getUserProfileById(currentUid).first() ?: UserProfileEntity(userId = currentUid)
        val updatedProfile = currentProfile.copy(
            userId = currentUid,
            soulPoints = currentProfile.soulPoints + pointsEarned,
            totalPracticeMinutes = currentProfile.totalPracticeMinutes + challenge.dailyTargetMinutes,
            totalDaysCompleted = currentProfile.totalDaysCompleted + 1,
            currentStreak = nextStreak,
            longestStreak = maxOf(currentProfile.longestStreak, nextStreak),
            completedChallengesCount = if (isChallengeCompleted) currentProfile.completedChallengesCount + 1 else currentProfile.completedChallengesCount
        )
        dao.insertOrUpdateUserProfile(updatedProfile)

        return milestoneCelebration
    }

    suspend fun useRecoveryDay(challengeId: String): Boolean {
        val challengeEntity = dao.getChallengeById(challengeId).first() ?: return false
        if (challengeEntity.recoveryDaysAvailable <= 0) return false

        val updated = challengeEntity.copy(
            recoveryDaysAvailable = challengeEntity.recoveryDaysAvailable - 1,
            recoveryDaysUsed = challengeEntity.recoveryDaysUsed + 1
        )
        dao.updateChallenge(updated)
        return true
    }

    suspend fun abandonChallenge(challengeId: String) {
        val challengeEntity = dao.getChallengeById(challengeId).first() ?: return
        dao.updateChallenge(challengeEntity.copy(status = "abandoned"))
    }

    suspend fun seedDefaults() {
        val entities = starterFrequencies.map { FrequencyEntity.fromDomain(it) }
        dao.insertFrequencies(entities)
    }

    private suspend fun checkAndSeedInitialData() {
        val existing = dao.getAllFrequencies().first()
        if (existing.isEmpty()) {
            val entities = starterFrequencies.map { FrequencyEntity.fromDomain(it) }
            dao.insertFrequencies(entities)
        }
    }

    companion object {
        val starterFrequencies = listOf(
            FrequencyItem(
                id = "freq_528",
                hz = 528.0f,
                name = "Heart & Harmony",
                shortDesc = "The Solfeggio frequency of heart coherence, inner balance, and transformative harmony.",
                longDesc = "528 Hz is historically recognized as the 'Transformation' tone within the ancient Solfeggio scale. It is widely explored during meditation and sound therapy sessions to cultivate reflective stillness, emotional openness, and resonant balance. This experience generates pure mathematical harmonic sine waves accompanied by rich acoustic undertones.",
                category = "Meditation",
                tags = listOf("Meditation", "Relax", "Heart", "Balance", "Coherence", "Transformation"),
                intendedExperience = listOf("Reflective Meditation", "Emotional Balance", "Heart Coherence", "Stress Decompression"),
                suggestedContext = "Morning intentional meditation or evening reflection with comfortable headphones.",
                durationMinutes = 30,
                isFeatured = true,
                isPremium = false,
                binauralBeatHz = 6.0f,
                harmonicWarmth = 0.4f
            ),
            FrequencyItem(
                id = "freq_432",
                hz = 432.0f,
                name = "Deep Relaxation",
                shortDesc = "Natural geometric pitch explored for profound calm, serene stillness, and nighttime unwinding.",
                longDesc = "432 Hz is commonly associated with natural harmonic resonance and organic mathematical proportions. Often preferred by sound practitioners for deep bodily relaxation, it provides a warm acoustic grounding that gently guides the mind from busy daytime beta states into restful alpha stillness.",
                category = "Relaxation",
                tags = listOf("Relax", "Sleep", "Meditation", "Calm", "Natural Pitch", "Stress Relief"),
                intendedExperience = listOf("Deep Relaxation", "Night Unwinding", "Restful Stillness", "Calm Breathing"),
                suggestedContext = "Late evening relaxation, unwinding before bed, or quiet reflective reading.",
                durationMinutes = 45,
                isFeatured = true,
                isPremium = false,
                binauralBeatHz = 4.5f,
                harmonicWarmth = 0.5f
            ),
            FrequencyItem(
                id = "freq_741",
                hz = 741.0f,
                name = "Clear Focus & Awakening",
                shortDesc = "Sharp, lucid tone designed for mindful concentration, creative study, and mental clarity.",
                longDesc = "741 Hz is explored in intentional listening for mental clarity, problem-solving, and intuitive insight. Its bright harmonic overtone structure stimulates alert focus without anxiety or agitation, making it an ideal companion for deep work sessions, study, and creative brainstorming.",
                category = "Focus",
                tags = listOf("Focus", "Study", "Clarity", "Intuition", "Creativity", "Deep Work"),
                intendedExperience = listOf("Deep Concentrated Work", "Study Sessions", "Intuitive Problem Solving", "Mindful Alertness"),
                suggestedContext = "Desk work, coding, writing, or focused study with ambient volume.",
                durationMinutes = 60,
                isFeatured = true,
                isPremium = false,
                binauralBeatHz = 14.0f,
                harmonicWarmth = 0.25f
            ),
            FrequencyItem(
                id = "freq_396",
                hz = 396.0f,
                name = "Liberation & Grounding",
                shortDesc = "Low resonant tone intended for releasing emotional tension and establishing grounded peace.",
                longDesc = "396 Hz is a foundational low-frequency Solfeggio tone associated with releasing lingering emotional stress, worry, and subconscious tension. Its warm, enveloping resonance encourages deep rhythmic breathing and an anchor of safe inner calm.",
                category = "Relaxation",
                tags = listOf("Grounding", "Release", "Peace", "Emotional Wellbeing", "Relaxation"),
                intendedExperience = listOf("Tension Release", "Grounded Presence", "Subconscious Calm", "Root Stillness"),
                suggestedContext = "Transitioning after a high-stress workday or grounding before sleep.",
                durationMinutes = 30,
                isFeatured = false,
                isPremium = false,
                binauralBeatHz = 5.0f,
                harmonicWarmth = 0.6f
            ),
            FrequencyItem(
                id = "freq_639",
                hz = 639.0f,
                name = "Compassion & Connection",
                shortDesc = "Warm harmonic resonance designed for empathy, interpersonal harmony, and relational peace.",
                longDesc = "639 Hz is traditionally embraced for cultivating kindness, empathy, and harmonious social connections. The soothing tonal characteristics create a welcoming sonic sanctuary for loving-kindness contemplation and heartfelt communication.",
                category = "Balance",
                tags = listOf("Connection", "Compassion", "Relationships", "Empathy", "Harmony"),
                intendedExperience = listOf("Loving-Kindness Practice", "Relational Stillness", "Empathy Exploration", "Social Peace"),
                suggestedContext = "Mindful journaling, compassionate meditation, or evening family quiet time.",
                durationMinutes = 25,
                isFeatured = true,
                isPremium = false,
                binauralBeatHz = 7.83f,
                harmonicWarmth = 0.45f
            ),
            FrequencyItem(
                id = "freq_852",
                hz = 852.0f,
                name = "Pure Consciousness",
                shortDesc = "Crystalline tone associated with heightened awareness, spiritual order, and mindful stillness.",
                longDesc = "852 Hz offers an ethereal, pristine acoustic texture intended to support introspective meditation. By helping quiet persistent mental chatter, this frequency provides an expansive sonic field for deep contemplation and pure stillness.",
                category = "Meditation",
                tags = listOf("Meditation", "Consciousness", "Intuition", "Stillness", "Mindfulness"),
                intendedExperience = listOf("Expansive Awareness", "Mindful Stillness", "Ethereal Contemplation", "Quiet Mind"),
                suggestedContext = "Morning seated meditation, silent retreat, or twilight contemplation.",
                durationMinutes = 40,
                isFeatured = false,
                isPremium = false,
                binauralBeatHz = 8.0f,
                harmonicWarmth = 0.3f
            ),
            FrequencyItem(
                id = "freq_963",
                hz = 963.0f,
                name = "Crown Awakening & Transcendent State",
                shortDesc = "High harmonic tone designed for transcendent meditation, inspiration, and higher consciousness.",
                longDesc = "963 Hz is known as the highest of the core Solfeggio tones, often connected to cosmic unity and transcendent visionary contemplation. It delivers a shimmering sonic presence that invites the listener into a timeless, serene sanctuary of thought.",
                category = "Manifestation",
                tags = listOf("Manifestation", "Awakening", "Transcendence", "Inspiration", "Higher State"),
                intendedExperience = listOf("Transcendent Stillness", "Creative Vision", "Inspirational Contemplation", "Unity Flow"),
                suggestedContext = "Intentional manifestation sessions, breathwork, or visualization practices.",
                durationMinutes = 35,
                isFeatured = true,
                isPremium = true,
                binauralBeatHz = 10.0f,
                harmonicWarmth = 0.2f
            ),
            FrequencyItem(
                id = "freq_174",
                hz = 174.0f,
                name = "Natural Grounding & Relief",
                shortDesc = "Deep, warm acoustic foundation intended for physical relaxation and organic security.",
                longDesc = "174 Hz delivers a comforting, low-pitch drone that functions as a natural acoustic security blanket. Sound therapy practitioners explore this tone to facilitate physical relaxation and comforting sensory grounding.",
                category = "Relaxation",
                tags = listOf("Grounding", "Body Comfort", "Relief", "Security", "Calm"),
                intendedExperience = listOf("Deep Somatic Relaxation", "Physical Grounding", "Comforting Rest", "Steady Breathing"),
                suggestedContext = "Pre-sleep routine or resting after rigorous exercise.",
                durationMinutes = 30,
                isFeatured = false,
                isPremium = false,
                binauralBeatHz = 3.0f,
                harmonicWarmth = 0.7f
            ),
            FrequencyItem(
                id = "freq_285",
                hz = 285.0f,
                name = "Cellular Energy & Restoration",
                shortDesc = "Uplifting regenerative frequency explored for revitalization, vitality, and inner recharge.",
                longDesc = "285 Hz is associated with restoration and cellular revitalization. The frequency provides a dynamic yet calming energetic signature that helps listeners recharge their vitality during afternoon slumps or recovery days.",
                category = "Energy",
                tags = listOf("Energy", "Restoration", "Vitality", "Recharge", "Wellness"),
                intendedExperience = listOf("Vitality Recharge", "Energetic Balance", "Refreshed Alertness", "Regenerative Rest"),
                suggestedContext = "Afternoon energy reset or morning stretching routine.",
                durationMinutes = 20,
                isFeatured = false,
                isPremium = false,
                binauralBeatHz = 12.0f,
                harmonicWarmth = 0.35f
            ),
            FrequencyItem(
                id = "freq_417",
                hz = 417.0f,
                name = "Facilitating Change & Renewal",
                shortDesc = "Clearing harmonic frequency intended to break stagnation, catalyze change, and welcome new energy.",
                longDesc = "417 Hz is designed to accompany transitions and new beginnings. It is commonly utilized by wellness practitioners to dissolve repetitive negative loops and welcome fresh perspectives, optimism, and renewed momentum.",
                category = "Manifestation",
                tags = listOf("Manifestation", "Change", "Renewal", "Optimism", "Energy"),
                intendedExperience = listOf("Catalyzing Change", "Breaking Stagnation", "Optimistic Mindset", "Intentional Shift"),
                suggestedContext = "New moon, new month, goal-setting days, or starting a new endeavor.",
                durationMinutes = 30,
                isFeatured = false,
                isPremium = false,
                binauralBeatHz = 8.5f,
                harmonicWarmth = 0.4f
            ),
            FrequencyItem(
                id = "freq_888",
                hz = 888.0f,
                name = "Abundance & Prosperity Mindset",
                shortDesc = "Radiant golden tone designed for gratitude, prosperity alignment, and manifestation routines.",
                longDesc = "888 Hz is recognized as the abundance frequency, celebrated for its uplifting harmonic resonance. It is intended as an acoustic backdrop for gratitude journaling, visualizing prosperity, and adopting an expansive, generous abundance mindset.",
                category = "Money",
                tags = listOf("Money", "Abundance", "Prosperity", "Gratitude", "Manifestation", "Mindset"),
                intendedExperience = listOf("Gratitude Cultivation", "Prosperity Visualization", "Expansive Confidence", "Abundance Flow"),
                suggestedContext = "Morning gratitude practice, visionary planning, or abundance meditation.",
                durationMinutes = 30,
                isFeatured = true,
                isPremium = true,
                binauralBeatHz = 9.0f,
                harmonicWarmth = 0.35f
            ),
            FrequencyItem(
                id = "freq_3",
                hz = 3.2f,
                name = "Deep Delta Sleep Wave",
                shortDesc = "Ultra-low soothing brainwave frequency tailored for deep sleep, nighttime recovery, and serene dreams.",
                longDesc = "Utilizing an organic 432 Hz warm carrier wave modulated with a 3.2 Hz Delta binaural differential, this track is specifically designed to support nighttime sleep routines and profound restorative relaxation.",
                category = "Sleep",
                tags = listOf("Sleep", "Delta Wave", "Nighttime", "Rest", "Insomnia Relief", "Calm"),
                intendedExperience = listOf("Deep Sleep Induction", "Nighttime Restoration", "Slowing Racing Thoughts", "Serene Dreams"),
                suggestedContext = "In bed with eye mask and gentle sleep timer set to 30 or 60 minutes.",
                durationMinutes = 60,
                isFeatured = true,
                isPremium = false,
                binauralBeatHz = 3.2f,
                harmonicWarmth = 0.65f
            ),
            FrequencyItem(
                id = "freq_40",
                hz = 40.0f,
                name = "Gamma High Focus & Flow",
                shortDesc = "Synchronized 40 Hz gamma wave designed for peak cognitive processing, memorization, and intense flow.",
                longDesc = "Gamma brainwaves (around 40 Hz) are associated with simultaneous information processing, heightened awareness, and flow state. Paired with a calming 528 Hz harmonic foundation, this track creates a focused, non-distracting sound shield.",
                category = "Focus",
                tags = listOf("Focus", "Gamma Wave", "Deep Work", "Study", "Cognition", "Flow State"),
                intendedExperience = listOf("Flow State Immersion", "Rapid Information Processing", "Task Execution", "Distraction Shielding"),
                suggestedContext = "High-concentration work blocks, coding sprints, or complex problem-solving.",
                durationMinutes = 45,
                isFeatured = false,
                isPremium = true,
                binauralBeatHz = 40.0f,
                harmonicWarmth = 0.2f
            )
        )
    }
}
