package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.NexuButton
import com.example.ui.components.NexuCard
import com.example.ui.theme.NexuAmberGold
import com.example.ui.theme.NexuDanger
import com.example.ui.theme.NexuDarkBackground
import com.example.ui.theme.NexuElectricCyan
import com.example.ui.theme.NexuEmeraldGreen
import com.example.ui.theme.NexuSurface
import com.example.ui.theme.NexuSurfaceBorder
import com.example.ui.theme.NexuSurfaceVariant
import com.example.ui.theme.NexuTextMuted
import com.example.ui.theme.NexuTextPrimary
import com.example.ui.theme.NexuTextSecondary
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.GameViewModel

@Composable
fun ResultScreen(viewModel: GameViewModel) {
    val result by viewModel.matchResult.collectAsStateWithLifecycle()
    val trophyScale = remember { Animatable(0.5f) }

    LaunchedEffect(Unit) {
        trophyScale.animateTo(1.15f, tween(350))
        trophyScale.animateTo(1.0f, tween(150))
    }

    val res = result
    val isWin = res?.isUserWinner == true
    val primaryColor = if (isWin) NexuAmberGold else NexuElectricCyan

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
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                // Trophy / Medal Graphic
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .scale(trophyScale.value)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(primaryColor.copy(alpha = 0.3f), Color.Transparent)
                            )
                        )
                        .border(2.dp, primaryColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "Outcome",
                        tint = primaryColor,
                        modifier = Modifier.size(60.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (isWin) "🏆 VICTORY ACHIEVED!" else "MATCH CONCLUDED",
                    color = primaryColor,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Winner: ${res?.winnerName ?: "Operative"}",
                    color = NexuTextSecondary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Stats Dashboard
                NexuCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = primaryColor.copy(alpha = 0.6f),
                    glowColor = primaryColor
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "MISSION REPORT",
                            color = NexuTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatBox(
                                label = "COINS EARNED",
                                value = "+${res?.coinsEarned ?: 0}",
                                color = NexuAmberGold
                            )
                            StatBox(
                                label = "XP GAINED",
                                value = "+${res?.xpEarned ?: 0}",
                                color = NexuElectricCyan
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatBox(
                                label = "CAPTURES",
                                value = "${res?.capturesCount ?: 0}",
                                color = NexuEmeraldGreen
                            )
                            val durationSec = res?.matchDurationSeconds ?: 0
                            val mins = durationSec / 60
                            val secs = durationSec % 60
                            StatBox(
                                label = "DURATION",
                                value = "%02d:%02d".format(mins, secs),
                                color = NexuTextPrimary
                            )
                        }
                    }
                }
            }

            // Bottom Actions
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                NexuButton(
                    text = "REMATCH",
                    icon = Icons.Default.Replay,
                    accentColor = NexuEmeraldGreen,
                    onClick = {
                        viewModel.startVsAiMatch(4, com.example.engine.AiDifficulty.MEDIUM)
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                NexuButton(
                    text = "RETURN HOME",
                    icon = Icons.Default.Home,
                    accentColor = NexuElectricCyan,
                    onClick = {
                        viewModel.navigateTo(AppScreen.HOME)
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun StatBox(
    label: String,
    value: String,
    color: Color
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = NexuSurfaceVariant,
        border = androidx.compose.foundation.BorderStroke(1.dp, NexuSurfaceBorder)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                color = NexuTextMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                color = color,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}
