package com.example.engine

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import kotlin.random.Random

enum class RoomType {
    LOCAL,
    VS_AI,
    QUICK_MATCH,
    PRIVATE_ROOM
}

data class RoomMember(
    val id: String,
    val name: String,
    val avatarId: Int,
    val color: LudoColor,
    val isHost: Boolean = false,
    val isReady: Boolean = true,
    val isAi: Boolean = false,
    val isConnected: Boolean = true
)

data class MultiplayerRoom(
    val roomCode: String,
    val roomType: RoomType,
    val maxPlayers: Int = 4,
    val members: List<RoomMember>,
    val rules: LudoRules = LudoRules(),
    val isGameStarted: Boolean = false,
    val gameEngine: LudoGameEngine? = null,
    val isReconnecting: Boolean = false
)

class MultiplayerEngine {

    private val _currentRoom = MutableStateFlow<MultiplayerRoom?>(null)
    val currentRoom: StateFlow<MultiplayerRoom?> = _currentRoom.asStateFlow()

    fun createRoom(
        type: RoomType,
        hostId: String,
        hostName: String,
        hostAvatar: Int,
        maxPlayers: Int = 4,
        preferredColor: LudoColor = LudoColor.CYAN,
        rules: LudoRules = LudoRules()
    ): MultiplayerRoom {
        val code = "NEXU-${Random.nextInt(1000, 9999)}"
        val hostMember = RoomMember(
            id = hostId,
            name = hostName,
            avatarId = hostAvatar,
            color = preferredColor,
            isHost = true,
            isReady = true
        )

        val members = mutableListOf(hostMember)
        if (type == RoomType.VS_AI) {
            // Add AI players to fill up to maxPlayers
            val availableColors = LudoColor.values().filter { it != preferredColor }
            val aiNames = listOf("NexusBot-Beta", "CyberAura", "MatrixOverlord")
            for (i in 0 until (maxPlayers - 1)) {
                members.add(
                    RoomMember(
                        id = "bot_${i + 1}",
                        name = aiNames.getOrElse(i) { "Bot ${i + 1}" },
                        avatarId = i + 2,
                        color = availableColors[i],
                        isHost = false,
                        isReady = true,
                        isAi = true
                    )
                )
            }
        }

        val room = MultiplayerRoom(
            roomCode = code,
            roomType = type,
            maxPlayers = maxPlayers,
            members = members,
            rules = rules
        )
        _currentRoom.value = room
        return room
    }

    fun joinRoomByCode(
        code: String,
        userId: String,
        userName: String,
        userAvatar: Int
    ): Result<MultiplayerRoom> {
        val cleanCode = code.trim().uppercase()
        if (!cleanCode.startsWith("NEXU-") && cleanCode.length != 9) {
            return Result.failure(IllegalArgumentException("Invalid Room Code format. Expected NEXU-XXXX"))
        }

        // Simulate joining or creating a simulated remote room if none exists with that code
        val existing = _currentRoom.value
        if (existing != null && existing.roomCode == cleanCode) {
            if (existing.members.size >= existing.maxPlayers) {
                return Result.failure(IllegalStateException("Room is full"))
            }
            if (existing.isGameStarted) {
                return Result.failure(IllegalStateException("Match has already begun"))
            }

            val takenColors = existing.members.map { it.color }
            val nextColor = LudoColor.values().firstOrNull { it !in takenColors } ?: LudoColor.AMBER
            val newMember = RoomMember(
                id = userId,
                name = userName,
                avatarId = userAvatar,
                color = nextColor,
                isHost = false,
                isReady = false
            )
            val updated = existing.copy(members = existing.members + newMember)
            _currentRoom.value = updated
            return Result.success(updated)
        } else {
            // Fresh simulated room for this code with pre-joined opponents
            val otherColors = listOf(LudoColor.EMERALD, LudoColor.AMBER, LudoColor.CRIMSON)
            val room = MultiplayerRoom(
                roomCode = cleanCode,
                roomType = RoomType.PRIVATE_ROOM,
                maxPlayers = 4,
                members = listOf(
                    RoomMember("host_rem", "AeroPilot", 2, LudoColor.CYAN, isHost = true, isReady = true),
                    RoomMember(userId, userName, userAvatar, otherColors[0], isHost = false, isReady = true),
                    RoomMember("player_3", "Valkyrie", 3, otherColors[1], isHost = false, isReady = true)
                )
            )
            _currentRoom.value = room
            return Result.success(room)
        }
    }

    fun startQuickMatch(
        userId: String,
        userName: String,
        userAvatar: Int,
        playerCount: Int = 4
    ): MultiplayerRoom {
        val code = "NEXU-${Random.nextInt(2000, 8999)}"
        val myColor = LudoColor.CYAN
        val otherColors = LudoColor.values().filter { it != myColor }
        val names = listOf("Starlight_09", "VoltStriker", "ApexPredator")

        val members = mutableListOf(
            RoomMember(userId, userName, userAvatar, myColor, isHost = true, isReady = true)
        )

        for (i in 0 until (playerCount - 1)) {
            members.add(
                RoomMember(
                    id = "qm_player_${i + 1}",
                    name = names.getOrElse(i) { "Player ${i + 2}" },
                    avatarId = (i + 3) % 6 + 1,
                    color = otherColors[i],
                    isHost = false,
                    isReady = true,
                    isAi = false
                )
            )
        }

        val room = MultiplayerRoom(
            roomCode = code,
            roomType = RoomType.QUICK_MATCH,
            maxPlayers = playerCount,
            members = members
        )
        _currentRoom.value = room
        return room
    }

    fun toggleReady(userId: String) {
        val room = _currentRoom.value ?: return
        val updated = room.members.map {
            if (it.id == userId) it.copy(isReady = !it.isReady) else it
        }
        _currentRoom.value = room.copy(members = updated)
    }

    fun launchGame(): LudoGameEngine? {
        val room = _currentRoom.value ?: return null
        val ludoPlayers = room.members.map { m ->
            LudoPlayer(
                id = m.id,
                name = m.name,
                color = m.color,
                isAi = m.isAi,
                aiDifficulty = if (m.isAi) AiDifficulty.HARD else AiDifficulty.MEDIUM,
                avatarId = m.avatarId,
                isHost = m.isHost,
                isReady = m.isReady,
                isConnected = m.isConnected
            )
        }

        val engine = LudoGameEngine(ludoPlayers, room.rules)
        _currentRoom.value = room.copy(isGameStarted = true, gameEngine = engine)
        return engine
    }

    fun handlePlayerDisconnect(userId: String) {
        val room = _currentRoom.value ?: return
        val updated = room.members.map {
            if (it.id == userId) it.copy(isConnected = false) else it
        }
        _currentRoom.value = room.copy(members = updated, isReconnecting = true)
    }

    fun handlePlayerReconnect(userId: String) {
        val room = _currentRoom.value ?: return
        val updated = room.members.map {
            if (it.id == userId) it.copy(isConnected = true) else it
        }
        _currentRoom.value = room.copy(members = updated, isReconnecting = false)
    }

    fun leaveRoom() {
        _currentRoom.value = null
    }
}
