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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AchievementItem
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
fun AchievementsScreen(viewModel: GameViewModel) {
    val achievements by viewModel.achievements.collectAsStateWithLifecycle()

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
                    text = "CYBER TROPHIES",
                    color = NexuEmeraldGreen,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
            }
        },
        bottomBar = {
            NexuBottomNavigation(
                currentScreen = AppScreen.ACHIEVEMENTS,
                onNavigate = { viewModel.navigateTo(it) }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "UNLOCK OPERATIONAL HONORS & REWARDS",
                    color = NexuTextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            items(achievements) { item ->
                AchievementCard(
                    item = item,
                    onClaim = { viewModel.claimAchievement(item.id) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun AchievementCard(
    item: AchievementItem,
    onClaim: () -> Unit
) {
    val isCompleted = item.currentProgress >= item.targetCount
    val progressFraction = (item.currentProgress.toFloat() / item.targetCount.toFloat()).coerceIn(0f, 1f)

    NexuCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = if (item.isUnlocked) NexuAmberGold.copy(alpha = 0.5f) else NexuSurfaceBorder,
        glowColor = if (item.isUnlocked && !item.isClaimed) NexuAmberGold else null
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Icon
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (item.isUnlocked) NexuAmberGold.copy(alpha = 0.2f) else NexuSurfaceVariant
                        )
                        .border(
                            1.dp,
                            if (item.isUnlocked) NexuAmberGold else NexuSurfaceBorder,
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (item.isUnlocked) Icons.Default.MilitaryTech else Icons.Default.Lock,
                        contentDescription = null,
                        tint = if (item.isUnlocked) NexuAmberGold else NexuTextMuted,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        color = NexuTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.description,
                        color = NexuTextSecondary,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    // Progress bar
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LinearProgressIndicator(
                            progress = { progressFraction },
                            modifier = Modifier
                                .weight(1f)
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = if (isCompleted) NexuEmeraldGreen else NexuElectricCyan,
                            trackColor = NexuSurfaceVariant,
                            strokeCap = StrokeCap.Round
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${item.currentProgress}/${item.targetCount}",
                            color = NexuTextMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Action / Reward Button
            if (item.isClaimed) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(NexuSurfaceVariant)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "CLAIMED",
                        color = NexuTextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else if (item.isUnlocked) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(NexuAmberGold)
                        .clickable(onClick = onClaim)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "CLAIM +${item.rewardCoins}",
                        color = NexuDarkBackground,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            } else {
                Text(
                    text = "+${item.rewardCoins} ⬡",
                    color = NexuAmberGold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
