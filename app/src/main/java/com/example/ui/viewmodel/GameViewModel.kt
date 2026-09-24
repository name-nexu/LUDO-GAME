package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.SoundManager
import com.example.data.db.NexuDatabase
import com.example.data.model.AchievementItem
import com.example.data.model.CoinTransaction
import com.example.data.model.GameSettingsEntity
import com.example.data.model.LeaderboardEntry
import com.example.data.model.MatchHistoryItem
import com.example.data.model.UserProfile
import com.example.data.repository.GameRepository
import com.example.engine.AiDifficulty
import com.example.engine.GamePhase
import com.example.engine.GameSnapshot
import com.example.engine.LudoAi
import com.example.engine.LudoColor
import com.example.engine.LudoGameEngine
import com.example.engine.LudoPlayer
import com.example.engine.LudoRules
import com.example.engine.MultiplayerEngine
import com.example.engine.MultiplayerRoom
import com.example.engine.RoomType
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppScreen {
    HOME,
    LOBBY,
    GAME,
    RESULT,
    LEADERBOARD,
    PROFILE,
    ACHIEVEMENTS,
    MATCH_HISTORY,
    SETTINGS,
    ADMIN
}

data class MatchResultData(
    val winnerName: String,
    val isUserWinner: Boolean,
    val userRank: Int,
    val coinsEarned: Int,
    val xpEarned: Int,
    val capturesCount: Int,
    val matchDurationSeconds: Int
)

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val db = NexuDatabase.getDatabase(application)
    val repository = GameRepository(
        userDao = db.userDao(),
        coinTransactionDao = db.coinTransactionDao(),
        matchHistoryDao = db.matchHistoryDao(),
        achievementDao = db.achievementDao(),
        leaderboardDao = db.leaderboardDao(),
        settingsDao = db.settingsDao()
    )

    val soundManager = SoundManager(application)
    val multiplayerEngine = MultiplayerEngine()

    val userProfile: StateFlow<UserProfile?> = repository.userProfileFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val achievements: StateFlow<List<AchievementItem>> = repository.achievementsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val matchHistory: StateFlow<List<MatchHistoryItem>> = repository.matchHistoryFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val settings: StateFlow<GameSettingsEntity?> = repository.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val transactions: StateFlow<List<CoinTransaction>> = repository.transactionsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentScreen = MutableStateFlow(AppScreen.HOME)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _gameSnapshot = MutableStateFlow<GameSnapshot?>(null)
    val gameSnapshot: StateFlow<GameSnapshot?> = _gameSnapshot.asStateFlow()

    private val _isDiceRolling = MutableStateFlow(false)
    val isDiceRolling: StateFlow<Boolean> = _isDiceRolling.asStateFlow()

    private val _turnRemainingSeconds = MutableStateFlow(25)
    val turnRemainingSeconds: StateFlow<Int> = _turnRemainingSeconds.asStateFlow()

    private val _matchResult = MutableStateFlow<MatchResultData?>(null)
    val matchResult: StateFlow<MatchResultData?> = _matchResult.asStateFlow()

    private val _bannerMessage = MutableStateFlow<String?>(null)
    val bannerMessage: StateFlow<String?> = _bannerMessage.asStateFlow()

    private val _dailyRewardClaimStatus = MutableStateFlow<String?>(null)
    val dailyRewardClaimStatus: StateFlow<String?> = _dailyRewardClaimStatus.asStateFlow()

    val currentRoom: StateFlow<MultiplayerRoom?> = multiplayerEngine.currentRoom

    private var activeEngine: LudoGameEngine? = null
    private var timerJob: Job? = null
    private var aiJob: Job? = null
    private var matchStartTime: Long = 0L
    private var userCapturesThisMatch: Int = 0

    init {
        viewModelScope.launch {
            repository.initializeDefaults()
        }
        viewModelScope.launch {
            settings.collect { s ->
                if (s != null) {
                    soundManager.soundEnabled = s.soundEnabled
                    soundManager.vibrationEnabled = s.vibrationEnabled
                    soundManager.sfxVolume = s.sfxVolume
                }
            }
        }
    }

    fun navigateTo(screen: AppScreen) {
        soundManager.playButtonClick()
        _currentScreen.value = screen
    }

    // --- GAME INITIALIZATION & MODES ---
    fun startLocalMatch(playerCount: Int) {
        val user = userProfile.value
        val userName = user?.username ?: "Player 1"
        val colors = listOf(LudoColor.CYAN, LudoColor.EMERALD, LudoColor.AMBER, LudoColor.CRIMSON)

        val players = (0 until playerCount).map { i ->
            LudoPlayer(
                id = if (i == 0) (user?.id ?: "p1") else "local_p${i + 1}",
                name = if (i == 0) userName else "Player ${i + 1}",
                color = colors[i],
                isAi = false,
                avatarId = (i % 6) + 1
            )
        }

        launchEngine(players, "LOCAL")
    }

    fun startVsAiMatch(playerCount: Int, difficulty: AiDifficulty) {
        val user = userProfile.value
        val userName = user?.username ?: "CyberKnight"
        val colors = listOf(LudoColor.CYAN, LudoColor.EMERALD, LudoColor.AMBER, LudoColor.CRIMSON)
        val botNames = listOf("NexusAura", "QuantumGhost", "VortexAI")

        val players = mutableListOf(
            LudoPlayer(
                id = user?.id ?: "p1",
                name = userName,
                color = colors[0],
                isAi = false,
                avatarId = user?.avatarId ?: 1
            )
        )

        for (i in 0 until (playerCount - 1)) {
            players.add(
                LudoPlayer(
                    id = "bot_${i + 1}",
                    name = botNames.getOrElse(i) { "Bot ${i + 1}" },
                    color = colors[i + 1],
                    isAi = true,
                    aiDifficulty = difficulty,
                    avatarId = i + 2
                )
            )
        }

        launchEngine(players, "VS_AI")
    }

    fun setupQuickMatch(playerCount: Int) {
        val user = userProfile.value ?: return
        multiplayerEngine.startQuickMatch(user.id, user.username, user.avatarId, playerCount)
        _currentScreen.value = AppScreen.LOBBY
    }

    fun setupPrivateRoom(maxPlayers: Int = 4) {
        val user = userProfile.value ?: return
        multiplayerEngine.createRoom(
            type = RoomType.PRIVATE_ROOM,
            hostId = user.id,
            hostName = user.username,
            hostAvatar = user.avatarId,
            maxPlayers = maxPlayers
        )
        _currentScreen.value = AppScreen.LOBBY
    }

    fun joinRoomWithCode(code: String, onResult: (Boolean, String) -> Unit) {
        val user = userProfile.value ?: return
        val res = multiplayerEngine.joinRoomByCode(code, user.id, user.username, user.avatarId)
        res.onSuccess {
            _currentScreen.value = AppScreen.LOBBY
            onResult(true, "Joined room ${it.roomCode} successfully!")
        }.onFailure {
            onResult(false, it.message ?: "Failed to join room")
        }
    }

    fun startLobbyMatch() {
        val room = currentRoom.value ?: return
        val engine = multiplayerEngine.launchGame() ?: return
        val snap = engine.getSnapshot()
        activeEngine = engine
        _gameSnapshot.value = snap
        matchStartTime = System.currentTimeMillis()
        userCapturesThisMatch = 0
        _currentScreen.value = AppScreen.GAME
        startTurnTimer()
        checkAiTurn(snap)
    }

    private fun launchEngine(players: List<LudoPlayer>, mode: String) {
        val engine = LudoGameEngine(players)
        activeEngine = engine
        val snap = engine.getSnapshot()
        _gameSnapshot.value = snap
        matchStartTime = System.currentTimeMillis()
        userCapturesThisMatch = 0
        _currentScreen.value = AppScreen.GAME
        startTurnTimer()
        checkAiTurn(snap)
    }

    // --- GAMEPLAY ACTIONS ---
    fun onDiceClick() {
        val engine = activeEngine ?: return
        val snap = _gameSnapshot.value ?: return
        val currentP = snap.players[snap.activePlayerIndex]

        if (currentP.isAi || _isDiceRolling.value || snap.phase != GamePhase.WAITING_FOR_DICE) {
            return
        }

        performDiceRoll()
    }

    private fun performDiceRoll() {
        val engine = activeEngine ?: return
        _isDiceRolling.value = true
        soundManager.playDiceRoll()

        viewModelScope.launch {
            delay(400)
            _isDiceRolling.value = false
            val snap = engine.rollDice()
            _gameSnapshot.value = snap
            postEventBanner(snap.latestEvent?.message)

            // If auto advanced or single move
            if (snap.phase == GamePhase.WAITING_FOR_DICE) {
                // No valid moves, switched turn
                resetTurnTimer()
                checkAiTurn(snap)
            } else if (snap.phase == GamePhase.SELECTING_TOKEN) {
                if (snap.validTokenIds.size == 1 && !snap.players[snap.activePlayerIndex].isAi) {
                    // Optional auto-move if only 1 legal token
                    delay(250)
                    onTokenSelected(snap.validTokenIds.first())
                }
            }
        }
    }

    fun onTokenSelected(tokenId: Int) {
        val engine = activeEngine ?: return
        val snap = _gameSnapshot.value ?: return
        if (snap.phase != GamePhase.SELECTING_TOKEN) return

        val moveResult = engine.moveToken(tokenId)
        if (!moveResult.success) return

        soundManager.playTokenMove()
        if (moveResult.capturedPlayerColor != null) {
            soundManager.playCapture()
            if (snap.activePlayerIndex == 0) {
                userCapturesThisMatch++
            }
        } else if (moveResult.snapshot.latestEvent?.type == com.example.engine.EventType.SAFE_LANDING) {
            soundManager.playSafeLanding()
        }

        _gameSnapshot.value = moveResult.snapshot
        postEventBanner(moveResult.snapshot.latestEvent?.message)

        if (engine.isGameOver || moveResult.snapshot.winner != null) {
            handleGameEnd(moveResult.snapshot)
        } else {
            resetTurnTimer()
            checkAiTurn(moveResult.snapshot)
        }
    }

    private fun checkAiTurn(snapshot: GameSnapshot) {
        aiJob?.cancel()
        val p = snapshot.players[snapshot.activePlayerIndex]
        if (!p.isAi || engineIsOver()) return

        aiJob = viewModelScope.launch {
            delay(700)
            if (snapshot.phase == GamePhase.WAITING_FOR_DICE) {
                _isDiceRolling.value = true
                soundManager.playDiceRoll()
                delay(400)
                _isDiceRolling.value = false
                val rolledSnap = activeEngine?.rollDice() ?: return@launch
                _gameSnapshot.value = rolledSnap
                postEventBanner(rolledSnap.latestEvent?.message)

                if (rolledSnap.phase == GamePhase.SELECTING_TOKEN) {
                    delay(600)
                    val chosenTokenId = LudoAi.chooseTokenToMove(rolledSnap, p.aiDifficulty)
                    if (chosenTokenId != null) {
                        val moveRes = activeEngine?.moveToken(chosenTokenId) ?: return@launch
                        soundManager.playTokenMove()
                        if (moveRes.capturedPlayerColor != null) {
                            soundManager.playCapture()
                        }
                        _gameSnapshot.value = moveRes.snapshot
                        postEventBanner(moveRes.snapshot.latestEvent?.message)

                        if (activeEngine?.isGameOver == true) {
                            handleGameEnd(moveRes.snapshot)
                        } else {
                            resetTurnTimer()
                            checkAiTurn(moveRes.snapshot)
                        }
                    }
                } else {
                    resetTurnTimer()
                    checkAiTurn(rolledSnap)
                }
            }
        }
    }

    private fun engineIsOver(): Boolean {
        return activeEngine?.isGameOver == true
    }

    private fun handleGameEnd(snapshot: GameSnapshot) {
        timerJob?.cancel()
        aiJob?.cancel()
        soundManager.playVictory()

        val winner = snapshot.winner ?: snapshot.players.first()
        val isUserWinner = winner.id == (userProfile.value?.id ?: "p1")
        val userRank = if (isUserWinner) 1 else 2
        val durationSeconds = ((System.currentTimeMillis() - matchStartTime) / 1000).toInt().coerceAtLeast(10)

        val coinsEarned = if (isUserWinner) 500 else 100
        val xpEarned = if (isUserWinner) 250 else 60

        val resultData = MatchResultData(
            winnerName = winner.name,
            isUserWinner = isUserWinner,
            userRank = userRank,
            coinsEarned = coinsEarned,
            xpEarned = xpEarned,
            capturesCount = userCapturesThisMatch,
            matchDurationSeconds = durationSeconds
        )
        _matchResult.value = resultData

        viewModelScope.launch {
            repository.recordMatchCompleted(
                mode = currentRoom.value?.roomType?.name ?: "VS_AI",
                playerCount = snapshot.players.size,
                userRank = userRank,
                userColor = snapshot.players.first().color.name,
                capturesCount = userCapturesThisMatch,
                xpEarned = xpEarned,
                coinsEarned = coinsEarned,
                durationSeconds = durationSeconds
            )
        }

        _currentScreen.value = AppScreen.RESULT
    }

    private fun startTurnTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            _turnRemainingSeconds.value = 25
            while (_turnRemainingSeconds.value > 0 && !engineIsOver()) {
                delay(1000)
                _turnRemainingSeconds.value--
            }
            if (_turnRemainingSeconds.value <= 0 && !engineIsOver()) {
                // Timeout occurred
                val snap = activeEngine?.handleTurnTimeout()
                if (snap != null) {
                    _gameSnapshot.value = snap
                    resetTurnTimer()
                    checkAiTurn(snap)
                }
            }
        }
    }

    private fun resetTurnTimer() {
        startTurnTimer()
    }

    private fun postEventBanner(message: String?) {
        if (message.isNullOrBlank()) return
        _bannerMessage.value = message
        viewModelScope.launch {
            delay(2500)
            if (_bannerMessage.value == message) {
                _bannerMessage.value = null
            }
        }
    }

    fun claimAchievement(id: String) {
        viewModelScope.launch {
            val success = repository.claimAchievement(id)
            if (success) {
                soundManager.playSafeLanding()
                postEventBanner("Achievement reward claimed!")
            }
        }
    }

    fun claimDailyReward() {
        viewModelScope.launch {
            val res = repository.claimDailyReward()
            if (res.first) {
                soundManager.playVictory()
                _dailyRewardClaimStatus.value = "Claimed +${res.second} Coins!"
            } else {
                _dailyRewardClaimStatus.value = "Already claimed today! Return tomorrow."
            }
        }
    }

    fun dismissDailyDialog() {
        _dailyRewardClaimStatus.value = null
    }

    fun updateSettings(sound: Boolean, vibration: Boolean, sfxVol: Float, motion: Boolean) {
        viewModelScope.launch {
            repository.updateSettings(
                GameSettingsEntity(
                    soundEnabled = sound,
                    vibrationEnabled = vibration,
                    sfxVolume = sfxVol,
                    reducedMotion = motion
                )
            )
        }
    }

    fun resetAllData() {
        viewModelScope.launch {
            val user = userProfile.value ?: return@launch
            repository.updateUserProfile(
                user.copy(
                    level = 1,
                    xp = 100,
                    coins = 1000,
                    gamesPlayed = 0,
                    gamesWon = 0,
                    gamesLost = 0,
                    totalCaptures = 0,
                    currentStreak = 0,
                    bestStreak = 0
                )
            )
            postEventBanner("Data reset completed")
        }
    }

    fun grantAdminCoins(amount: Long) {
        viewModelScope.launch {
            repository.adjustCoins(amount, "Admin Sandbox Grant")
            postEventBanner("Granted +$amount Coins")
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        aiJob?.cancel()
    }
}
