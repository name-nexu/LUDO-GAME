package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import kotlin.random.Random

@Composable
fun NexuButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    accentColor: Color = NexuElectricCyan,
    testTag: String = "nexu_button"
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1.0f,
        animationSpec = tween(durationMillis = 100),
        label = "button_scale"
    )

    Surface(
        modifier = modifier
            .testTag(testTag)
            .scale(scale)
            .height(52.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled && !isLoading,
                onClick = onClick
            ),
        shape = RoundedCornerShape(14.dp),
        color = if (enabled) NexuSurfaceVariant else NexuSurface,
        border = BorderStroke(
            width = 1.5.dp,
            brush = if (enabled) {
                Brush.horizontalGradient(listOf(accentColor.copy(alpha = 0.8f), accentColor.copy(alpha = 0.3f)))
            } else {
                Brush.linearGradient(listOf(NexuSurfaceBorder, NexuSurfaceBorder))
            }
        ),
        shadowElevation = if (enabled) 6.dp else 0.dp
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        listOf(
                            (if (enabled) accentColor else Color.Transparent).copy(alpha = 0.15f),
                            Color.Transparent
                        )
                    )
                )
                .padding(horizontal = 20.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = accentColor,
                    strokeWidth = 2.5.dp
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (icon != null) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (enabled) accentColor else NexuTextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = text,
                        color = if (enabled) NexuTextPrimary else NexuTextMuted,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

@Composable
fun NexuCard(
    modifier: Modifier = Modifier,
    borderColor: Color = NexuSurfaceBorder,
    glowColor: Color? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val clickableModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else Modifier

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .then(clickableModifier),
        shape = RoundedCornerShape(16.dp),
        color = NexuSurface,
        border = BorderStroke(
            1.dp,
            glowColor?.let {
                Brush.horizontalGradient(listOf(it.copy(alpha = 0.6f), NexuSurfaceBorder))
            } ?: Brush.linearGradient(listOf(borderColor, borderColor.copy(alpha = 0.5f)))
        ),
        shadowElevation = 4.dp
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        listOf(
                            NexuSurfaceVariant.copy(alpha = 0.4f),
                            NexuSurface.copy(alpha = 0.9f)
                        )
                    )
                )
                .padding(16.dp)
        ) {
            content()
        }
    }
}

