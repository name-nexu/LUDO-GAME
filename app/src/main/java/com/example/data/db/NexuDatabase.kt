package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.AchievementItem
import com.example.data.model.CoinTransaction
import com.example.data.model.GameSettingsEntity
import com.example.data.model.LeaderboardEntry
import com.example.data.model.MatchHistoryItem
import com.example.data.model.UserProfile

@Database(
    entities = [
        UserProfile::class,
        CoinTransaction::class,
        MatchHistoryItem::class,
        AchievementItem::class,
        LeaderboardEntry::class,
        GameSettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class NexuDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun coinTransactionDao(): CoinTransactionDao
    abstract fun matchHistoryDao(): MatchHistoryDao
    abstract fun achievementDao(): AchievementDao
    abstract fun leaderboardDao(): LeaderboardDao
    abstract fun settingsDao(): SettingsDao

    companion object {
        @Volatile
        private var INSTANCE: NexuDatabase? = null

        fun getDatabase(context: Context): NexuDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NexuDatabase::class.java,
                    "nexu_ludo.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
