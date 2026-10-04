package com.example.data.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ChallengeItem(
    val id: String,
    val durationDays: Int,
    val goal: String, // Focus, Meditation, Relaxation, Sleep, Energy, Balance, Money, Manifestation
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
    val status: String = "active", // active, completed, paused, abandoned
    val rewardPoints: Int = 0,
    val createdAtMillis: Long = System.currentTimeMillis(),
    val completedAtMillis: Long? = null,
    val reminderTime: String = "08:00 PM",
    val reminderEnabled: Boolean = true,
    val isTodayCompleted: Boolean = false,
    val todayListenedMinutes: Int = 0
) {
    val progressPercentage: Int
        get() = if (durationDays > 0) ((completedDays.toFloat() / durationDays) * 100).toInt().coerceIn(0, 100) else 0

    val remainingDays: Int
        get() = (durationDays - completedDays).coerceAtLeast(0)

    val formattedStartDate: String
        get() = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(startDateMillis))

    val formattedEndDate: String
        get() = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(expectedEndDateMillis))
}

data class ChallengeDayItem(
    val dayNumber: Int,
    val targetMinutes: Int,
    val listenedMinutes: Int,
    val isCompleted: Boolean,
    val completedAtMillis: Long?,
    val isCurrent: Boolean,
    val isFuture: Boolean,
    val isMissed: Boolean
)

data class UserProfileStats(
    val userId: String = "soul_user",
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
    val preferredGoals: List<String>
        get() = if (preferredGoalsCsv.isBlank()) listOf("Meditation") else preferredGoalsCsv.split("|").map { it.trim() }

    val levelNumber: Int
        get() = when {
            soulPoints >= 2001 -> 5
            soulPoints >= 1001 -> 4
            soulPoints >= 501 -> 3
            soulPoints >= 201 -> 2
            else -> 1
        }

    val levelTitle: String
        get() = when (levelNumber) {
            1 -> "Frequency Beginner"
            2 -> "Sound Explorer"
            3 -> "Mindful Listener"
            4 -> "Frequency Practitioner"
            else -> "SoulSound Master"
        }

    val levelMinXp: Int
        get() = when (levelNumber) {
            1 -> 0
            2 -> 201
            3 -> 501
            4 -> 1001
            else -> 2001
        }

    val levelMaxXp: Int
        get() = when (levelNumber) {
            1 -> 200
            2 -> 500
            3 -> 1000
            4 -> 2000
            else -> 3500
        }

    val levelProgressFraction: Float
        get() {
            val range = levelMaxXp - levelMinXp
            if (range <= 0) return 1f
            return ((soulPoints - levelMinXp).toFloat() / range).coerceIn(0f, 1f)
        }
}

data class MilestoneCelebration(
    val title: String,
    val message: String,
    val pointsAwarded: Int,
    val milestoneDay: Int
)

data class AchievementBadge(
    val id: String,
    val icon: String,
    val title: String,
    val description: String,
    val requirement: String,
    val isUnlocked: Boolean,
    val progress: Float = 0f
)

data class WeeklyDayActivity(
    val dayName: String,
    val dateMillis: Long,
    val minutesListened: Int,
    val intensity: Int // 0 = none, 1 = low, 2 = medium, 3 = high
)
