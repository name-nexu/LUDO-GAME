package com.example.engine

import kotlin.random.Random

object LudoAi {

    fun chooseTokenToMove(
        snapshot: GameSnapshot,
        difficulty: AiDifficulty
    ): Int? {
        val validIds = snapshot.validTokenIds
        if (validIds.isEmpty()) return null
        if (validIds.size == 1) return validIds.first()

        val activePlayer = snapshot.players[snapshot.activePlayerIndex]
        val roll = snapshot.lastDiceValue ?: return validIds.first()

        return when (difficulty) {
            AiDifficulty.EASY -> validIds.random()

            AiDifficulty.MEDIUM -> chooseMediumMove(activePlayer, validIds, roll, snapshot)

            AiDifficulty.HARD -> chooseHardMove(activePlayer, validIds, roll, snapshot)
        }
    }

    private fun chooseMediumMove(
        player: LudoPlayer,
        validIds: List<Int>,
        roll: Int,
        snapshot: GameSnapshot
    ): Int {
        var bestTokenId = validIds.first()
        var highestScore = -1000

        for (tokenId in validIds) {
            val token = player.tokens.first { it.id == tokenId }
            var score = 0

            val targetStep = if (token.isInYard) 0 else token.step + roll

            // 1. Can finish home? (Very high priority)
            if (targetStep == 57) {
                score += 300
            }

            // 2. Can capture opponent? (High priority)
            if (targetStep in 0..50) {
                val targetGlobalIdx = (player.color.startOffset + targetStep) % 52
                val isSafe = targetGlobalIdx in LudoBoardCoordinates.safeTrackIndices
                if (!isSafe) {
                    val canCapture = snapshot.players.filterIndexed { idx, _ -> idx != snapshot.activePlayerIndex }
                        .any { opp -> opp.tokens.any { it.isTrack && it.globalTrackIndex() == targetGlobalIdx } }
                    if (canCapture) {
                        score += 250
                    }
                }
            }

            // 3. Exiting yard on 6
            if (token.isInYard && roll == 6) {
                score += 180
            }

            // 4. Moving to safe cell
            if (targetStep in 0..50) {
                val targetGlobalIdx = (player.color.startOffset + targetStep) % 52
                if (targetGlobalIdx in LudoBoardCoordinates.safeTrackIndices) {
                    score += 100
                }
            }

            // 5. Prefer advancing further tokens
            score += targetStep * 2

            if (score > highestScore) {
                highestScore = score
                bestTokenId = tokenId
            }
        }

        return bestTokenId
    }

    private fun chooseHardMove(
        player: LudoPlayer,
        validIds: List<Int>,
        roll: Int,
        snapshot: GameSnapshot
    ): Int {
        var bestTokenId = validIds.first()
        var highestScore = -10000

        for (tokenId in validIds) {
            val token = player.tokens.first { it.id == tokenId }
            var score = 0

            val targetStep = if (token.isInYard) 0 else token.step + roll

            // 1. Finishing home: supreme reward
            if (targetStep == 57) {
                score += 1000
            }

            // 2. Entering safe home stretch: immunity reward
            if (targetStep in 51..56 && token.step < 51) {
                score += 450
            }

            // 3. Capture opportunity
            if (targetStep in 0..50) {
                val targetGlobalIdx = (player.color.startOffset + targetStep) % 52
                val isSafe = targetGlobalIdx in LudoBoardCoordinates.safeTrackIndices
                if (!isSafe) {
                    val opponentTokens = snapshot.players
                        .filterIndexed { idx, _ -> idx != snapshot.activePlayerIndex }
                        .flatMap { opp -> opp.tokens.filter { it.isTrack && it.globalTrackIndex() == targetGlobalIdx } }

                    if (opponentTokens.isNotEmpty()) {
                        score += 600 + (opponentTokens.size * 50)
                    }
                }
            }

            // 4. Danger avoidance: if currently in danger on unsafe cell, reward moving away
            if (token.isTrack) {
                val currentGlobalIdx = token.globalTrackIndex()!!
                if (currentGlobalIdx !in LudoBoardCoordinates.safeTrackIndices) {
                    val inDanger = isVulnerableToOpponents(currentGlobalIdx, snapshot)
                    if (inDanger) {
                        score += 300 // Escape danger!
                    }
                }
            }

            // 5. Landing on a safe cell
            if (targetStep in 0..50) {
                val targetGlobalIdx = (player.color.startOffset + targetStep) % 52
                if (targetGlobalIdx in LudoBoardCoordinates.safeTrackIndices) {
                    score += 220
                } else {
                    // Penalty if landing right in front of an opponent (within 1..6 cells behind)
                    if (isVulnerableToOpponents(targetGlobalIdx, snapshot)) {
                        score -= 280
                    }
                }
            }

            // 6. Release from yard on 6
            if (token.isInYard && roll == 6) {
                val tokensInYard = player.tokens.count { it.isInYard }
                score += 260 + (tokensInYard * 40)
            }

            // 7. General progress bonus
            score += targetStep * 4

            if (score > highestScore) {
                highestScore = score
                bestTokenId = tokenId
            }
        }

        return bestTokenId
    }

    private fun isVulnerableToOpponents(globalIdx: Int, snapshot: GameSnapshot): Boolean {
        for (idx in snapshot.players.indices) {
            if (idx == snapshot.activePlayerIndex) continue
            val opp = snapshot.players[idx]
            for (t in opp.tokens) {
                if (!t.isTrack) continue
                val oppIdx = t.globalTrackIndex() ?: continue
                val dist = (globalIdx - oppIdx + 52) % 52
                if (dist in 1..6) {
                    return true
                }
            }
        }
        return false
    }
}
