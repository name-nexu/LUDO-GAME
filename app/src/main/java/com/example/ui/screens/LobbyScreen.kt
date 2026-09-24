package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.engine.LudoColor
import com.example.engine.RoomMember
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
import com.example.ui.theme.PlayerAmber
import com.example.ui.theme.PlayerCrimson
import com.example.ui.theme.PlayerCyan
import com.example.ui.theme.PlayerEmerald
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.GameViewModel

@Composable
fun LobbyScreen(viewModel: GameViewModel) {
    val room by viewModel.currentRoom.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val currentRoom = room
    if (currentRoom == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(NexuDarkBackground),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("No active room found", color = NexuTextSecondary)
                Spacer(modifier = Modifier.height(12.dp))
                NexuButton(text = "Return Home", onClick = { viewModel.navigateTo(AppScreen.HOME) })
            }
        }
        return
    }

    val isHost = currentRoom.members.firstOrNull { it.id == userProfile?.id }?.isHost ?: true

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
                    text = "MISSION LOBBY",
                    color = NexuElectricCyan,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                // Room Code Card
                NexuCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = NexuElectricCyan,
                    glowColor = NexuElectricCyan
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "ROOM CODE",
                            color = NexuTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = currentRoom.roomCode,
                                color = NexuElectricCyan,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("NEXU Room Code", currentRoom.roomCode)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Room Code copied!", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy",
                                    tint = NexuElectricCyan
                                )
                            }
                        }
                        Text(
                            text = "Share this code with friends to let them join your match",
                            color = NexuTextMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Players List Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "OPERATIVES IN SQUAD",
                        color = NexuTextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "${currentRoom.members.size}/${currentRoom.maxPlayers}",
                        color = NexuElectricCyan,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Member Slot Cards
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(currentRoom.members) { member ->
                        MemberSlotCard(member = member)
                    }

                    // Empty slots
                    val emptySlots = currentRoom.maxPlayers - currentRoom.members.size
                    items(emptySlots) {
                        EmptySlotCard()
                    }
                }
            }

            // Bottom Actions
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (isHost) {
                    NexuButton(
                        text = "START MATCH",
                        icon = Icons.Default.PlayArrow,
                        accentColor = NexuEmeraldGreen,
                        onClick = { viewModel.startLobbyMatch() },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = NexuElectricCyan,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Waiting for Host to start match...",
                            color = NexuTextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }

                NexuButton(
                    text = "LEAVE LOBBY",
                    accentColor = NexuTextMuted,
                    onClick = {
                        viewModel.multiplayerEngine.leaveRoom()
                        viewModel.navigateTo(AppScreen.HOME)
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun MemberSlotCard(member: RoomMember) {
    val colorAccent = when (member.color) {
        LudoColor.CYAN -> PlayerCyan
        LudoColor.EMERALD -> PlayerEmerald
        LudoColor.AMBER -> PlayerAmber
        LudoColor.CRIMSON -> PlayerCrimson
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = NexuSurfaceVariant,
        border = androidx.compose.foundation.BorderStroke(1.dp, colorAccent.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(colorAccent.copy(alpha = 0.2f))
                        .border(1.dp, colorAccent, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "A${member.avatarId}",
                        color = colorAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = member.name,
                            color = NexuTextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (member.isHost) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(NexuAmberGold.copy(alpha = 0.2f))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "HOST",
                                    color = NexuAmberGold,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                    Text(
                        text = member.color.displayName + " Squad",
                        color = colorAccent,
                        fontSize = 11.sp
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (member.isReady) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Ready",
                        tint = NexuEmeraldGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "READY",
                        color = NexuEmeraldGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.HourglassEmpty,
                        contentDescription = "Waiting",
                        tint = NexuAmberGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "WAITING",
                        color = NexuAmberGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun EmptySlotCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = NexuSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, NexuSurfaceBorder)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(NexuSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+",
                    color = NexuTextMuted,
                    fontSize = 18.sp
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Waiting for operative to join...",
                color = NexuTextMuted,
                fontSize = 13.sp
            )
        }
    }
}
