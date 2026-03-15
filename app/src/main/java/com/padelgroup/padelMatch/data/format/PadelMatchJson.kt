package com.padelgroup.padelMatch.data.format

import kotlinx.serialization.Serializable

@Serializable
data class PadelMatchExport(
    val version: Int = 1,
    val players: List<JsonPlayer>,
    val sessions: List<JsonSession>
)

@Serializable
data class JsonPlayer(val id: Long, val name: String)

@Serializable
data class JsonSession(
    val date: String,
    val playerIds: List<Long>,
    val games: List<JsonGame>
)

@Serializable
data class JsonGame(
    val gameNumber: Int,
    val pair1: List<Long>,
    val pair2: List<Long>,
    val winner: Int? = null
)
