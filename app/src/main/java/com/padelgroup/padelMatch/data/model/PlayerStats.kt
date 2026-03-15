package com.padelgroup.padelMatch.data.model

import com.padelgroup.padelMatch.data.db.entity.PlayerEntity

data class PlayerStats(
    val player: PlayerEntity,
    val totalGames: Int,
    val wins: Int,
    val losses: Int,
    val winRatio: Float,
    val history: List<Float>
)
