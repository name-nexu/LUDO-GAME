package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.ui.theme.NexuTextMuted
import com.example.ui.theme.NexuTextPrimary
import com.example.ui.theme.NexuTextSecondary
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.GameViewModel

@Composable
fun AdminDashboardScreen(viewModel: GameViewModel) {
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()

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
                IconButton(onClick = { viewModel.navigateTo(AppScreen.SETTINGS) }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = NexuTextPrimary)
                }
                Text(
                    text = "SANDBOX ADMIN CONSOLE",
                    color = NexuAmberGold,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Live Status Card
            item {
                NexuCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = NexuAmberGold
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "DATABASE / USER STATUS",
                            color = NexuAmberGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Operative: ${profile?.username} (${profile?.id})",
                            color = NexuTextPrimary,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Coins Balance: %,d ⬡".format(profile?.coins ?: 0),
                            color = NexuAmberGold,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "XP: ${profile?.xp} • Level ${profile?.level}",
                            color = NexuElectricCyan,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Grant Virtual Coins
            item {
                Text(
                    text = "CURRENCY GRANT PROTOCOLS",
                    color = NexuElectricCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    NexuButton(
                        text = "+1,000 Coins",
                        accentColor = NexuAmberGold,
                        onClick = { viewModel.grantAdminCoins(1000) },
                        modifier = Modifier.weight(1f)
                    )
                    NexuButton(
                        text = "+5,000 Coins",
                        accentColor = NexuAmberGold,
                        onClick = { viewModel.grantAdminCoins(5000) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Reset Operations
            item {
                Text(
                    text = "MAINTENANCE & RESET",
                    color = NexuDanger,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            item {
                NexuCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = NexuDanger
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = NexuDanger)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Reset Local Progression", color = NexuDanger, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            "Reinitializes level, XP, and stats back to starting cadet defaults.",
                            color = NexuTextSecondary,
                            fontSize = 12.sp
                        )
                        NexuButton(
                            text = "RESET ALL DATA",
                            accentColor = NexuDanger,
                            onClick = { viewModel.resetAllData() },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Coin Transaction Ledger Log
            item {
                Text(
                    text = "LOCAL TRANSACTION LEDGER",
                    color = NexuTextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    transactions.take(8).forEach { tx ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(tx.reason, color = NexuTextSecondary, fontSize = 11.sp)
                            Text(
                                (if (tx.amount >= 0) "+" else "") + "${tx.amount} ⬡",
                                color = if (tx.amount >= 0) NexuAmberGold else NexuDanger,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
