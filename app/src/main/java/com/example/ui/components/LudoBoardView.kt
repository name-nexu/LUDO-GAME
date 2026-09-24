package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.GamePhase
import com.example.engine.GameSnapshot
import com.example.engine.GridCoord
import com.example.engine.LudoBoardCoordinates
import com.example.engine.LudoColor
import com.example.engine.LudoPlayer
import com.example.engine.LudoToken
import com.example.ui.theme.NexuAmberGold
import com.example.ui.theme.NexuDarkBackground
import com.example.ui.theme.NexuElectricCyan
import com.example.ui.theme.NexuEmeraldGreen
import com.example.ui.theme.NexuStarSafe
import com.example.ui.theme.NexuSurface
import com.example.ui.theme.NexuSurfaceBorder
import com.example.ui.theme.NexuSurfaceVariant
import com.example.ui.theme.PlayerAmber
import com.example.ui.theme.PlayerCrimson
import com.example.ui.theme.PlayerCyan
import com.example.ui.theme.PlayerEmerald

@Composable
fun LudoBoardView(
    snapshot: GameSnapshot,
    onTokenClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val activePlayer = snapshot.players[snapshot.activePlayerIndex]
    val isSelecting = snapshot.phase == GamePhase.SELECTING_TOKEN
    val movableIds = if (isSelecting) snapshot.validTokenIds else emptyList()

    val pulseTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by pulseTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    BoxWithConstraints(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(20.dp))
            .background(NexuSurface)
            .border(2.dp, NexuSurfaceBorder, RoundedCornerShape(20.dp))
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        val boardSize = maxWidth
        val cellSize = boardSize / 15f

        // 1. Draw static background grid, 4 yards, central home and paths
        BoardCanvas(cellSize = cellSize)

        // 2. Render all tokens on top
        // Group tokens by cell coordinate so multiple tokens on same cell are offset
        val tokensWithCoords = mutableListOf<TokenRenderItem>()
        for (player in snapshot.players) {
            for (token in player.tokens) {
                if (token.isHome) continue // Finished tokens vanish into center core
                val coord = LudoBoardCoordinates.getTokenGridCoord(token)
                tokensWithCoords.add(
                    TokenRenderItem(
                        token = token,
                        owner = player,
                        coord = coord,
                        isSelectable = player.id == activePlayer.id && token.id in movableIds
                    )
                )
            }
        }

        // Group by coordinate
        val groupedByCoord = tokensWithCoords.groupBy { it.coord }

        for ((coord, items) in groupedByCoord) {
            items.forEachIndexed { index, item ->
                // Offset multiple tokens inside same cell
                val offsetFraction = if (items.size > 1) {
                    when (index) {
                        0 -> Offset(-0.18f, -0.18f)
                        1 -> Offset(0.18f, 0.18f)
                        2 -> Offset(0.18f, -0.18f)
                        else -> Offset(-0.18f, 0.18f)
                    }
                } else Offset.Zero

                val xPos = (coord.col.toFloat() + 0.5f + offsetFraction.x) * cellSize.value
                val yPos = (coord.row.toFloat() + 0.5f + offsetFraction.y) * cellSize.value

                TokenView(
                    item = item,
                    cellSize = cellSize,
                    pulseScale = if (item.isSelectable) pulseScale else 1.0f,
                    onClick = {
                        if (item.isSelectable) {
                            onTokenClick(item.token.id)
                        }
                    },
                    modifier = Modifier.offset(
                        x = (xPos - (cellSize.value * 0.45f)).dp,
                        y = (yPos - (cellSize.value * 0.45f)).dp
                    )
                )
            }
        }
    }
}

data class TokenRenderItem(
    val token: LudoToken,
    val owner: LudoPlayer,
    val coord: GridCoord,
    val isSelectable: Boolean
)

