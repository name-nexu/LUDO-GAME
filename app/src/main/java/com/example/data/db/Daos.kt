package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AchievementItem
import com.example.data.model.CoinTransaction
import com.example.data.model.GameSettingsEntity
import com.example.data.model.LeaderboardEntry
import com.example.data.model.MatchHistoryItem
import com.example.data.model.UserProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM user_profile WHERE id = :userId LIMIT 1")
    fun getUserProfileFlow(userId: String = "nexu_player_1"): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile WHERE id = :userId LIMIT 1")
    suspend fun getUserProfile(userId: String = "nexu_player_1"): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateUser(user: UserProfile)

    @Update
    suspend fun updateUser(user: UserProfile)
}

@Dao
interface CoinTransactionDao {
    @Query("SELECT * FROM coin_transactions ORDER BY timestamp DESC LIMIT 50")
    fun getRecentTransactions(): Flow<List<CoinTransaction>>

    @Insert
    suspend fun insertTransaction(transaction: CoinTransaction)
}

@Dao
interface MatchHistoryDao {
    @Query("SELECT * FROM match_history ORDER BY timestamp DESC LIMIT 50")
    fun getMatchHistory(): Flow<List<MatchHistoryItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatch(match: MatchHistoryItem)
}

@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievements ORDER BY isUnlocked DESC, isClaimed ASC")
    fun getAllAchievements(): Flow<List<AchievementItem>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertInitialAchievements(achievements: List<AchievementItem>)

    @Update
    suspend fun updateAchievement(achievement: AchievementItem)

    @Query("SELECT * FROM achievements WHERE id = :id LIMIT 1")
    suspend fun getAchievement(id: String): AchievementItem?
}

@Dao
interface LeaderboardDao {
    @Query("SELECT * FROM leaderboard_entries WHERE category = :category ORDER BY wins DESC, xp DESC")
    fun getLeaderboardByCategory(category: String): Flow<List<LeaderboardEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeaderboard(entries: List<LeaderboardEntry>)
}

@Dao
interface SettingsDao {
    @Query("SELECT * FROM game_settings WHERE id = 1 LIMIT 1")
    fun getSettingsFlow(): Flow<GameSettingsEntity?>

    @Query("SELECT * FROM game_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettings(): GameSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: GameSettingsEntity)
}
