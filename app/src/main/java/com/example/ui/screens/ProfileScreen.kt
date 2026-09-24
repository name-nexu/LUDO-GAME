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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Brush
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(viewModel: GameViewModel) {
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val profile = userProfile

    var showEditNameDialog by remember { mutableStateOf(false) }
    var newNameInput by remember { mutableStateOf("") }

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
                    text = "OPERATIVE DOSSIER",
                    color = NexuElectricCyan,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
            }
        },
        bottomBar = {
            NexuBottomNavigation(
                currentScreen = AppScreen.PROFILE,
                onNavigate = { viewModel.navigateTo(it) }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Card with Avatar & Name
            item {
                NexuCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = NexuElectricCyan,
                    glowColor = NexuElectricCyan
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Avatar Badge
                        Box(
                            modifier = Modifier
                                .size(84.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.sweepGradient(
                                        listOf(NexuElectricCyan, NexuAmberGold, NexuEmeraldGreen, NexuElectricCyan)
                                    )
                                )
                                .padding(3.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(78.dp)
                                    .clip(CircleShape)
                                    .background(NexuSurfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "A${profile?.avatarId ?: 1}",
                                    color = NexuElectricCyan,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Avatar Picker Selection Row
                        Text("SELECT AVATAR", color = NexuTextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            (1..5).forEach { aId ->
                                val isSelected = (profile?.avatarId ?: 1) == aId
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) NexuElectricCyan else NexuSurfaceVariant)
                                        .clickable {
                                            if (profile != null) {
                                                CoroutineScope(Dispatchers.IO).launch {
                                                    viewModel.repository.updateUserProfile(profile.copy(avatarId = aId))
                                                }
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "A$aId",
                                        color = if (isSelected) NexuDarkBackground else NexuTextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Username + Edit icon
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = profile?.username ?: "CyberKnight",
                                color = NexuTextPrimary,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = {
                                newNameInput = profile?.username ?: ""
                                showEditNameDialog = true
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Name",
                                    tint = NexuElectricCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Text(
                            text = "Level ${profile?.level ?: 1} Operative • ${profile?.xp ?: 0} Total XP",
                            color = NexuAmberGold,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Statistics Grid
            item {
                Text(
                    text = "BATTLE STATISTICS",
                    color = NexuElectricCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
            }

            item {
                val totalGames = profile?.gamesPlayed ?: 0
                val wins = profile?.gamesWon ?: 0
                val winRate = if (totalGames > 0) (wins * 100 / totalGames) else 0

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard("GAMES PLAYED", "$totalGames", NexuTextPrimary, Modifier.weight(1f))
                    StatCard("VICTORIES", "$wins", NexuEmeraldGreen, Modifier.weight(1f))
                    StatCard("WIN RATE", "$winRate%", NexuAmberGold, Modifier.weight(1f))
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard("TOTAL CAPTURES", "${profile?.totalCaptures ?: 0}", NexuElectricCyan, Modifier.weight(1f))
                    StatCard("CURRENT STREAK", "${profile?.currentStreak ?: 0}", NexuAmberGold, Modifier.weight(1f))
                    StatCard("BEST STREAK", "${profile?.bestStreak ?: 0}", NexuEmeraldGreen, Modifier.weight(1f))
                }
            }

            // View Match History Button
            item {
                NexuButton(
                    text = "VIEW MATCH LOGS",
                    icon = Icons.Default.History,
                    accentColor = NexuElectricCyan,
                    onClick = { viewModel.navigateTo(AppScreen.MATCH_HISTORY) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Edit Name Dialog
    if (showEditNameDialog) {
        AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            containerColor = NexuSurface,
            title = {
                Text("Edit Call-Sign", color = NexuTextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                OutlinedTextField(
                    value = newNameInput,
                    onValueChange = { if (it.length <= 16) newNameInput = it },
                    label = { Text("Operative Name", color = NexuTextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NexuElectricCyan,
                        unfocusedBorderColor = NexuSurfaceBorder,
                        focusedTextColor = NexuTextPrimary,
                        unfocusedTextColor = NexuTextPrimary
                    ),
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newNameInput.isNotBlank() && profile != null) {
                        CoroutineScope(Dispatchers.IO).launch {
                            viewModel.repository.updateUserProfile(profile.copy(username = newNameInput.trim()))
                        }
                    }
                    showEditNameDialog = false
                }) {
                    Text("SAVE", color = NexuElectricCyan, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditNameDialog = false }) {
                    Text("CANCEL", color = NexuTextSecondary)
                }
            }
        )
    }
}

@Composable
fun StatCard(label: String, value: String, valueColor: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = NexuSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, NexuSurfaceBorder)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, color = NexuTextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, color = valueColor, fontSize = 17.sp, fontWeight = FontWeight.Black)
        }
    }
}
