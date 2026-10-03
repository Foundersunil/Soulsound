package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SoulSoundDao {

    @Query("SELECT * FROM frequencies ORDER BY hz ASC")
    fun getAllFrequencies(): Flow<List<FrequencyEntity>>

    @Query("SELECT * FROM frequencies WHERE isFeatured = 1 ORDER BY hz ASC")
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
}
