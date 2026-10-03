package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        FrequencyEntity::class,
        FavoriteEntity::class,
        ListeningHistoryEntity::class,
        PlaylistEntity::class,
        PlaylistTrackEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SoulSoundDatabase : RoomDatabase() {

    abstract fun soulSoundDao(): SoulSoundDao

    companion object {
        @Volatile
        private var INSTANCE: SoulSoundDatabase? = null

        fun getInstance(context: Context): SoulSoundDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SoulSoundDatabase::class.java,
                    "soulsound_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
