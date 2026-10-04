package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.example.data.model.FrequencyItem

@Entity(tableName = "frequencies")
data class FrequencyEntity(
    @PrimaryKey val id: String,
    val hz: Float,
    val name: String,
    val shortDesc: String,
    val longDesc: String,
    val category: String,
    val tagsCsv: String,
    val intendedExperienceCsv: String,
    val suggestedContext: String,
    val durationMinutes: Int,
    val isFeatured: Boolean,
    val isPremium: Boolean,
    val binauralBeatHz: Float,
    val harmonicWarmth: Float,
    val createdAt: Long,
    val isDeleted: Boolean = false,
    val deletedAtMillis: Long? = null,
    val deletedBy: String? = null,
    val audioUrl: String? = null,
    val audioFileName: String? = null,
    val audioFilePath: String? = null
) {
    fun toDomain(): FrequencyItem {
        return FrequencyItem(
            id = id,
            hz = hz,
            name = name,
            shortDesc = shortDesc,
            longDesc = longDesc,
            category = category,
            tags = if (tagsCsv.isBlank()) emptyList() else tagsCsv.split("|").map { it.trim() },
            intendedExperience = if (intendedExperienceCsv.isBlank()) emptyList() else intendedExperienceCsv.split("|").map { it.trim() },
            suggestedContext = suggestedContext,
            durationMinutes = durationMinutes,
            isFeatured = isFeatured,
            isPremium = isPremium,
            binauralBeatHz = binauralBeatHz,
            harmonicWarmth = harmonicWarmth,
            createdAt = createdAt,
            isDeleted = isDeleted,
            deletedAtMillis = deletedAtMillis,
            deletedBy = deletedBy,
            audioUrl = audioUrl,
            audioFileName = audioFileName,
            audioFilePath = audioFilePath
        )
    }

    companion object {
        fun fromDomain(item: FrequencyItem): FrequencyEntity {
            return FrequencyEntity(
                id = item.id,
                hz = item.hz,
                name = item.name,
                shortDesc = item.shortDesc,
                longDesc = item.longDesc,
                category = item.category,
                tagsCsv = item.tags.joinToString("|"),
                intendedExperienceCsv = item.intendedExperience.joinToString("|"),
                suggestedContext = item.suggestedContext,
                durationMinutes = item.durationMinutes,
                isFeatured = item.isFeatured,
                isPremium = item.isPremium,
                binauralBeatHz = item.binauralBeatHz,
                harmonicWarmth = item.harmonicWarmth,
                createdAt = item.createdAt,
                isDeleted = item.isDeleted,
                deletedAtMillis = item.deletedAtMillis,
                deletedBy = item.deletedBy,
                audioUrl = item.audioUrl,
                audioFileName = item.audioFileName,
                audioFilePath = item.audioFilePath
            )
        }
    }
}

@Entity(tableName = "admin_audit_logs")
data class AdminAuditLogEntity(
    @PrimaryKey val logId: String,
    val adminUserId: String,
    val adminEmail: String,
    val action: String, // CREATE, UPDATE, DELETE, RESTORE, SEED
    val resourceType: String, // FREQUENCY, CATEGORY, METADATA
    val resourceId: String,
    val resourceName: String,
    val timestampMillis: Long = System.currentTimeMillis()
) {
    fun toDomain(): com.example.data.model.AdminAuditLog {
        return com.example.data.model.AdminAuditLog(
            logId = logId,
            adminUserId = adminUserId,
            adminEmail = adminEmail,
            action = action,
            resourceType = resourceType,
            resourceId = resourceId,
            resourceName = resourceName,
            timestampMillis = timestampMillis
        )
    }

    companion object {
        fun fromDomain(item: com.example.data.model.AdminAuditLog): AdminAuditLogEntity {
            return AdminAuditLogEntity(
                logId = item.logId,
                adminUserId = item.adminUserId,
                adminEmail = item.adminEmail,
                action = item.action,
                resourceType = item.resourceType,
                resourceId = item.resourceId,
                resourceName = item.resourceName,
                timestampMillis = item.timestampMillis
            )
        }
    }
}

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val frequencyId: String,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "listening_history")
data class ListeningHistoryEntity(
    @PrimaryKey(autoGenerate = true) val historyId: Long = 0L,
    val frequencyId: String,
    val playedAt: Long = System.currentTimeMillis(),
    val progressMs: Long = 0L,
    val totalDurationMs: Long = 0L
)

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey val playlistId: String,
    val title: String,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "playlist_tracks", primaryKeys = ["playlistId", "frequencyId"])
data class PlaylistTrackEntity(
    val playlistId: String,
    val frequencyId: String,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "challenges")
