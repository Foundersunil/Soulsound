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
    val createdAt: Long
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
            createdAt = createdAt
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
                createdAt = item.createdAt
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
