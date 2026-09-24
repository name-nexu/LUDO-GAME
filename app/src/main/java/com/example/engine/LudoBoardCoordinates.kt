package com.example.engine

object LudoBoardCoordinates {

    // 52 Common Path Coordinates (col, row) on 15x15 grid (0..14)
    // Starting at Cyan's starting point (col 1, row 6) and proceeding clockwise
    val commonTrackCoords: List<GridCoord> = listOf(
        // Cyan arm top half going right (5 cells)
        GridCoord(1, 6), // 0: Cyan Start (Safe)
        GridCoord(2, 6), // 1
        GridCoord(3, 6), // 2
        GridCoord(4, 6), // 3
        GridCoord(5, 6), // 4

        // Up into top arm left side (6 cells)
        GridCoord(6, 5), // 5
        GridCoord(6, 4), // 6
        GridCoord(6, 3), // 7
        GridCoord(6, 2), // 8: Safe Star
        GridCoord(6, 1), // 9
        GridCoord(6, 0), // 10

        // Across top arm peak (2 cells)
        GridCoord(7, 0), // 11
        GridCoord(8, 0), // 12

        // Down top arm right side (5 cells)
        GridCoord(8, 1), // 13: Emerald Start (Safe)
        GridCoord(8, 2), // 14
        GridCoord(8, 3), // 15
        GridCoord(8, 4), // 16
        GridCoord(8, 5), // 17

        // Right into right arm top side (6 cells)
        GridCoord(9, 6),  // 18
        GridCoord(10, 6), // 19
        GridCoord(11, 6), // 20
        GridCoord(12, 6), // 21: Safe Star
        GridCoord(13, 6), // 22
        GridCoord(14, 6), // 23

        // Down right arm edge (2 cells)
        GridCoord(14, 7), // 24
        GridCoord(14, 8), // 25

        // Left along right arm bottom side (5 cells)
        GridCoord(13, 8), // 26: Amber Start (Safe)
        GridCoord(12, 8), // 27
        GridCoord(11, 8), // 28
        GridCoord(10, 8), // 29
        GridCoord(9, 8),  // 30

        // Down into bottom arm right side (6 cells)
        GridCoord(8, 9),  // 31
        GridCoord(8, 10), // 32
        GridCoord(8, 11), // 33
        GridCoord(8, 12), // 34: Safe Star
        GridCoord(8, 13), // 35
        GridCoord(8, 14), // 36

        // Across bottom arm peak (2 cells)
        GridCoord(7, 14), // 37
        GridCoord(6, 14), // 38

        // Up bottom arm left side (5 cells)
        GridCoord(6, 13), // 39: Crimson Start (Safe)
        GridCoord(6, 12), // 40
        GridCoord(6, 11), // 41
        GridCoord(6, 10), // 42
        GridCoord(6, 9),  // 43

        // Left into left arm bottom side (6 cells)
        GridCoord(5, 8), // 44
        GridCoord(4, 8), // 45
        GridCoord(3, 8), // 46
        GridCoord(2, 8), // 47: Safe Star
        GridCoord(1, 8), // 48
        GridCoord(0, 8), // 49

        // Up left arm edge (2 cells)
        GridCoord(0, 7), // 50
        GridCoord(0, 6)  // 51: Entry to Cyan home turn
    )

    // Safe track indices on the 52-cell circle
    val safeTrackIndices: Set<Int> = setOf(0, 8, 13, 21, 26, 34, 39, 47)

    // Private home stretch coordinates (steps 51..56) and home center (step 57)
    fun getHomeStretchCoord(color: LudoColor, step: Int): GridCoord {
        return when (color) {
            LudoColor.CYAN -> when (step) {
                51 -> GridCoord(1, 7)
                52 -> GridCoord(2, 7)
                53 -> GridCoord(3, 7)
                54 -> GridCoord(4, 7)
                55 -> GridCoord(5, 7)
                56 -> GridCoord(6, 7)
                else -> GridCoord(7, 7) // Final Center
            }
            LudoColor.EMERALD -> when (step) {
                51 -> GridCoord(7, 1)
                52 -> GridCoord(7, 2)
                53 -> GridCoord(7, 3)
                54 -> GridCoord(7, 4)
                55 -> GridCoord(7, 5)
                56 -> GridCoord(7, 6)
                else -> GridCoord(7, 7) // Final Center
            }
            LudoColor.AMBER -> when (step) {
                51 -> GridCoord(13, 7)
                52 -> GridCoord(12, 7)
                53 -> GridCoord(11, 7)
                54 -> GridCoord(10, 7)
                55 -> GridCoord(9, 7)
                56 -> GridCoord(8, 7)
                else -> GridCoord(7, 7) // Final Center
            }
            LudoColor.CRIMSON -> when (step) {
                51 -> GridCoord(7, 13)
                52 -> GridCoord(7, 12)
                53 -> GridCoord(7, 11)
                54 -> GridCoord(7, 10)
                55 -> GridCoord(7, 9)
                56 -> GridCoord(7, 8)
                else -> GridCoord(7, 7) // Final Center
            }
        }
    }

    // Yard slot coordinates for tokens in their home bases
    fun getYardSlotCoord(color: LudoColor, slotId: Int): GridCoord {
        return when (color) {
            LudoColor.CYAN -> when (slotId) {
                0 -> GridCoord(2, 2)
                1 -> GridCoord(3, 2)
                2 -> GridCoord(2, 3)
                else -> GridCoord(3, 3)
            }
            LudoColor.EMERALD -> when (slotId) {
                0 -> GridCoord(11, 2)
                1 -> GridCoord(12, 2)
                2 -> GridCoord(11, 3)
                else -> GridCoord(12, 3)
            }
            LudoColor.AMBER -> when (slotId) {
                0 -> GridCoord(11, 11)
                1 -> GridCoord(12, 11)
                2 -> GridCoord(11, 12)
                else -> GridCoord(12, 12)
            }
            LudoColor.CRIMSON -> when (slotId) {
                0 -> GridCoord(2, 11)
                1 -> GridCoord(3, 11)
                2 -> GridCoord(2, 12)
                else -> GridCoord(3, 12)
            }
        }
    }

    fun getTokenGridCoord(token: LudoToken): GridCoord {
        if (token.isInYard) {
            return getYardSlotCoord(token.color, token.id)
        }
        if (token.step <= 50) {
            val trackIdx = (token.color.startOffset + token.step) % 52
            return commonTrackCoords[trackIdx]
        }
        return getHomeStretchCoord(token.color, token.step)
    }

    fun isSafeCell(coord: GridCoord): Boolean {
        val idx = commonTrackCoords.indexOf(coord)
        return idx in safeTrackIndices
    }
}
