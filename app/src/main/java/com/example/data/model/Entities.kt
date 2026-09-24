package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: String = "nexu_player_1",
    val username: String = "NexuPlayer",
    val avatarId: Int = 1,
    val level: Int = 1,
    val xp: Long = 120,
    val coins: Long = 1000,
    val gamesPlayed: Int = 0,
    val gamesWon: Int = 0,
    val gamesLost: Int = 0,
    val totalCaptures: Int = 0,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val lastDailyClaimTime: Long = 0L,
    val dailyStreakCount: Int = 0,
    val isGuest: Boolean = true,
    val email: String? = null
)

@Entity(tableName = "coin_transactions")
data class CoinTransaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val playerId: String,
    val amount: Long,
    val reason: String,
    val timestamp: Long = System.currentTimeMillis(),
    val balanceBefore: Long,
    val balanceAfter: Long
)

@Entity(tableName = "match_history")
data class MatchHistoryItem(
    @PrimaryKey val matchId: String,
    val mode: String, // "LOCAL", "VS_AI", "QUICK_MATCH", "PRIVATE_ROOM"
    val playerCount: Int,
    val userRank: Int, // 1 = Winner
    val userColor: String,
    val capturesCount: Int,
    val xpEarned: Int,
    val coinsEarned: Int,
    val durationSeconds: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "achievements")
data class AchievementItem(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val targetCount: Int,
    val currentProgress: Int,
    val isUnlocked: Boolean,
    val rewardCoins: Int,
    val rewardXp: Int,
    val isClaimed: Boolean
)

@Entity(tableName = "leaderboard_entries")
data class LeaderboardEntry(
    @PrimaryKey val id: String,
    val username: String,
    val avatarId: Int,
    val level: Int,
    val wins: Int,
    val xp: Long,
    val category: String // "GLOBAL", "WEEKLY", "FRIENDS"
)

@Entity(tableName = "game_settings")
data class GameSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val soundEnabled: Boolean = true,
    val musicVolume: Float = 0.8f,
    val sfxVolume: Float = 1.0f,
    val vibrationEnabled: Boolean = true,
    val reducedMotion: Boolean = false
)