data class ChallengeEntity(
    @PrimaryKey val challengeId: String,
    val durationDays: Int,
    val goal: String,
    val frequencyId: String,
    val frequencyName: String,
    val frequencyHz: Float,
    val category: String,
    val dailyTargetMinutes: Int,
    val startDateMillis: Long,
    val expectedEndDateMillis: Long,
    val currentDay: Int,
    val completedDays: Int,
    val streak: Int,
    val longestStreak: Int,
    val recoveryDaysAvailable: Int = 1,
    val recoveryDaysUsed: Int = 0,
    val status: String = "active",
    val rewardPoints: Int = 0,
    val createdAtMillis: Long = System.currentTimeMillis(),
    val completedAtMillis: Long? = null,
    val reminderTime: String = "08:00 PM",
    val reminderEnabled: Boolean = true,
    val isTodayCompleted: Boolean = false,
    val todayListenedMinutes: Int = 0
) {
    fun toDomain(): com.example.data.model.ChallengeItem {
        return com.example.data.model.ChallengeItem(
            id = challengeId,
            durationDays = durationDays,
            goal = goal,
            frequencyId = frequencyId,
            frequencyName = frequencyName,
            frequencyHz = frequencyHz,
            category = category,
            dailyTargetMinutes = dailyTargetMinutes,
            startDateMillis = startDateMillis,
            expectedEndDateMillis = expectedEndDateMillis,
            currentDay = currentDay,
            completedDays = completedDays,
            streak = streak,
            longestStreak = longestStreak,
            recoveryDaysAvailable = recoveryDaysAvailable,
            recoveryDaysUsed = recoveryDaysUsed,
            status = status,
            rewardPoints = rewardPoints,
            createdAtMillis = createdAtMillis,
            completedAtMillis = completedAtMillis,
            reminderTime = reminderTime,
            reminderEnabled = reminderEnabled,
            isTodayCompleted = isTodayCompleted,
            todayListenedMinutes = todayListenedMinutes
        )
    }

    companion object {
        fun fromDomain(item: com.example.data.model.ChallengeItem): ChallengeEntity {
            return ChallengeEntity(
                challengeId = item.id,
                durationDays = item.durationDays,
                goal = item.goal,
                frequencyId = item.frequencyId,
                frequencyName = item.frequencyName,
                frequencyHz = item.frequencyHz,
                category = item.category,
                dailyTargetMinutes = item.dailyTargetMinutes,
                startDateMillis = item.startDateMillis,
                expectedEndDateMillis = item.expectedEndDateMillis,
                currentDay = item.currentDay,
                completedDays = item.completedDays,
                streak = item.streak,
                longestStreak = item.longestStreak,
                recoveryDaysAvailable = item.recoveryDaysAvailable,
                recoveryDaysUsed = item.recoveryDaysUsed,
                status = item.status,
                rewardPoints = item.rewardPoints,
                createdAtMillis = item.createdAtMillis,
                completedAtMillis = item.completedAtMillis,
                reminderTime = item.reminderTime,
                reminderEnabled = item.reminderEnabled,
                isTodayCompleted = item.isTodayCompleted,
                todayListenedMinutes = item.todayListenedMinutes
            )
        }
    }
}

@Entity(tableName = "challenge_days")
data class ChallengeDayEntity(
    @PrimaryKey val dayId: String,
    val challengeId: String,
    val dayNumber: Int,
    val dateMillis: Long,
    val targetMinutes: Int,
    val listenedMinutes: Int,
    val isCompleted: Boolean,
    val completedAtMillis: Long? = null
)

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val userId: String = "soul_user",
    val userName: String = "Sunil",
    val userEmail: String = "toolssunilmaurya@gmail.com",
    val avatarUri: String? = null,
    val preferredGoalsCsv: String = "Meditation|Focus|Sleep",
    val reminderTime: String = "08:00 PM",
    val reminderEnabled: Boolean = true,
    val soulPoints: Int = 120,
    val totalPracticeMinutes: Int = 60,
    val totalDaysCompleted: Int = 4,
    val currentStreak: Int = 3,
    val longestStreak: Int = 7,
    val completedChallengesCount: Int = 0
) {
    fun toDomain(): com.example.data.model.UserProfileStats {
        return com.example.data.model.UserProfileStats(
            userId = userId,
            userName = userName,
            userEmail = userEmail,
            avatarUri = avatarUri,
            preferredGoalsCsv = preferredGoalsCsv,
            reminderTime = reminderTime,
            reminderEnabled = reminderEnabled,
            soulPoints = soulPoints,
            totalPracticeMinutes = totalPracticeMinutes,
            totalDaysCompleted = totalDaysCompleted,
            currentStreak = currentStreak,
            longestStreak = longestStreak,
            completedChallengesCount = completedChallengesCount
        )
    }

    companion object {
        fun fromDomain(item: com.example.data.model.UserProfileStats): UserProfileEntity {
            return UserProfileEntity(
                userId = item.userId,
                userName = item.userName,
                userEmail = item.userEmail,
                avatarUri = item.avatarUri,
                preferredGoalsCsv = item.preferredGoalsCsv,
                reminderTime = item.reminderTime,
                reminderEnabled = item.reminderEnabled,
                soulPoints = item.soulPoints,
                totalPracticeMinutes = item.totalPracticeMinutes,
                totalDaysCompleted = item.totalDaysCompleted,
                currentStreak = item.currentStreak,
                longestStreak = item.longestStreak,
                completedChallengesCount = item.completedChallengesCount
            )
        }
    }
}

