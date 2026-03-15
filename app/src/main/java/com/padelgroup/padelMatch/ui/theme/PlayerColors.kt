package com.padelgroup.padelMatch.ui.theme

import androidx.compose.ui.graphics.Color

private val namedPlayerColors: Map<String, Pair<Color, Color>> = mapOf(
    "charlie" to (Color(0xFFC8E6C9) to Color(0xFF1B5E20)),  // green
    "rubén"   to (Color(0xFFFFF9C4) to Color(0xFF5C4A00)),  // yellow
    "ruben"   to (Color(0xFFFFF9C4) to Color(0xFF5C4A00)),  // yellow (no accent)
    "david"   to (Color(0xFFFFCDD2) to Color(0xFFB71C1C)),  // red
    "javi"    to (Color(0xFFBBDEFB) to Color(0xFF0D2B6E)),  // blue
    "iván"    to (Color(0xFFFFE0B2) to Color(0xFF7A3B00)),  // orange
    "ivan"    to (Color(0xFFFFE0B2) to Color(0xFF7A3B00)),  // orange (no accent)
    "sancho"  to (Color(0xFFE1BEE7) to Color(0xFF37006E)),  // purple
    "chito"   to (Color(0xFFD7CCC8) to Color(0xFF3E2723)),  // brown
)

private val fallbackPalette = listOf(
    Color(0xFFB5EAD7) to Color(0xFF1B5E42),
    Color(0xFFFFC8A2) to Color(0xFF7A2E00),
    Color(0xFFB5D0FF) to Color(0xFF0D2B6E),
    Color(0xFFFFD6E0) to Color(0xFF7A1630),
    Color(0xFFD4BBFF) to Color(0xFF37006E),
    Color(0xFFA2E0FF) to Color(0xFF003E5C),
    Color(0xFFD6EADF) to Color(0xFF1D4731),
    Color(0xFFE8D5B7) to Color(0xFF5C3A00),
)

fun playerColors(name: String): Pair<Color, Color> =
    namedPlayerColors[name.lowercase()]
        ?: fallbackPalette[kotlin.math.abs(name.hashCode()) % fallbackPalette.size]
