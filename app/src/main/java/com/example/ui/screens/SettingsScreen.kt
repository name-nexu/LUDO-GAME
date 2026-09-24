package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.example.ui.components.NexuButton
import com.example.ui.components.NexuCard
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
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.GameViewModel

@Composable
fun SettingsScreen(viewModel: GameViewModel) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    var showRulesDialog by remember { mutableStateOf(false) }

    val soundEnabled = settings?.soundEnabled ?: true
    val vibrationEnabled = settings?.vibrationEnabled ?: true
    val sfxVol = settings?.sfxVolume ?: 1.0f
    val reducedMotion = settings?.reducedMotion ?: false

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        containerColor = NexuDarkBackground,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.navigateTo(AppScreen.HOME) }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = NexuTextPrimary)
                }
                Text(
                    text = "SYSTEM SETTINGS",
                    color = NexuTextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
            }
        },
        bottomBar = {
            NexuBottomNavigation(
                currentScreen = AppScreen.SETTINGS,
                onNavigate = { viewModel.navigateTo(it) }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Audio & Haptics
            item {
                Text(
                    text = "AUDIO & HAPTICS",
                    color = NexuElectricCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            item {
                NexuCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.VolumeUp, contentDescription = null, tint = NexuElectricCyan)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Sound Effects", color = NexuTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    Text("Synthesized audio chimes & dice rolls", color = NexuTextSecondary, fontSize = 11.sp)
                                }
                            }
                            Switch(
                                checked = soundEnabled,
                                onCheckedChange = {
                                    viewModel.updateSettings(it, vibrationEnabled, sfxVol, reducedMotion)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = NexuDarkBackground,
                                    checkedTrackColor = NexuElectricCyan,
                                    uncheckedThumbColor = NexuTextMuted,
                                    uncheckedTrackColor = NexuSurfaceVariant
                                )
                            )
                        }

                        if (soundEnabled) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("SFX Volume", color = NexuTextSecondary, fontSize = 12.sp)
                                    Text("${(sfxVol * 100).toInt()}%", color = NexuElectricCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Slider(
                                    value = sfxVol,
                                    onValueChange = {
                                        viewModel.updateSettings(soundEnabled, vibrationEnabled, it, reducedMotion)
                                    },
                                    valueRange = 0f..1f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = NexuElectricCyan,
                                        activeTrackColor = NexuElectricCyan,
                                        inactiveTrackColor = NexuSurfaceVariant
                                    )
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Vibration, contentDescription = null, tint = NexuAmberGold)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Vibration Feedback", color = NexuTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    Text("Haptics on dice roll, capture & safe cell", color = NexuTextSecondary, fontSize = 11.sp)
                                }
                            }
                            Switch(
                                checked = vibrationEnabled,
                                onCheckedChange = {
                                    viewModel.updateSettings(soundEnabled, it, sfxVol, reducedMotion)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = NexuDarkBackground,
                                    checkedTrackColor = NexuAmberGold,
                                    uncheckedThumbColor = NexuTextMuted,
                                    uncheckedTrackColor = NexuSurfaceVariant
                                )
                            )
                        }
                    }
                }
            }

            // Rules Guide & Strategy
            item {
                Text(
                    text = "INFORMATION & GUIDE",
                    color = NexuElectricCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            item {
                NexuCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { showRulesDialog = true }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.MenuBook, contentDescription = null, tint = NexuEmeraldGreen)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Official Ludo Tactical Rules", color = NexuTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("Movement, safe cells, captures & victory guidelines", color = NexuTextSecondary, fontSize = 11.sp)
                        }
                    }
                }
            }

            // Developer / Admin Tools
            item {
                Text(
                    text = "ENGINE DIAGNOSTICS",
                    color = NexuElectricCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            item {
                NexuCard(
                    modifier = Modifier.fillMaxWidth(),
                    glowColor = Color(0xFFFF9900),
                    onClick = { viewModel.navigateTo(AppScreen.ADMIN) }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = NexuAmberGold)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Admin Sandbox Console", color = NexuAmberGold, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("Test multiplayer synchronization, grant coins & inspect state", color = NexuTextSecondary, fontSize = 11.sp)
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Rules Dialog
    if (showRulesDialog) {
        AlertDialog(
            onDismissRequest = { showRulesDialog = false },
            containerColor = NexuSurface,
            title = {
                Text("NEXU Ludo Rules Protocol", color = NexuEmeraldGreen, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("• Starting Out: A roll of 6 releases a token from the Yard onto your starting safe cell.", color = NexuTextSecondary, fontSize = 12.sp)
                    Text("• Safe Cells: 8 star-marked cells provide complete immunity against capture.", color = NexuTextSecondary, fontSize = 12.sp)
                    Text("• Capturing: Landing on an opponent token on an unsafe cell captures it, returning it to their base, and grants you an EXTRA TURN!", color = NexuTextSecondary, fontSize = 12.sp)
                    Text("• Sixes: Rolling a 6 grants an extra turn (maximum 2 times; 3 consecutive sixes forfeit the turn).", color = NexuTextSecondary, fontSize = 12.sp)
                    Text("• Home Victory: Navigate through the colored home stretch to the center core. First player to bring all 4 tokens home wins!", color = NexuTextSecondary, fontSize = 12.sp)
                }
            },
            confirmButton = {
                TextButton(onClick = { showRulesDialog = false }) {
                    Text("UNDERSTOOD", color = NexuEmeraldGreen, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
