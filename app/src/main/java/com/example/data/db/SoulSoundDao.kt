package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SoulSoundDao {

    @Query("SELECT * FROM frequencies WHERE isDeleted = 0 ORDER BY hz ASC")
    fun getAllFrequencies(): Flow<List<FrequencyEntity>>

    @Query("SELECT * FROM frequencies ORDER BY hz ASC")
    fun getAllFrequenciesAdmin(): Flow<List<FrequencyEntity>>

    @Query("SELECT * FROM frequencies WHERE isFeatured = 1 AND isDeleted = 0 ORDER BY hz ASC")
    fun getFeaturedFrequencies(): Flow<List<FrequencyEntity>>

    @Query("SELECT * FROM frequencies WHERE id = :id LIMIT 1")
    fun getFrequencyById(id: String): Flow<FrequencyEntity?>

    @Query("SELECT * FROM frequencies WHERE id = :id LIMIT 1")
    suspend fun getFrequencyByIdOnce(id: String): FrequencyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFrequencies(items: List<FrequencyEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFrequency(item: FrequencyEntity)

    @Update
    suspend fun updateFrequency(item: FrequencyEntity)

    @Query("UPDATE frequencies SET isDeleted = 1, deletedAtMillis = :deletedAt, deletedBy = :deletedBy WHERE id = :id")
    suspend fun softDeleteFrequency(id: String, deletedAt: Long = System.currentTimeMillis(), deletedBy: String = "admin@soulsound.app")

    @Query("UPDATE frequencies SET isDeleted = 0, deletedAtMillis = NULL, deletedBy = NULL WHERE id = :id")
    suspend fun restoreFrequency(id: String)

    @Query("DELETE FROM frequencies WHERE id = :id")
    suspend fun deleteFrequency(id: String)

    // Favorites
    @Query("SELECT * FROM favorites ORDER BY addedAt DESC")
    fun getAllFavorites(): Flow<List<FavoriteEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE frequencyId = :frequencyId)")
    fun isFavorite(frequencyId: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(fav: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE frequencyId = :frequencyId")
    suspend fun deleteFavorite(frequencyId: String)

    // History
    @Query("SELECT * FROM listening_history ORDER BY playedAt DESC LIMIT 20")
    fun getRecentHistory(): Flow<List<ListeningHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun recordHistory(history: ListeningHistoryEntity)

    @Query("DELETE FROM listening_history")
    suspend fun clearHistory()

    // Playlists
    @Query("SELECT * FROM playlists ORDER BY createdAt DESC")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity)

    @Query("DELETE FROM playlists WHERE playlistId = :id")
    suspend fun deletePlaylist(id: String)

    @Query("SELECT * FROM playlist_tracks WHERE playlistId = :playlistId ORDER BY addedAt DESC")
    fun getTracksForPlaylist(playlistId: String): Flow<List<PlaylistTrackEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addTrackToPlaylist(entry: PlaylistTrackEntity)

    @Query("DELETE FROM playlist_tracks WHERE playlistId = :playlistId AND frequencyId = :frequencyId")
    suspend fun removeTrackFromPlaylist(playlistId: String, frequencyId: String)

    // Challenges
    @Query("SELECT * FROM challenges WHERE status = 'active' ORDER BY createdAtMillis DESC LIMIT 1")
    fun getActiveChallenge(): Flow<ChallengeEntity?>

    @Query("SELECT * FROM challenges ORDER BY createdAtMillis DESC")
    fun getAllChallenges(): Flow<List<ChallengeEntity>>

    @Query("SELECT * FROM challenges WHERE challengeId = :id LIMIT 1")
    fun getChallengeById(id: String): Flow<ChallengeEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChallenge(challenge: ChallengeEntity)

    @Update
    suspend fun updateChallenge(challenge: ChallengeEntity)

    @Query("DELETE FROM challenges WHERE challengeId = :id")
    suspend fun deleteChallenge(id: String)

    // Challenge Days
    @Query("SELECT * FROM challenge_days WHERE challengeId = :challengeId ORDER BY dayNumber ASC")
    fun getDaysForChallenge(challengeId: String): Flow<List<ChallengeDayEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChallengeDays(days: List<ChallengeDayEntity>)

    @Update
    suspend fun updateChallengeDay(day: ChallengeDayEntity)

    @Query("UPDATE challenge_days SET listenedMinutes = :listenedMinutes, isCompleted = :isCompleted, completedAtMillis = :completedAtMillis WHERE dayId = :dayId")
    suspend fun updateDayProgress(dayId: String, listenedMinutes: Int, isCompleted: Boolean, completedAtMillis: Long?)

    // User Profile
    @Query("SELECT * FROM user_profile WHERE userId = 'soul_user' LIMIT 1")
    fun getUserProfile(): Flow<UserProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateUserProfile(profile: UserProfileEntity)

    // Admin Audit Logs
    @Query("SELECT * FROM admin_audit_logs ORDER BY timestampMillis DESC")
    fun getAllAuditLogs(): Flow<List<AdminAuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AdminAuditLogEntity)
}
