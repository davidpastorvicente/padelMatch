@file:Suppress("unused")

package com.davidpv.padelmatch.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
data object HomeRoute

@Serializable
data object NewMatchRoute

@Serializable
data class EditResultsRoute(val sessionId: Long)

@Serializable
data class SessionDetailRoute(val sessionId: Long)

@Serializable
data class PlayerDetailRoute(val playerId: Long)

@Serializable
data object CombinedChartRoute

