package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.engine.GamePhase
import com.example.engine.LudoColor
import com.example.engine.LudoPlayer
import com.example.ui.components.GameEventBanner
import com.example.ui.components.LudoBoardView
import com.example.ui.components.NexuButton
import com.example.ui.components.NexuDiceView
import com.example.ui.components.TurnTimerRing
import com.example.ui.theme.NexuAmberGold
import com.example.ui.theme.NexuDarkBackground
import com.example.ui.theme.NexuElectricCyan
import com.example.ui.theme.NexuEmeraldGreen
import com.example.ui.theme.NexuSurface
import com.example.ui.theme.NexuSurfaceBorder
import com.example.ui.theme.NexuSurfaceVariant
import com.example.ui.theme.NexuTextMuted
import com.example.ui.theme.NexuTextPrimary
import com.example.ui.theme.NexuTextSecondary
import com.example.ui.theme.PlayerAmber
import com.example.ui.theme.PlayerCrimson
import com.example.ui.theme.PlayerCyan
import com.example.ui.theme.PlayerEmerald
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.GameViewModel

@Composable
fun GameScreen(viewModel: GameViewModel) {
    val snapshot by viewModel.gameSnapshot.collectAsStateWithLifecycle()
    val isRolling by viewModel.isDiceRolling.collectAsStateWithLifecycle()
    val remainingSeconds by viewModel.turnRemainingSeconds.collectAsStateWithLifecycle()
    val bannerMessage by viewModel.bannerMessage.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    var showPauseDialog by remember { mutableStateOf(false) }
    var selectedEmote by remember { mutableStateOf<String?>(null) }

    val snap = snapshot
    if (snap == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(NexuDarkBackground),
            contentAlignment = Alignment.Center
        ) {
            Text("Initializing Neural Grid...", color = NexuTextSecondary)
        }
        return
    }

    val activePlayer = snap.players[snap.activePlayerIndex]
    val activeColor = when (activePlayer.color) {
        LudoColor.CYAN -> PlayerCyan
        LudoColor.EMERALD -> PlayerEmerald
        LudoColor.AMBER -> PlayerAmber
        LudoColor.CRIMSON -> PlayerCrimson
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        containerColor = NexuDarkBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Top HUD: Opponent summary cards & Pause/Settings
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Mini Player Badges
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    snap.players.forEachIndexed { index, p ->
                        MiniPlayerStatusCard(
                            player = p,
                            isActive = index == snap.activePlayerIndex
                        )
                    }
                }

                // Pause / Audio toggle
                IconButton(onClick = { showPauseDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Pause,
                        contentDescription = "Pause",
                        tint = NexuTextPrimary
                    )
                }
            }

            // 2. Center: Event Banner & Ludo Board
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Floating Event Banner
                    GameEventBanner(
                        message = bannerMessage,
                        accentColor = activeColor,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    // 15x15 Ludo Board
                    LudoBoardView(
                        snapshot = snap,
                        onTokenClick = { tokenId ->
                            viewModel.onTokenSelected(tokenId)
                        },
                        modifier = Modifier.fillMaxWidth(0.96f)
                    )
                }
            }

            // 3. Bottom Control Console
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(18.dp),
                color = NexuSurface,
                border = androidx.compose.foundation.BorderStroke(1.5.dp, activeColor.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Turn Status Info
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(activeColor)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${activePlayer.name}'s Turn",
                                color = NexuTextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (activePlayer.isAi) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(activeColor.copy(alpha = 0.2f))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text("AI", color = activeColor, fontSize = 9.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        }

                        // Countdown Timer
                        TurnTimerRing(remainingSeconds = remainingSeconds)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Center Dice & Action Guidance
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column {
                            val promptText = when (snap.phase) {
                                GamePhase.WAITING_FOR_DICE -> if (activePlayer.isAi) "AI is rolling..." else "TAP DICE TO ROLL"
                                GamePhase.ROLLING_DICE -> "Rolling..."
                                GamePhase.SELECTING_TOKEN -> if (activePlayer.isAi) "AI selecting token..." else "TAP GLOWING TOKEN"
                                GamePhase.TOKEN_MOVING -> "Advancing token..."
                                GamePhase.GAME_OVER -> "MATCH FINISHED"
                            }
                            Text(
                                text = promptText,
                                color = activeColor,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.5.sp
                            )
                            if (snap.lastDiceValue != null) {
                                Text(
                                    text = "Rolled: ${snap.lastDiceValue}",
                                    color = NexuTextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        // Interactive 3D Animated Dice
                        NexuDiceView(
                            diceValue = snap.lastDiceValue,
                            isRolling = isRolling,
                            isSelectable = snap.phase == GamePhase.WAITING_FOR_DICE && !activePlayer.isAi,
                            onRollClick = { viewModel.onDiceClick() },
                            playerAccentColor = activeColor
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Emote Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        listOf("🔥 Let's Go!", "🎲 Need a 6!", "⚡ Safe Zone!", "🏆 GG!").forEach { emote ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(NexuSurfaceVariant)
                                    .clickable {
                                        selectedEmote = emote
                                        viewModel.soundManager.playButtonClick()
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(text = emote, color = NexuTextSecondary, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    // Pause Dialog
    if (showPauseDialog) {
        AlertDialog(
            onDismissRequest = { showPauseDialog = false },
            containerColor = NexuSurface,
            title = {
                Text("Match Paused", color = NexuTextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text("Game is currently active. Choose an action:", color = NexuTextSecondary, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Sound FX", color = NexuTextPrimary, fontSize = 14.sp)
                        IconButton(
                            onClick = {
                                val current = settings?.soundEnabled ?: true
                                viewModel.updateSettings(
                                    sound = !current,
                                    vibration = settings?.vibrationEnabled ?: true,
                                    sfxVol = settings?.sfxVolume ?: 1f,
                                    motion = settings?.reducedMotion ?: false
                                )
                            }
                        ) {
                            Icon(
                                imageVector = if (settings?.soundEnabled != false) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                                contentDescription = "Toggle Sound",
                                tint = NexuElectricCyan
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPauseDialog = false }) {
                    Text("RESUME", color = NexuElectricCyan, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showPauseDialog = false
                        viewModel.navigateTo(AppScreen.HOME)
                    }
                ) {
                    Text("QUIT MATCH", color = Color(0xFFFF3366), fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
fun MiniPlayerStatusCard(
    player: LudoPlayer,
    isActive: Boolean
) {
    val color = when (player.color) {
        LudoColor.CYAN -> PlayerCyan
        LudoColor.EMERALD -> PlayerEmerald
        LudoColor.AMBER -> PlayerAmber
        LudoColor.CRIMSON -> PlayerCrimson
    }

    val homeCount = player.tokens.count { it.isHome }
    val yardCount = player.tokens.count { it.isInYard }

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isActive) color.copy(alpha = 0.25f) else NexuSurfaceVariant,
        border = androidx.compose.foundation.BorderStroke(
            width = if (isActive) 1.5.dp else 0.5.dp,
            color = if (isActive) color else NexuSurfaceBorder
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Column {
                Text(
                    text = player.name.take(6),
                    color = if (isActive) color else NexuTextPrimary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "🏠$yardCount ⭐$homeCount",
                    color = NexuTextSecondary,
                    fontSize = 9.sp
                )
            }
        }
    }
}
