package com.davidpv.padelmatch.data.model

import com.davidpv.padelmatch.data.db.entity.PlayerEntity

data class PlayerStats(
    val player: PlayerEntity,
    val totalGames: Int,
    val wins: Int,
    val losses: Int,
    val winRatio: Float,
    val history: List<PlayerSessionEntry>,
    val sessionsAttended: Int
)
