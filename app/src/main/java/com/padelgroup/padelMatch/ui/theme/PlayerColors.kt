package com.davidpv.padelmatch.ui.theme

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Theme-aware colours for a player.
 * - [container] / [onContainer]: filled badges and selected chips.
 * - [content]: text and icons drawn directly on the app surface.
 * - [accent]: chart lines, dots and progress bars drawn on the app surface.
 */
@Immutable
data class PlayerColorSet(
    val container: Color,
    val onContainer: Color,
    val content: Color,
    val accent: Color
)

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

fun playerColors(name: String, darkTheme: Boolean): PlayerColorSet {
    val (container, onContainer) = namedPlayerColors[name.lowercase()]
        ?: fallbackPalette[kotlin.math.abs(name.hashCode()) % fallbackPalette.size]
    return if (darkTheme) {
        PlayerColorSet(container, onContainer, content = container, accent = container)
    } else {
        PlayerColorSet(container, onContainer, content = onContainer, accent = lerp(container, onContainer, 0.45f))
    }
}

@Composable
@ReadOnlyComposable
fun isAppInDarkTheme(): Boolean = MaterialTheme.colorScheme.surface.luminance() < 0.5f

@Composable
@ReadOnlyComposable
fun playerColors(name: String): PlayerColorSet = playerColors(name, isAppInDarkTheme())

@Composable
fun PlayerBadge(
    name: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.labelSmall,
    horizontalPadding: Dp = 8.dp,
    verticalPadding: Dp = 3.dp
) {
    val colors = playerColors(name)
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = colors.container,
        contentColor = colors.onContainer
    ) {
        Text(
            text = name,
            style = style,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = horizontalPadding, vertical = verticalPadding)
        )
    }
}
