package com.example.data.repository

import com.example.data.db.AchievementDao
import com.example.data.db.CoinTransactionDao
import com.example.data.db.LeaderboardDao
import com.example.data.db.MatchHistoryDao
import com.example.data.db.SettingsDao
import com.example.data.db.UserDao
import com.example.data.model.AchievementItem
import com.example.data.model.CoinTransaction
import com.example.data.model.GameSettingsEntity
import com.example.data.model.LeaderboardEntry
import com.example.data.model.MatchHistoryItem
import com.example.data.model.UserProfile
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class GameRepository(
    private val userDao: UserDao,
    private val coinTransactionDao: CoinTransactionDao,
    private val matchHistoryDao: MatchHistoryDao,
    private val achievementDao: AchievementDao,
    private val leaderboardDao: LeaderboardDao,
    private val settingsDao: SettingsDao
) {
    val userProfileFlow: Flow<UserProfile?> = userDao.getUserProfileFlow()
    val matchHistoryFlow: Flow<List<MatchHistoryItem>> = matchHistoryDao.getMatchHistory()
    val achievementsFlow: Flow<List<AchievementItem>> = achievementDao.getAllAchievements()
    val settingsFlow: Flow<GameSettingsEntity?> = settingsDao.getSettingsFlow()
    val transactionsFlow: Flow<List<CoinTransaction>> = coinTransactionDao.getRecentTransactions()

    fun getLeaderboard(category: String): Flow<List<LeaderboardEntry>> =
        leaderboardDao.getLeaderboardByCategory(category)

    suspend fun initializeDefaults() {
        val existingUser = userDao.getUserProfile()
        if (existingUser == null) {
            userDao.insertOrUpdateUser(
                UserProfile(
                    id = "nexu_player_1",
                    username = "CyberKnight",
                    avatarId = 1,
                    level = 1,
                    xp = 150,
                    coins = 1500,
                    gamesPlayed = 0,
                    gamesWon = 0,
                    gamesLost = 0,
                    totalCaptures = 0,
                    currentStreak = 0,
                    bestStreak = 0,
                    lastDailyClaimTime = 0L,
                    dailyStreakCount = 0
                )
            )
        }

        val existingSettings = settingsDao.getSettings()
        if (existingSettings == null) {
            settingsDao.saveSettings(GameSettingsEntity())
        }

        achievementDao.insertInitialAchievements(initialAchievementsList)
        seedLeaderboardEntries()
    }

    suspend fun updateSettings(settings: GameSettingsEntity) {
        settingsDao.saveSettings(settings)
    }

    suspend fun updateUserProfile(user: UserProfile) {
        userDao.updateUser(user)
    }

    suspend fun adjustCoins(amount: Long, reason: String): Boolean {
        val user = userDao.getUserProfile() ?: return false
        val newBalance = user.coins + amount
        if (newBalance < 0) return false // Prevent overdraft

        val transaction = CoinTransaction(
            playerId = user.id,
            amount = amount,
            reason = reason,
            balanceBefore = user.coins,
            balanceAfter = newBalance
        )
        coinTransactionDao.insertTransaction(transaction)
        userDao.updateUser(user.copy(coins = newBalance))
        return true
    }

    suspend fun recordMatchCompleted(
        mode: String,
        playerCount: Int,
        userRank: Int,
        userColor: String,
        capturesCount: Int,
        xpEarned: Int,
        coinsEarned: Int,
        durationSeconds: Int
    ) {
        val user = userDao.getUserProfile() ?: return
        val isWin = userRank == 1
        val newStreak = if (isWin) user.currentStreak + 1 else 0
        val bestStreak = maxOf(user.bestStreak, newStreak)
        val newXp = user.xp + xpEarned
        val newLevel = calculateLevel(newXp)
        val newCoins = user.coins + coinsEarned

        val match = MatchHistoryItem(
            matchId = "match_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(5)}",
            mode = mode,
            playerCount = playerCount,
            userRank = userRank,
            userColor = userColor,
            capturesCount = capturesCount,
            xpEarned = xpEarned,
            coinsEarned = coinsEarned,
            durationSeconds = durationSeconds
        )
        matchHistoryDao.insertMatch(match)

        // Ledger coin record if reward given
        if (coinsEarned > 0) {
            coinTransactionDao.insertTransaction(
                CoinTransaction(
                    playerId = user.id,
                    amount = coinsEarned.toLong(),
                    reason = "Match ${if (isWin) "Victory" else "Placement"} Reward ($mode)",
                    balanceBefore = user.coins,
                    balanceAfter = newCoins
                )
            )
        }

        userDao.updateUser(
            user.copy(
                level = newLevel,
                xp = newXp,
                coins = newCoins,
                gamesPlayed = user.gamesPlayed + 1,
                gamesWon = if (isWin) user.gamesWon + 1 else user.gamesWon,
                gamesLost = if (!isWin) user.gamesLost + 1 else user.gamesLost,
                totalCaptures = user.totalCaptures + capturesCount,
                currentStreak = newStreak,
                bestStreak = bestStreak
            )
        )

        // Update achievements progress
        updateAchievementProgress("ach_first_game", 1)
        if (isWin) {
            updateAchievementProgress("ach_first_win", 1)
            updateAchievementProgress("ach_10_wins", 1)
            updateAchievementProgress("ach_50_wins", 1)
        }
        if (capturesCount > 0) {
            updateAchievementProgress("ach_first_capture", 1)
            updateAchievementProgress("ach_10_captures", capturesCount)
        }
        if (newStreak >= 3) {
            updateAchievementProgress("ach_win_streak", newStreak)
        }
    }

    suspend fun claimAchievement(achievementId: String): Boolean {
        val ach = achievementDao.getAchievement(achievementId) ?: return false
        if (!ach.isUnlocked || ach.isClaimed) return false

        val updated = ach.copy(isClaimed = true)
        achievementDao.updateAchievement(updated)

        adjustCoins(ach.rewardCoins.toLong(), "Achievement: ${ach.title}")
        val user = userDao.getUserProfile()
        if (user != null) {
            val newXp = user.xp + ach.rewardXp
            userDao.updateUser(user.copy(xp = newXp, level = calculateLevel(newXp)))
        }
        return true
    }

    private suspend fun updateAchievementProgress(achievementId: String, increment: Int) {
        val ach = achievementDao.getAchievement(achievementId) ?: return
        if (ach.isUnlocked) return
        val newProgress = ach.currentProgress + increment
        val unlocked = newProgress >= ach.targetCount
        achievementDao.updateAchievement(
            ach.copy(
                currentProgress = newProgress,
                isUnlocked = unlocked
            )
        )
    }

    suspend fun claimDailyReward(): Pair<Boolean, Int> {
        val user = userDao.getUserProfile() ?: return Pair(false, 0)
        val now = System.currentTimeMillis()
        val oneDayMillis = 24 * 60 * 60 * 1000L
        val timeSinceLastClaim = now - user.lastDailyClaimTime

        // Check if 24 hours elapsed (or first claim)
        if (user.lastDailyClaimTime > 0 && timeSinceLastClaim < oneDayMillis) {
            return Pair(false, 0)
        }

        // Check if streak is broken (more than 48 hours)
        val streak = if (user.lastDailyClaimTime == 0L || timeSinceLastClaim < 2 * oneDayMillis) {
            (user.dailyStreakCount % 7) + 1
        } else {
            1
        }

        val rewardAmounts = listOf(100, 150, 200, 250, 300, 400, 500)
        val coins = rewardAmounts.getOrElse(streak - 1) { 100 }

        adjustCoins(coins.toLong(), "Daily Reward Day $streak")
        userDao.updateUser(
            user.copy(
                lastDailyClaimTime = now,
                dailyStreakCount = streak
            )
        )
        return Pair(true, coins)
    }

    private suspend fun seedLeaderboardEntries() {
        val globalEntries = listOf(
            LeaderboardEntry("lb_1", "ValkyriePrime", 4, 38, 412, 45200, "GLOBAL"),
            LeaderboardEntry("lb_2", "NeonStriker", 2, 34, 380, 41100, "GLOBAL"),
            LeaderboardEntry("lb_3", "CyberKnight", 1, 30, 345, 36000, "GLOBAL"),
            LeaderboardEntry("lb_4", "ApexRoll", 3, 27, 290, 31200, "GLOBAL"),
            LeaderboardEntry("lb_5", "MatrixGhost", 5, 25, 260, 28500, "GLOBAL"),
            LeaderboardEntry("lb_6", "QuantumDice", 6, 21, 210, 23400, "GLOBAL"),
            LeaderboardEntry("lb_7", "VoltRacer", 2, 19, 185, 19800, "GLOBAL"),
            LeaderboardEntry("lb_8", "SolarisX", 3, 16, 150, 16500, "GLOBAL")
        )
        val weeklyEntries = listOf(
            LeaderboardEntry("lb_w1", "NeonStriker", 2, 34, 28, 4100, "WEEKLY"),
            LeaderboardEntry("lb_w2", "CyberKnight", 1, 30, 24, 3600, "WEEKLY"),
            LeaderboardEntry("lb_w3", "ValkyriePrime", 4, 38, 22, 3200, "WEEKLY"),
            LeaderboardEntry("lb_w4", "EchoBlade", 5, 18, 19, 2800, "WEEKLY"),
            LeaderboardEntry("lb_w5", "VoltRacer", 2, 19, 15, 2100, "WEEKLY")
        )
        val friendsEntries = listOf(
            LeaderboardEntry("lb_f1", "CyberKnight (You)", 1, 1, 0, 150, "FRIENDS"),
            LeaderboardEntry("lb_f2", "Aria_Star", 2, 12, 18, 2400, "FRIENDS"),
            LeaderboardEntry("lb_f3", "Kaelen_007", 3, 15, 23, 3100, "FRIENDS"),
            LeaderboardEntry("lb_f4", "ZackSpeed", 4, 9, 11, 1500, "FRIENDS")
        )
        leaderboardDao.insertLeaderboard(globalEntries + weeklyEntries + friendsEntries)
    }

    private fun calculateLevel(xp: Long): Int {
        val lvl = (Math.sqrt(xp.toDouble() / 150.0)).toInt() + 1
        return maxOf(1, lvl)
    }

    companion object {
        val initialAchievementsList = listOf(
            AchievementItem("ach_first_game", "Initiation", "Complete your first match in NEXU Ludo", 1, 0, false, 200, 100, false),
            AchievementItem("ach_first_win", "Neon Champion", "Win your first match", 1, 0, false, 300, 150, false),
            AchievementItem("ach_first_capture", "Bounty Hunter", "Capture an opponent token", 1, 0, false, 150, 80, false),
            AchievementItem("ach_10_captures", "Token Obliterator", "Capture 10 opponent tokens in total", 10, 0, false, 500, 300, false),
            AchievementItem("ach_10_wins", "Master of the Grid", "Win 10 matches", 10, 0, false, 1000, 600, false),
            AchievementItem("ach_50_wins", "Legendary Tactician", "Win 50 matches", 50, 0, false, 3000, 2000, false),
            AchievementItem("ach_win_streak", "Unstoppable Surge", "Achieve a 3-game win streak", 3, 0, false, 800, 450, false)
        )
    }
}