@Composable
fun CyberHeader(
    username: String,
    level: Int,
    xp: Long,
    coins: Long,
    avatarId: Int,
    onAvatarClick: () -> Unit = {},
    onCoinsClick: () -> Unit = {}
) {
    val xpInCurrentLevel = xp % 300
    val xpProgress = (xpInCurrentLevel.toFloat() / 300f).coerceIn(0f, 1f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Player Info Box
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .clickable(onClick = onAvatarClick)
                .padding(4.dp)
        ) {
            // Avatar badge
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.sweepGradient(
                            listOf(
                                NexuElectricCyan,
                                NexuAmberGold,
                                NexuEmeraldGreen,
                                NexuElectricCyan
                            )
                        )
                    )
                    .padding(2.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(NexuSurfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "A$avatarId",
                        color = NexuElectricCyan,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = username,
                        color = NexuTextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(NexuElectricCyan.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "LVL $level",
                            color = NexuElectricCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LinearProgressIndicator(
                        progress = { xpProgress },
                        modifier = Modifier
                            .width(80.dp)
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = NexuAmberGold,
                        trackColor = NexuSurfaceBorder,
                        strokeCap = StrokeCap.Round
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$xpInCurrentLevel/300 XP",
                        color = NexuTextSecondary,
                        fontSize = 10.sp
                    )
                }
            }
        }

        // Coins Chip
        Surface(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable(onClick = onCoinsClick),
            shape = RoundedCornerShape(12.dp),
            color = NexuSurfaceVariant,
            border = BorderStroke(1.dp, NexuAmberGold.copy(alpha = 0.6f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(NexuAmberGold),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "⬡",
                        color = NexuDarkBackground,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "%,d".format(coins),
                    color = NexuAmberGold,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
fun NexuDiceView(
    diceValue: Int?,
    isRolling: Boolean,
    isSelectable: Boolean,
    onRollClick: () -> Unit,
    modifier: Modifier = Modifier,
    playerAccentColor: Color = NexuElectricCyan
) {
    val rotation = remember { Animatable(0f) }
    val scale = remember { Animatable(1f) }

    LaunchedEffect(isRolling) {
        if (isRolling) {
            // Rapid 3D tumbling rotation
            rotation.animateTo(
                targetValue = rotation.value + 720f,
                animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing)
            )
            scale.animateTo(1.15f, tween(100))
            scale.animateTo(1.0f, tween(150))
        }
    }

    val displayValue = if (isRolling) Random.nextInt(1, 7) else (diceValue ?: 1)

    Box(
        modifier = modifier
            .testTag("nexu_dice")
            .size(72.dp)
            .scale(scale.value)
            .rotate(rotation.value)
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        NexuSurfaceVariant,
                        NexuSurface
                    )
                )
            )
            .border(
                width = if (isSelectable) 2.5.dp else 1.dp,
                brush = if (isSelectable) {
                    Brush.sweepGradient(
                        listOf(playerAccentColor, NexuAmberGold, playerAccentColor)
                    )
                } else {
                    Brush.linearGradient(listOf(NexuSurfaceBorder, NexuSurfaceBorder))
                },
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(
                enabled = isSelectable && !isRolling,
                onClick = onRollClick
            )
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        // Glowing pip matrix
        DiceFace(
            value = displayValue,
            tint = if (isSelectable) playerAccentColor else NexuTextPrimary
        )
    }
}

@Composable
fun DiceFace(value: Int, tint: Color) {
    Box(modifier = Modifier.size(50.dp)) {
        val pipSize = 9.dp
        when (value) {
            1 -> {
                DicePip(Modifier.align(Alignment.Center), pipSize, tint)
            }
            2 -> {
                DicePip(Modifier.align(Alignment.TopStart), pipSize, tint)
                DicePip(Modifier.align(Alignment.BottomEnd), pipSize, tint)
            }
            3 -> {
                DicePip(Modifier.align(Alignment.TopStart), pipSize, tint)
                DicePip(Modifier.align(Alignment.Center), pipSize, tint)
                DicePip(Modifier.align(Alignment.BottomEnd), pipSize, tint)
            }
            4 -> {
                DicePip(Modifier.align(Alignment.TopStart), pipSize, tint)
                DicePip(Modifier.align(Alignment.TopEnd), pipSize, tint)
                DicePip(Modifier.align(Alignment.BottomStart), pipSize, tint)
                DicePip(Modifier.align(Alignment.BottomEnd), pipSize, tint)
            }
            5 -> {
                DicePip(Modifier.align(Alignment.TopStart), pipSize, tint)
                DicePip(Modifier.align(Alignment.TopEnd), pipSize, tint)
                DicePip(Modifier.align(Alignment.Center), pipSize, tint)
                DicePip(Modifier.align(Alignment.BottomStart), pipSize, tint)
                DicePip(Modifier.align(Alignment.BottomEnd), pipSize, tint)
            }
            6 -> {
                DicePip(Modifier.align(Alignment.TopStart), pipSize, tint)
                DicePip(Modifier.align(Alignment.TopEnd), pipSize, tint)
                DicePip(Modifier.align(Alignment.CenterStart), pipSize, tint)
                DicePip(Modifier.align(Alignment.CenterEnd), pipSize, tint)
                DicePip(Modifier.align(Alignment.BottomStart), pipSize, tint)
                DicePip(Modifier.align(Alignment.BottomEnd), pipSize, tint)
            }
        }
    }
}

@Composable
fun DicePip(modifier: Modifier, size: Dp, tint: Color) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(tint)
            .shadow(4.dp, CircleShape)
    )
}

@Composable
fun TurnTimerRing(
    remainingSeconds: Int,
    totalSeconds: Int = 25,
    modifier: Modifier = Modifier
) {
    val progress = (remainingSeconds.toFloat() / totalSeconds.toFloat()).coerceIn(0f, 1f)
    val ringColor = when {
        progress > 0.5f -> NexuElectricCyan
        progress > 0.2f -> NexuAmberGold
        else -> NexuDanger
    }

    Box(
        modifier = modifier.size(46.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier.size(46.dp),
            color = ringColor,
            trackColor = NexuSurfaceBorder,
            strokeWidth = 3.5.dp,
            strokeCap = StrokeCap.Round
        )
        Text(
            text = "$remainingSeconds",
            color = ringColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
fun GameEventBanner(
    message: String?,
    accentColor: Color = NexuElectricCyan,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = !message.isNullOrBlank(),
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut(),
        modifier = modifier
    ) {
        if (message != null) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = NexuSurfaceVariant,
                border = BorderStroke(1.5.dp, accentColor),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = message,
                        color = NexuTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
