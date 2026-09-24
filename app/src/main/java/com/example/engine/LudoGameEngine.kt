package com.example.engine

import kotlin.random.Random

data class GameEvent(
    val type: EventType,
    val message: String,
    val playerIndex: Int,
    val tokenId: Int? = null,
    val value: Int? = null
)

enum class EventType {
    DICE_ROLLED,
    TOKEN_MOVED,
    TOKEN_CAPTURED,
    SAFE_LANDING,
    TOKEN_FINISHED,
    EXTRA_TURN_GRANTED,
    NO_VALID_MOVES,
    THREE_SIXES_PENALTY,
    PLAYER_WON
}

data class GameSnapshot(
    val stateVersion: Long,
    val players: List<LudoPlayer>,
    val activePlayerIndex: Int,
    val lastDiceValue: Int?,
    val consecutiveSixes: Int,
    val phase: GamePhase,
    val validTokenIds: List<Int>,
    val winner: LudoPlayer?,
    val ranking: List<LudoPlayer>,
    val latestEvent: GameEvent?
)

class LudoGameEngine(
    initialPlayers: List<LudoPlayer>,
    val rules: LudoRules = LudoRules()
) {
    private var stateVersion: Long = 1L
    private val players: MutableList<LudoPlayer> = initialPlayers.toMutableList()
    private var activePlayerIndex: Int = 0
    private var lastDiceValue: Int? = null
    private var consecutiveSixes: Int = 0
    private var phase: GamePhase = GamePhase.WAITING_FOR_DICE
    private var validTokenIds: List<Int> = emptyList()
    private var winner: LudoPlayer? = null
    private val ranking: MutableList<LudoPlayer> = mutableListOf()
    private var latestEvent: GameEvent? = null

    val currentPlayer: LudoPlayer get() = players[activePlayerIndex]
    val isGameOver: Boolean get() = winner != null || ranking.size >= players.size - 1

    fun getSnapshot(): GameSnapshot {
        return GameSnapshot(
            stateVersion = stateVersion,
            players = players.map { it.copy(tokens = it.tokens.toList()) },
            activePlayerIndex = activePlayerIndex,
            lastDiceValue = lastDiceValue,
            consecutiveSixes = consecutiveSixes,
            phase = phase,
            validTokenIds = validTokenIds.toList(),
            winner = winner,
            ranking = ranking.toList(),
            latestEvent = latestEvent
        )
    }

    /**
     * Rolls the dice for the current active player.
     * Can receive an authoritative dice value for server/test synchronization.
     */
    fun rollDice(authoritativeValue: Int? = null): GameSnapshot {
        if (phase != GamePhase.WAITING_FOR_DICE || isGameOver) {
            return getSnapshot()
        }

        val roll = authoritativeValue ?: Random.nextInt(1, 7)
        lastDiceValue = roll
        stateVersion++

        if (roll == 6) {
            consecutiveSixes++
            if (consecutiveSixes >= rules.maxConsecutiveSixes) {
                // Penalty for 3 consecutive sixes: turn is forfeited
                consecutiveSixes = 0
                latestEvent = GameEvent(
                    type = EventType.THREE_SIXES_PENALTY,
                    message = "${currentPlayer.name} rolled three 6s! Turn forfeited.",
                    playerIndex = activePlayerIndex,
                    value = roll
                )
                advanceToNextPlayer()
                return getSnapshot()
            }
        } else {
            consecutiveSixes = 0
        }

        val movableTokens = computeValidTokens(activePlayerIndex, roll)
        validTokenIds = movableTokens

        latestEvent = GameEvent(
            type = EventType.DICE_ROLLED,
            message = "${currentPlayer.name} rolled $roll",
            playerIndex = activePlayerIndex,
            value = roll
        )

        if (movableTokens.isEmpty()) {
            latestEvent = GameEvent(
                type = EventType.NO_VALID_MOVES,
                message = "${currentPlayer.name} has no valid moves with $roll",
                playerIndex = activePlayerIndex,
                value = roll
            )
            // Advance turn after no valid moves
            advanceToNextPlayer()
        } else {
            phase = GamePhase.SELECTING_TOKEN
        }

        return getSnapshot()
    }

    /**
     * Moves the specified token of the active player.
     */
    fun moveToken(tokenId: Int): MoveResult {
        if (phase != GamePhase.SELECTING_TOKEN || isGameOver) {
            return MoveResult(success = false, snapshot = getSnapshot())
        }

        if (tokenId !in validTokenIds) {
            return MoveResult(success = false, snapshot = getSnapshot())
        }

        val roll = lastDiceValue ?: return MoveResult(success = false, snapshot = getSnapshot())
        val player = currentPlayer
        val token = player.tokens.firstOrNull { it.id == tokenId }
            ?: return MoveResult(success = false, snapshot = getSnapshot())

        phase = GamePhase.TOKEN_MOVING
        stateVersion++

        // Calculate new step
        val startStep = token.step
        val endStep = if (token.isInYard) {
            0 // Enter track
        } else {
            token.step + roll
        }

        // Generate intermediate path steps for smooth animation
        val intermediateSteps = mutableListOf<Int>()
        if (token.isInYard) {
            intermediateSteps.add(0)
        } else {
            for (s in (startStep + 1)..endStep) {
                intermediateSteps.add(s)
            }
        }

        val newState = when {
            endStep >= 57 -> TokenState.FINISHED
            endStep in 51..56 -> TokenState.HOME_STRETCH
            else -> {
                val globalIdx = (player.color.startOffset + endStep) % 52
                if (globalIdx in LudoBoardCoordinates.safeTrackIndices) TokenState.SAFE else TokenState.ACTIVE
            }
        }

        val updatedToken = token.copy(step = endStep, state = newState)
        val updatedTokens = player.tokens.map { if (it.id == tokenId) updatedToken else it }
        players[activePlayerIndex] = player.copy(tokens = updatedTokens)

        var capturedOpponent: LudoPlayer? = null
        var capturedTokenId: Int? = null

        // Check for capture if landed on common track (0..50) and not safe
        if (endStep in 0..50) {
            val globalIdx = (player.color.startOffset + endStep) % 52
            val isSafe = globalIdx in LudoBoardCoordinates.safeTrackIndices

            if (!isSafe) {
                // Find opponents on the same cell
                for (pIdx in players.indices) {
                    if (pIdx == activePlayerIndex) continue
                    val opp = players[pIdx]
                    val victim = opp.tokens.firstOrNull { it.isTrack && it.globalTrackIndex() == globalIdx }
                    if (victim != null) {
                        // Capture victim! Send back to yard
                        val resetTokens = opp.tokens.map {
                            if (it.id == victim.id) it.copy(step = -1, state = TokenState.YARD) else it
                        }
                        players[pIdx] = opp.copy(tokens = resetTokens)
                        capturedOpponent = opp
                        capturedTokenId = victim.id
                        break // Single or primary capture
                    }
                }
            }
        }

        // Check if token finished home
        val justFinished = updatedToken.isHome && startStep < 57
        if (justFinished) {
            latestEvent = GameEvent(
                type = EventType.TOKEN_FINISHED,
                message = "${player.name} got a token HOME!",
                playerIndex = activePlayerIndex,
                tokenId = tokenId
            )
        }

        // Check if player won
        val playerWon = players[activePlayerIndex].tokens.all { it.isHome }
        if (playerWon && !ranking.contains(players[activePlayerIndex])) {
            ranking.add(players[activePlayerIndex])
            if (winner == null) {
                winner = players[activePlayerIndex]
                latestEvent = GameEvent(
                    type = EventType.PLAYER_WON,
                    message = "🏆 ${players[activePlayerIndex].name} WINS THE MATCH!",
                    playerIndex = activePlayerIndex
                )
            }
        }

        // Check extra turn
        val hasBonusTurn = when {
            playerWon -> false
            rules.bonusTurnOnCapture && capturedOpponent != null -> true
            rules.bonusTurnOnHome && justFinished -> true
            rules.bonusTurnOnSix && roll == 6 && consecutiveSixes < rules.maxConsecutiveSixes -> true
            else -> false
        }

        if (capturedOpponent != null) {
            latestEvent = GameEvent(
                type = EventType.TOKEN_CAPTURED,
                message = "${player.name} CAPTURED ${capturedOpponent.name}'s token!",
                playerIndex = activePlayerIndex,
                tokenId = capturedTokenId
            )
        } else if (newState == TokenState.SAFE) {
            latestEvent = GameEvent(
                type = EventType.SAFE_LANDING,
                message = "${player.name} landed on a SAFE cell",
                playerIndex = activePlayerIndex,
                tokenId = tokenId
            )
        }

        if (hasBonusTurn && !isGameOver) {
            latestEvent = GameEvent(
                type = EventType.EXTRA_TURN_GRANTED,
                message = "${player.name} earns an EXTRA TURN!",
                playerIndex = activePlayerIndex
            )
            phase = GamePhase.WAITING_FOR_DICE
            validTokenIds = emptyList()
        } else {
            advanceToNextPlayer()
        }

        return MoveResult(
            success = true,
            snapshot = getSnapshot(),
            pathSteps = intermediateSteps,
            capturedPlayerColor = capturedOpponent?.color,
            capturedTokenId = capturedTokenId,
            earnedExtraTurn = hasBonusTurn,
            isVictory = playerWon
        )
    }

    private fun advanceToNextPlayer() {
        if (isGameOver) {
            phase = GamePhase.GAME_OVER
            validTokenIds = emptyList()
            return
        }

        // Move to next player who hasn't finished all 4 tokens
        var nextIndex = (activePlayerIndex + 1) % players.size
        var attempts = 0
        while (players[nextIndex].hasWon && attempts < players.size) {
            nextIndex = (nextIndex + 1) % players.size
            attempts++
        }

        activePlayerIndex = nextIndex
        consecutiveSixes = 0
        lastDiceValue = null
        validTokenIds = emptyList()
        phase = GamePhase.WAITING_FOR_DICE
    }

    fun computeValidTokens(playerIndex: Int, roll: Int): List<Int> {
        val player = players.getOrNull(playerIndex) ?: return emptyList()
        if (player.hasWon) return emptyList()

        val validList = mutableListOf<Int>()
        for (token in player.tokens) {
            if (token.isHome) continue
            if (token.isInYard) {
                if (roll == 6) {
                    validList.add(token.id)
                }
            } else {
                if (token.step + roll <= 57) {
                    validList.add(token.id)
                }
            }
        }
        return validList
    }

    /**
     * For timeout or disconnect: forfeits turn or picks first legal move.
     */
    fun handleTurnTimeout(): GameSnapshot {
        if (phase == GamePhase.WAITING_FOR_DICE) {
            // Auto roll or skip
            advanceToNextPlayer()
        } else if (phase == GamePhase.SELECTING_TOKEN) {
            if (validTokenIds.isNotEmpty()) {
                moveToken(validTokenIds.first())
            } else {
                advanceToNextPlayer()
            }
        }
        return getSnapshot()
    }
}

data class MoveResult(
    val success: Boolean,
    val snapshot: GameSnapshot,
    val pathSteps: List<Int> = emptyList(),
    val capturedPlayerColor: LudoColor? = null,
    val capturedTokenId: Int? = null,
    val earnedExtraTurn: Boolean = false,
    val isVictory: Boolean = false
)
