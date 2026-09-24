package com.example.engine

enum class LudoColor(val hex: Long, val displayName: String, val startOffset: Int) {
    CYAN(0xFF00E5FF, "Cyan", 0),
    EMERALD(0xFF00E676, "Emerald", 13),
    AMBER(0xFFFFC107, "Amber", 26),
    CRIMSON(0xFFFF3864, "Crimson", 39)
}

enum class TokenState {
    YARD,
    ACTIVE,
    SAFE,
    HOME_STRETCH,
    FINISHED
}

data class LudoToken(
    val id: Int, // 0..3
    val color: LudoColor,
    val step: Int = -1, // -1 is yard, 0..50 is common path, 51..56 is home lane, 57 is finished
    val state: TokenState = TokenState.YARD
) {
    val isHome: Boolean get() = step >= 57
    val isInYard: Boolean get() = step == -1
    val isTrack: Boolean get() = step in 0..50
    val isInHomeStretch: Boolean get() = step in 51..56

    fun globalTrackIndex(): Int? {
        if (!isTrack) return null
        return (color.startOffset + step) % 52
    }
}

enum class AiDifficulty {
    EASY,
    MEDIUM,
    HARD
}

data class LudoPlayer(
    val id: String,
    val name: String,
    val color: LudoColor,
    val isAi: Boolean = false,
    val aiDifficulty: AiDifficulty = AiDifficulty.MEDIUM,
    val avatarId: Int = 1,
    val tokens: List<LudoToken> = List(4) { LudoToken(id = it, color = color) },
    val isHost: Boolean = false,
    val isReady: Boolean = true,
    val isConnected: Boolean = true
) {
    val finishedTokensCount: Int get() = tokens.count { it.isHome }
    val hasWon: Boolean get() = finishedTokensCount == 4
}

enum class GamePhase {
    WAITING_FOR_DICE,
    ROLLING_DICE,
    SELECTING_TOKEN,
    TOKEN_MOVING,
    GAME_OVER
}

data class GridCoord(val col: Int, val row: Int)

data class LudoRules(
    val rollSixToExitYard: Boolean = true,
    val bonusTurnOnSix: Boolean = true,
    val bonusTurnOnCapture: Boolean = true,
    val bonusTurnOnHome: Boolean = true,
    val maxConsecutiveSixes: Int = 3,
    val exactRollToFinish: Boolean = true,
    val turnTimeoutSeconds: Int = 25
)
