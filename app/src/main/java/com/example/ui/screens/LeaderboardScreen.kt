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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.example.data.model.LeaderboardEntry
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
fun LeaderboardScreen(viewModel: GameViewModel) {
    var selectedCategory by remember { mutableStateOf("GLOBAL") }
    val entries by viewModel.repository.getLeaderboard(selectedCategory)
        .collectAsStateWithLifecycle(initialValue = emptyList())

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
                    text = "CYBER LEADERBOARDS",
                    color = NexuAmberGold,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
            }
        },
        bottomBar = {
            NexuBottomNavigation(
                currentScreen = AppScreen.LEADERBOARD,
                onNavigate = { viewModel.navigateTo(it) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Category Tabs: GLOBAL, WEEKLY, FRIENDS
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(NexuSurface)
                    .border(1.dp, NexuSurfaceBorder, RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("GLOBAL", "WEEKLY", "FRIENDS").forEach { cat ->
                    val isSelected = selectedCategory == cat
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) NexuAmberGold else Color.Transparent)
                            .clickable {
                                selectedCategory = cat
                                viewModel.soundManager.playButtonClick()
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = cat,
                            color = if (isSelected) NexuDarkBackground else NexuTextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Leaderboard Items
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(entries) { index, entry ->
                    LeaderboardRow(rank = index + 1, entry = entry)
                }
            }
        }
    }
}

@Composable
fun LeaderboardRow(rank: Int, entry: LeaderboardEntry) {
    val rankBadgeColor = when (rank) {
        1 -> NexuAmberGold
        2 -> Color(0xFFC0C0C0) // Silver
        3 -> Color(0xFFCD7F32) // Bronze
        else -> NexuSurfaceVariant
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = NexuSurface,
        border = androidx.compose.foundation.BorderStroke(
            width = if (rank <= 3) 1.5.dp else 1.dp,
            color = if (rank <= 3) rankBadgeColor.copy(alpha = 0.6f) else NexuSurfaceBorder
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Rank Number / Medal
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (rank <= 3) rankBadgeColor else NexuSurfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "#$rank",
                        color = if (rank <= 3) NexuDarkBackground else NexuTextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Avatar
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(NexuSurfaceVariant)
                        .border(1.dp, NexuElectricCyan, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "A${entry.avatarId}",
                        color = NexuElectricCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = entry.username,
                        color = NexuTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "LVL ${entry.level} • ${entry.wins} Wins",
                        color = NexuTextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            // XP
            Text(
                text = "${entry.xp} XP",
                color = NexuElectricCyan,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}
