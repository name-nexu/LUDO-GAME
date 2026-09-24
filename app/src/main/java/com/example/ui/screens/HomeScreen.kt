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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Games
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.engine.AiDifficulty
import com.example.ui.components.CyberHeader
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
fun HomeScreen(viewModel: GameViewModel) {
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val dailyStatus by viewModel.dailyRewardClaimStatus.collectAsStateWithLifecycle()

    var showAiSetupDialog by remember { mutableStateOf(false) }
    var showLocalSetupDialog by remember { mutableStateOf(false) }
    var showJoinRoomDialog by remember { mutableStateOf(false) }
    var roomCodeInput by remember { mutableStateOf("") }
    var joinRoomError by remember { mutableStateOf<String?>(null) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        containerColor = NexuDarkBackground,
        bottomBar = {
            NexuBottomNavigation(
                currentScreen = AppScreen.HOME,
                onNavigate = { viewModel.navigateTo(it) }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Profile & Coins
            item {
                CyberHeader(
                    username = userProfile?.username ?: "CyberKnight",
                    level = userProfile?.level ?: 1,
                    xp = userProfile?.xp ?: 120L,
                    coins = userProfile?.coins ?: 1000L,
                    avatarId = userProfile?.avatarId ?: 1,
                    onAvatarClick = { viewModel.navigateTo(AppScreen.PROFILE) },
                    onCoinsClick = { viewModel.claimDailyReward() }
                )
            }

            // Daily Reward Hero Banner
            item {
                DailyRewardBanner(
                    currentStreak = userProfile?.dailyStreakCount ?: 0,
                    onClaimClick = { viewModel.claimDailyReward() }
                )
            }

            // Game Modes Grid / Cards
            item {
                Text(
                    text = "SELECT MISSION MODE",
                    color = NexuElectricCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.2.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // 1. Quick Match
            item {
                ModeCard(
                    title = "Quick Match",
                    subtitle = "Instant matchmaking with online players worldwide",
                    icon = Icons.Default.Public,
                    accentColor = NexuElectricCyan,
                    badge = "ONLINE",
                    onClick = {
                        viewModel.setupQuickMatch(4)
                    }
                )
            }

            // 2. Play vs AI
            item {
                ModeCard(
                    title = "Play vs Cyber-AI",
                    subtitle = "Train your tactical skills against Easy, Medium or Hard bots",
                    icon = Icons.Default.Psychology,
                    accentColor = NexuEmeraldGreen,
                    badge = "OFFLINE OK",
                    onClick = { showAiSetupDialog = true }
                )
            }

            // 3. Local Multiplayer
            item {
                ModeCard(
                    title = "Local Pass & Play",
                    subtitle = "2, 3, or 4 players on one physical device",
                    icon = Icons.Default.Group,
                    accentColor = NexuAmberGold,
                    badge = "PARTY",
                    onClick = { showLocalSetupDialog = true }
                )
            }

            // 4. Private Room
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    NexuButton(
                        text = "Create Room",
                        icon = Icons.Default.PlayArrow,
                        accentColor = NexuElectricCyan,
                        onClick = { viewModel.setupPrivateRoom(4) },
                        modifier = Modifier.weight(1f)
                    )
                    NexuButton(
                        text = "Join Room",
                        icon = Icons.Default.VpnKey,
                        accentColor = NexuAmberGold,
                        onClick = { showJoinRoomDialog = true },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // AI Setup Dialog
    if (showAiSetupDialog) {
        AiSetupDialog(
            onDismiss = { showAiSetupDialog = false },
            onConfirm = { playerCount, difficulty ->
                showAiSetupDialog = false
                viewModel.startVsAiMatch(playerCount, difficulty)
            }
        )
    }

    // Local Pass & Play Setup Dialog
    if (showLocalSetupDialog) {
        LocalSetupDialog(
            onDismiss = { showLocalSetupDialog = false },
            onConfirm = { playerCount ->
                showLocalSetupDialog = false
                viewModel.startLocalMatch(playerCount)
            }
        )
    }

    // Join Room Dialog
    if (showJoinRoomDialog) {
        AlertDialog(
            onDismissRequest = { showJoinRoomDialog = false },
            containerColor = NexuSurface,
            title = {
                Text("Join Private Room", color = NexuTextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(
                        "Enter the 9-character Room Code shared by the host (e.g. NEXU-4821):",
                        color = NexuTextSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = roomCodeInput,
                        onValueChange = { roomCodeInput = it.uppercase() },
                        placeholder = { Text("NEXU-XXXX", color = NexuTextMuted) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NexuElectricCyan,
                            unfocusedBorderColor = NexuSurfaceBorder,
                            focusedTextColor = NexuTextPrimary,
                            unfocusedTextColor = NexuTextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (joinRoomError != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(joinRoomError!!, color = Color(0xFFFF3366), fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.joinRoomWithCode(roomCodeInput) { success, msg ->
                            if (success) {
                                showJoinRoomDialog = false
                            } else {
                                joinRoomError = msg
                            }
                        }
                    }
                ) {
                    Text("JOIN", color = NexuElectricCyan, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showJoinRoomDialog = false }) {
                    Text("CANCEL", color = NexuTextSecondary)
                }
            }
        )
    }

    // Daily Claim Notification Dialog
    if (dailyStatus != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissDailyDialog() },
            containerColor = NexuSurface,
            title = {
                Text("Daily Reward Protocol", color = NexuAmberGold, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(dailyStatus ?: "", color = NexuTextPrimary, fontSize = 15.sp)
            },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissDailyDialog() }) {
                    Text("AWESOME", color = NexuAmberGold, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
fun ModeCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    badge: String,
    onClick: () -> Unit
) {
    NexuCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        glowColor = accentColor,
        onClick = onClick
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(accentColor.copy(alpha = 0.15f))
                    .border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        color = NexuTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(accentColor.copy(alpha = 0.2f))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badge,
                            color = accentColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    color = NexuTextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
fun DailyRewardBanner(
    currentStreak: Int,
    onClaimClick: () -> Unit
) {
    NexuCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        borderColor = NexuAmberGold.copy(alpha = 0.7f),
        glowColor = NexuAmberGold,
        onClick = onClaimClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🎁 DAILY POWER PROTOCOL",
                        color = NexuAmberGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(NexuAmberGold.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "DAY ${(currentStreak % 7) + 1}/7",
                            color = NexuAmberGold,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Tap to claim up to 500 Coins & XP boost!",
                    color = NexuTextSecondary,
                    fontSize = 12.sp
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(NexuAmberGold)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "CLAIM",
                    color = NexuDarkBackground,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
fun AiSetupDialog(
    onDismiss: () -> Unit,
    onConfirm: (Int, AiDifficulty) -> Unit
) {
    var selectedPlayers by remember { mutableIntStateOf(4) }
    var selectedDifficulty by remember { mutableStateOf(AiDifficulty.MEDIUM) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NexuSurface,
        title = {
            Text("Vs Cyber-AI Setup", color = NexuTextPrimary, fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text("Total Players:", color = NexuTextSecondary, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(2, 3, 4).forEach { count ->
                        val isSelected = selectedPlayers == count
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) NexuEmeraldGreen else NexuSurfaceVariant)
                                .clickable { selectedPlayers = count }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$count Players",
                                color = if (isSelected) NexuDarkBackground else NexuTextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("AI Neural Difficulty:", color = NexuTextSecondary, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AiDifficulty.values().forEach { diff ->
                        val isSelected = selectedDifficulty == diff
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) NexuEmeraldGreen else NexuSurfaceVariant)
                                .clickable { selectedDifficulty = diff }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = diff.name,
                                color = if (isSelected) NexuDarkBackground else NexuTextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedPlayers, selectedDifficulty) }) {
                Text("START MATCH", color = NexuEmeraldGreen, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = NexuTextSecondary)
            }
        }
    )
}

@Composable
fun LocalSetupDialog(
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var selectedPlayers by remember { mutableIntStateOf(4) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NexuSurface,
        title = {
            Text("Pass & Play Setup", color = NexuTextPrimary, fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text("Number of Players on this device:", color = NexuTextSecondary, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(2, 3, 4).forEach { count ->
                        val isSelected = selectedPlayers == count
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) NexuAmberGold else NexuSurfaceVariant)
                                .clickable { selectedPlayers = count }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$count Players",
                                color = if (isSelected) NexuDarkBackground else NexuTextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedPlayers) }) {
                Text("LAUNCH", color = NexuAmberGold, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = NexuTextSecondary)
            }
        }
    )
}

@Composable
fun NexuBottomNavigation(
    currentScreen: AppScreen,
    onNavigate: (AppScreen) -> Unit
) {
    NavigationBar(
        containerColor = NexuSurface,
        modifier = Modifier.navigationBarsPadding()
    ) {
        NavigationBarItem(
            selected = currentScreen == AppScreen.HOME,
            onClick = { onNavigate(AppScreen.HOME) },
            icon = { Icon(Icons.Default.Games, contentDescription = "Play") },
            label = { Text("Play") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = NexuElectricCyan,
                selectedTextColor = NexuElectricCyan,
                indicatorColor = NexuSurfaceVariant,
                unselectedIconColor = NexuTextMuted,
                unselectedTextColor = NexuTextMuted
            )
        )
        NavigationBarItem(
            selected = currentScreen == AppScreen.LEADERBOARD,
            onClick = { onNavigate(AppScreen.LEADERBOARD) },
            icon = { Icon(Icons.Default.EmojiEvents, contentDescription = "Rank") },
            label = { Text("Rank") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = NexuAmberGold,
                selectedTextColor = NexuAmberGold,
                indicatorColor = NexuSurfaceVariant,
                unselectedIconColor = NexuTextMuted,
                unselectedTextColor = NexuTextMuted
            )
        )
        NavigationBarItem(
            selected = currentScreen == AppScreen.ACHIEVEMENTS,
            onClick = { onNavigate(AppScreen.ACHIEVEMENTS) },
            icon = { Icon(Icons.Default.MilitaryTech, contentDescription = "Trophies") },
            label = { Text("Trophies") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = NexuEmeraldGreen,
                selectedTextColor = NexuEmeraldGreen,
                indicatorColor = NexuSurfaceVariant,
                unselectedIconColor = NexuTextMuted,
                unselectedTextColor = NexuTextMuted
            )
        )
        NavigationBarItem(
            selected = currentScreen == AppScreen.PROFILE,
            onClick = { onNavigate(AppScreen.PROFILE) },
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
            label = { Text("Profile") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = NexuElectricCyan,
                selectedTextColor = NexuElectricCyan,
                indicatorColor = NexuSurfaceVariant,
                unselectedIconColor = NexuTextMuted,
                unselectedTextColor = NexuTextMuted
            )
        )
        NavigationBarItem(
            selected = currentScreen == AppScreen.SETTINGS,
            onClick = { onNavigate(AppScreen.SETTINGS) },
            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
            label = { Text("Settings") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = NexuTextPrimary,
                selectedTextColor = NexuTextPrimary,
                indicatorColor = NexuSurfaceVariant,
                unselectedIconColor = NexuTextMuted,
                unselectedTextColor = NexuTextMuted
            )
        )
    }
}