@Composable
fun TokenView(
    item: TokenRenderItem,
    cellSize: Dp,
    pulseScale: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tokenSize = cellSize * 0.9f
    val tokenColor = when (item.token.color) {
        LudoColor.CYAN -> PlayerCyan
        LudoColor.EMERALD -> PlayerEmerald
        LudoColor.AMBER -> PlayerAmber
        LudoColor.CRIMSON -> PlayerCrimson
    }

    Box(
        modifier = modifier
            .size(tokenSize)
            .scale(pulseScale)
            .testTag("token_${item.owner.color.name.lowercase()}_${item.token.id}")
            .clickable(
                enabled = item.isSelectable,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        // Outer Glow if selectable
        if (item.isSelectable) {
            Box(
                modifier = Modifier
                    .size(tokenSize * 1.25f)
                    .clip(CircleShape)
                    .background(tokenColor.copy(alpha = 0.4f))
            )
        }

        // Token Body
        Box(
            modifier = Modifier
                .size(tokenSize * 0.85f)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(
                            Color.White,
                            tokenColor,
                            tokenColor.copy(alpha = 0.8f)
                        )
                    )
                )
                .border(1.5.dp, Color.White, CircleShape)
                .shadow(elevation = 6.dp, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // Token Core Emblem / ID
            Box(
                modifier = Modifier
                    .size(tokenSize * 0.45f)
                    .clip(CircleShape)
                    .background(NexuDarkBackground),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${item.token.id + 1}",
                    color = tokenColor,
                    fontSize = (cellSize.value * 0.32f).sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
fun BoardCanvas(cellSize: Dp) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val cs = cellSize.toPx()

        // 1. Draw 4 Home Yards (6x6 cells each)
        // Top-Left: CYAN Yard
        drawYard(0f, 0f, cs, PlayerCyan, "CYAN BASE")
        // Top-Right: EMERALD Yard
        drawYard(9 * cs, 0f, cs, PlayerEmerald, "EMERALD BASE")
        // Bottom-Right: AMBER Yard
        drawYard(9 * cs, 9 * cs, cs, PlayerAmber, "AMBER BASE")
        // Bottom-Left: CRIMSON Yard
        drawYard(0f, 9 * cs, cs, PlayerCrimson, "CRIMSON BASE")

        // 2. Draw Track Grid & Colored Paths
        for (col in 0..14) {
            for (row in 0..14) {
                val isInYard = (col < 6 && row < 6) ||
                        (col > 8 && row < 6) ||
                        (col > 8 && row > 8) ||
                        (col < 6 && row > 8)
                val isCenter = col in 6..8 && row in 6..8

                if (!isInYard && !isCenter) {
                    val coord = GridCoord(col, row)
                    val isSafe = LudoBoardCoordinates.isSafeCell(coord)

                    // Determine if in colored home stretch
                    val homeColor = when {
                        row == 7 && col in 1..5 -> PlayerCyan
                        col == 7 && row in 1..5 -> PlayerEmerald
                        row == 7 && col in 9..13 -> PlayerAmber
                        col == 7 && row in 9..13 -> PlayerCrimson
                        // Starting squares
                        col == 1 && row == 6 -> PlayerCyan
                        col == 8 && row == 1 -> PlayerEmerald
                        col == 13 && row == 8 -> PlayerAmber
                        col == 6 && row == 13 -> PlayerCrimson
                        else -> null
                    }

                    val cellBg = homeColor?.copy(alpha = 0.35f) ?: NexuSurfaceVariant.copy(alpha = 0.6f)
                    val borderCol = homeColor?.copy(alpha = 0.8f) ?: NexuSurfaceBorder.copy(alpha = 0.7f)

                    drawRoundRect(
                        color = cellBg,
                        topLeft = Offset(col * cs + 1f, row * cs + 1f),
                        size = Size(cs - 2f, cs - 2f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
                    )
                    drawRoundRect(
                        color = borderCol,
                        topLeft = Offset(col * cs + 1f, row * cs + 1f),
                        size = Size(cs - 2f, cs - 2f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f),
                        style = Stroke(width = 1f)
                    )

                    // Safe star marker
                    if (isSafe) {
                        drawSafeStar(col * cs + cs / 2, row * cs + cs / 2, cs * 0.28f, NexuStarSafe)
                    }
                }
            }
        }

        // 3. Central Home Triangle Nexus
        drawCentralNexus(cs)
    }
}

fun DrawScope.drawYard(
    left: Float,
    top: Float,
    cellSizePx: Float,
    color: Color,
    label: String
) {
    val yardSize = 6 * cellSizePx

    // Yard Outer Box
    drawRoundRect(
        brush = Brush.verticalGradient(
            listOf(color.copy(alpha = 0.25f), color.copy(alpha = 0.10f)),
            startY = top,
            endY = top + yardSize
        ),
        topLeft = Offset(left + 2f, top + 2f),
        size = Size(yardSize - 4f, yardSize - 4f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f)
    )
    drawRoundRect(
        color = color.copy(alpha = 0.7f),
        topLeft = Offset(left + 2f, top + 2f),
        size = Size(yardSize - 4f, yardSize - 4f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f),
        style = Stroke(width = 2f)
    )

    // Inner White circular pad with 4 token slots
    val innerPadSize = 4 * cellSizePx
    val innerLeft = left + cellSizePx
    val innerTop = top + cellSizePx

    drawRoundRect(
        color = NexuSurface.copy(alpha = 0.9f),
        topLeft = Offset(innerLeft, innerTop),
        size = Size(innerPadSize, innerPadSize),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f)
    )
    drawRoundRect(
        color = color.copy(alpha = 0.4f),
        topLeft = Offset(innerLeft, innerTop),
        size = Size(innerPadSize, innerPadSize),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f),
        style = Stroke(width = 1.5f)
    )

    // 4 slot circles inside yard
    val offsets = listOf(
        Offset(left + 2.5f * cellSizePx, top + 2.5f * cellSizePx),
        Offset(left + 3.5f * cellSizePx, top + 2.5f * cellSizePx),
        Offset(left + 2.5f * cellSizePx, top + 3.5f * cellSizePx),
        Offset(left + 3.5f * cellSizePx, top + 3.5f * cellSizePx)
    )

    for (slot in offsets) {
        drawCircle(
            color = color.copy(alpha = 0.3f),
            radius = cellSizePx * 0.38f,
            center = slot
        )
        drawCircle(
            color = color.copy(alpha = 0.8f),
            radius = cellSizePx * 0.38f,
            center = slot,
            style = Stroke(width = 1.5f)
        )
    }
}

fun DrawScope.drawSafeStar(cx: Float, cy: Float, radius: Float, color: Color) {
    val path = Path()
    val points = 8
    val innerRadius = radius * 0.45f
    val step = Math.PI / points

    for (i in 0 until (2 * points)) {
        val r = if (i % 2 == 0) radius else innerRadius
        val angle = i * step - Math.PI / 2
        val x = (cx + r * Math.cos(angle)).toFloat()
        val y = (cy + r * Math.sin(angle)).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()

    drawPath(path, color)
}

fun DrawScope.drawCentralNexus(cellSizePx: Float) {
    val centerLeft = 6 * cellSizePx
    val centerTop = 6 * cellSizePx
    val centerSize = 3 * cellSizePx
    val centerMidX = 7.5f * cellSizePx
    val centerMidY = 7.5f * cellSizePx

    // 4 Triangles meeting in center
    // Left triangle: Cyan
    val pathCyan = Path().apply {
        moveTo(centerLeft, centerTop)
        lineTo(centerMidX, centerMidY)
        lineTo(centerLeft, centerTop + centerSize)
        close()
    }
    drawPath(pathCyan, PlayerCyan.copy(alpha = 0.4f))
    drawPath(pathCyan, PlayerCyan, style = Stroke(1.5f))

    // Top triangle: Emerald
    val pathEmerald = Path().apply {
        moveTo(centerLeft, centerTop)
        lineTo(centerMidX, centerMidY)
        lineTo(centerLeft + centerSize, centerTop)
        close()
    }
    drawPath(pathEmerald, PlayerEmerald.copy(alpha = 0.4f))
    drawPath(pathEmerald, PlayerEmerald, style = Stroke(1.5f))

    // Right triangle: Amber
    val pathAmber = Path().apply {
        moveTo(centerLeft + centerSize, centerTop)
        lineTo(centerMidX, centerMidY)
        lineTo(centerLeft + centerSize, centerTop + centerSize)
        close()
    }
    drawPath(pathAmber, PlayerAmber.copy(alpha = 0.4f))
    drawPath(pathAmber, PlayerAmber, style = Stroke(1.5f))

    // Bottom triangle: Crimson
    val pathCrimson = Path().apply {
        moveTo(centerLeft, centerTop + centerSize)
        lineTo(centerMidX, centerMidY)
        lineTo(centerLeft + centerSize, centerTop + centerSize)
        close()
    }
    drawPath(pathCrimson, PlayerCrimson.copy(alpha = 0.4f))
    drawPath(pathCrimson, PlayerCrimson, style = Stroke(1.5f))

    // Central core orb
    drawCircle(
        brush = Brush.radialGradient(
            listOf(Color.White, NexuElectricCyan, NexuDarkBackground),
            center = Offset(centerMidX, centerMidY),
            radius = cellSizePx * 0.8f
        ),
        radius = cellSizePx * 0.7f,
        center = Offset(centerMidX, centerMidY)
    )
    drawCircle(
        color = NexuAmberGold,
        radius = cellSizePx * 0.7f,
        center = Offset(centerMidX, centerMidY),
        style = Stroke(2f)
    )
}
