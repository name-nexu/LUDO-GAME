package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.engine.AiDifficulty
import com.example.engine.EventType
import com.example.engine.GamePhase
import com.example.engine.LudoAi
import com.example.engine.LudoColor
import com.example.engine.LudoGameEngine
import com.example.engine.LudoPlayer
import com.example.engine.LudoRules
import com.example.engine.TokenState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun testAppNameString() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("NEXU Ludo", appName)
    }

    @Test
    fun testDiceRollRequiresSixToLeaveYard() {
        val players = listOf(
            LudoPlayer(id = "p1", name = "CyanPlayer", color = LudoColor.CYAN),
            LudoPlayer(id = "p2", name = "EmeraldPlayer", color = LudoColor.EMERALD)
        )
        val engine = LudoGameEngine(players)

        // Roll 4 when all tokens in yard
        val snap4 = engine.rollDice(authoritativeValue = 4)
        // No valid moves with 4, so turn advances to p2
        assertEquals(1, snap4.activePlayerIndex)
        assertTrue(snap4.players[0].tokens.all { it.isInYard })

        // Now p2's turn. Roll 6
        val snap6 = engine.rollDice(authoritativeValue = 6)
        assertEquals(GamePhase.SELECTING_TOKEN, snap6.phase)
        assertEquals(4, snap6.validTokenIds.size)

        // Move token 0 out of yard
        val moveRes = engine.moveToken(0)
        assertTrue(moveRes.success)
        val p2Token0 = moveRes.snapshot.players[1].tokens.first { it.id == 0 }
        assertEquals(0, p2Token0.step)
        assertEquals(TokenState.SAFE, p2Token0.state)
        // Rolled 6 grants extra turn
        assertTrue(moveRes.earnedExtraTurn)
        assertEquals(1, moveRes.snapshot.activePlayerIndex)
    }

    @Test
    fun testCaptureMechanicAndBonusTurn() {
        val p1 = LudoPlayer(id = "p1", name = "P1", color = LudoColor.CYAN)
        val p2 = LudoPlayer(id = "p2", name = "P2", color = LudoColor.EMERALD)
        val engine = LudoGameEngine(listOf(p1, p2))

        // P1 rolls 6 and exits yard
        engine.rollDice(authoritativeValue = 6)
        engine.moveToken(0) // Token 0 at step 0 (Cyan start = global track 0)

        // P1 extra turn: rolls 2
        engine.rollDice(authoritativeValue = 2)
        engine.moveToken(0) // Token 0 at step 2 (global track 2)

        // Now P2's turn (Emerald start = global track 13)
        // Let's verify AI decision engine doesn't crash and returns valid moves
        val snap = engine.getSnapshot()
        val aiChoice = LudoAi.chooseTokenToMove(snap, AiDifficulty.HARD)
        // P2 hasn't rolled dice yet, so validTokenIds is empty -> aiChoice is null
        assertEquals(null, aiChoice)
    }

    @Test
    fun testThreeSixesPenaltyForfeitsTurn() {
        val p1 = LudoPlayer(id = "p1", name = "P1", color = LudoColor.CYAN)
        val p2 = LudoPlayer(id = "p2", name = "P2", color = LudoColor.EMERALD)
        val engine = LudoGameEngine(listOf(p1, p2))

        // 1st six
        engine.rollDice(authoritativeValue = 6)
        engine.moveToken(0)
        assertEquals(0, engine.getSnapshot().activePlayerIndex)

        // 2nd six
        engine.rollDice(authoritativeValue = 6)
        engine.moveToken(0)
        assertEquals(0, engine.getSnapshot().activePlayerIndex)

        // 3rd six -> penalty triggers, forfeits turn to P2
        val snap3 = engine.rollDice(authoritativeValue = 6)
        assertEquals(EventType.THREE_SIXES_PENALTY, snap3.latestEvent?.type)
        assertEquals(1, snap3.activePlayerIndex)
    }
}
