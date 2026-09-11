package com.davidpv.padelmatch.data.model

import java.time.LocalDate

data class Season(val startYear: Int) {
    val label: String
        get() = "$startYear-${startYear + 1}"
    val startDate: String
        get() = "$startYear-09-01"
    val endDate: String
        get() = "${startYear + 1}-08-31"
}

sealed interface SeasonFilter {
    data object All : SeasonFilter
    data class Of(val season: Season) : SeasonFilter
}

fun currentSeason(today: LocalDate = LocalDate.now()): Season =
    Season(if (today.monthValue >= 9) today.year else today.year - 1)

fun seasonOf(date: String): Season? = try {
    val parsedDate = LocalDate.parse(date)
    Season(if (parsedDate.monthValue >= 9) parsedDate.year else parsedDate.year - 1)
} catch (_: Exception) {
    null
}

val SeasonFilter.label: String
    get() = when (this) {
        SeasonFilter.All -> "Todas las temporadas"
        is SeasonFilter.Of -> season.label
    }

val SeasonFilter.startDate: String
    get() = when (this) {
        SeasonFilter.All -> "0000-01-01"
        is SeasonFilter.Of -> season.startDate
    }

val SeasonFilter.endDate: String
    get() = when (this) {
        SeasonFilter.All -> "9999-12-31"
        is SeasonFilter.Of -> season.endDate
    }
