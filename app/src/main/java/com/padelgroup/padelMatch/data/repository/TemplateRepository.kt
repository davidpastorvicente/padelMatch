package com.davidpv.padelmatch.data.repository

import javax.inject.Inject
import javax.inject.Singleton

data class GameSlots(
    val pair1Slot1: Int,
    val pair1Slot2: Int,
    val pair2Slot1: Int,
    val pair2Slot2: Int
)

@Singleton
class TemplateRepository @Inject constructor() {

    private val templates: Map<Int, List<GameSlots>> = mapOf(
        4 to listOf(
            GameSlots(0, 1, 2, 3),
            GameSlots(0, 2, 1, 3),
            GameSlots(0, 3, 1, 2)
        ),
        5 to listOf(
            GameSlots(0, 1, 2, 3),
            GameSlots(0, 1, 2, 4),
            GameSlots(0, 1, 3, 4),
            GameSlots(0, 2, 1, 3),
            GameSlots(0, 2, 1, 4),
            GameSlots(0, 2, 3, 4),
            GameSlots(0, 3, 1, 2),
            GameSlots(0, 3, 1, 4),
            GameSlots(0, 3, 2, 4),
            GameSlots(0, 4, 1, 2),
            GameSlots(0, 4, 1, 3),
            GameSlots(0, 4, 2, 3),
            GameSlots(1, 2, 3, 4),
            GameSlots(1, 3, 2, 4),
            GameSlots(1, 4, 2, 3)
        ),
        6 to listOf(
            GameSlots(0, 1, 2, 3),
            GameSlots(0, 1, 4, 5),
            GameSlots(1, 2, 3, 4),
            GameSlots(0, 3, 2, 5),
            GameSlots(0, 4, 1, 5),
            GameSlots(0, 2, 4, 5),
            GameSlots(1, 3, 2, 4),
            GameSlots(0, 4, 3, 5),
            GameSlots(1, 4, 2, 5),
            GameSlots(0, 5, 2, 3),
            GameSlots(1, 4, 3, 5),
            GameSlots(0, 3, 1, 2),
            GameSlots(0, 5, 2, 4),
            GameSlots(0, 2, 1, 3),
            GameSlots(1, 5, 3, 4)
        ),
        7 to listOf(
            GameSlots(0, 1, 2, 3),
            GameSlots(2, 5, 4, 6),
            GameSlots(0, 3, 1, 6),
            GameSlots(2, 4, 3, 5),
            GameSlots(0, 6, 1, 4),
            GameSlots(1, 2, 4, 5),
            GameSlots(0, 5, 3, 6),
            GameSlots(1, 2, 3, 4),
            GameSlots(0, 2, 5, 6),
            GameSlots(0, 4, 1, 3),
            GameSlots(1, 5, 2, 6),
            GameSlots(3, 5, 0, 6)
        )
    )

    fun getTemplate(playerCount: Int): List<GameSlots>? = templates[playerCount]

}
