package com.davidpv.padelmatch.ui.statistics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import com.davidpv.padelmatch.data.model.PlayerSessionEntry
import com.davidpv.padelmatch.ui.theme.playerColors
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

/** One line in a win-ratio chart. [playerName] is shown in the tooltip when not null. */
data class WinRatioSeries(
    val playerName: String?,
    val color: Color,
    val entries: List<PlayerSessionEntry>,
    val xFractions: List<Float>
)

private val tooltipDateFormat = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.forLanguageTag("es"))

private fun epochDayOrNull(date: String): Long? =
    try { LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE).toEpochDay() } catch (_: Exception) { null }

fun winRatioEpochRange(entries: List<PlayerSessionEntry>): Pair<Long, Long>? {
    val epochs = entries.mapNotNull { epochDayOrNull(it.date) }
    return if (epochs.isEmpty()) null else epochs.min() to epochs.max()
}

/** Time-proportional X positions in [0, 1]. A zero-length range centers every point. */
fun winRatioXFractions(
    entries: List<PlayerSessionEntry>,
    epochRange: Pair<Long, Long>? = winRatioEpochRange(entries)
): List<Float> {
    val (minEpoch, maxEpoch) = epochRange ?: return entries.map { 0.5f }
    if (maxEpoch == minEpoch) return entries.map { 0.5f }
    val totalDays = (maxEpoch - minEpoch).toFloat()
    return entries.map { entry -> epochDayOrNull(entry.date)?.let { (it - minEpoch) / totalDays } ?: 0.5f }
}

fun DrawScope.drawWinRatioSeries(points: List<Offset>, color: Color) {
    points.zipWithNext { start, end -> drawLine(color = color, start = start, end = end, strokeWidth = 7f) }
    points.forEach { point ->
        drawCircle(color = color, radius = 7f, center = point)
        drawCircle(color = Color.White, radius = 3f, center = point)
    }
}

private const val PADDING_LEFT = 88f
private const val PADDING_RIGHT = 16f
private const val PADDING_TOP = 8f
private const val TAP_RADIUS_SQUARED = 3600f

private class ChartArea(size: Size) {
    val width = size.width
    val height = size.height
    private val chartW = width - PADDING_LEFT - PADDING_RIGHT
    private val chartH = height - PADDING_TOP

    fun x(fraction: Float) = PADDING_LEFT + fraction * chartW
    fun y(ratio: Float) = PADDING_TOP + chartH - ratio * chartH
    fun point(series: WinRatioSeries, index: Int) =
        Offset(x(series.xFractions[index]), y(series.entries[index].winRatio))
    fun points(series: WinRatioSeries) = series.entries.indices.map { point(series, it) }
}

/** Full-size, tappable win-ratio chart with a percentage axis and tooltips. */
@Composable
fun WinRatioChart(series: List<WinRatioSeries>, modifier: Modifier = Modifier) {
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val gridColorNormal = remember(onSurfaceVariant) { onSurfaceVariant.copy(alpha = 0.15f) }
    val gridColorMid = remember(onSurfaceVariant) { onSurfaceVariant.copy(alpha = 0.3f) }
    val axisPaint = remember(onSurfaceVariant) {
        android.graphics.Paint().apply {
            textSize = 30f
            color = onSurfaceVariant.copy(alpha = 0.7f).toArgb()
            textAlign = android.graphics.Paint.Align.RIGHT
        }
    }

    var canvasSize by remember { mutableStateOf(Size.Zero) }
    // Selected point as series index to entry index.
    var selected by remember(series) { mutableStateOf<Pair<Int, Int>?>(null) }

    Canvas(
        modifier = modifier
            .onSizeChanged { canvasSize = Size(it.width.toFloat(), it.height.toFloat()) }
            .pointerInput(series, canvasSize) {
                detectTapGestures { tap ->
                    if (canvasSize.width == 0f || canvasSize.height == 0f) return@detectTapGestures
                    val area = ChartArea(canvasSize)
                    val nearest = series.flatMapIndexed { si, s ->
                        s.entries.indices.map { pi -> (si to pi) to (area.point(s, pi) - tap).getDistanceSquared() }
                    }.minByOrNull { it.second }
                    selected = nearest?.takeIf { it.second < TAP_RADIUS_SQUARED }?.first
                }
            }
    ) {
        val area = ChartArea(size)

        listOf(0, 25, 50, 75, 100).forEach { pct ->
            val y = area.y(pct / 100f)
            val isMid = pct == 50
            drawLine(
                color = if (isMid) gridColorMid else gridColorNormal,
                start = Offset(PADDING_LEFT, y),
                end = Offset(area.width - PADDING_RIGHT, y),
                strokeWidth = if (isMid) 1.5f else 1f,
                pathEffect = if (isMid) PathEffect.dashPathEffect(floatArrayOf(8f, 8f)) else null
            )
            drawContext.canvas.nativeCanvas.drawText("$pct%", PADDING_LEFT - 28f, y + axisPaint.textSize / 3f, axisPaint)
        }

        series.forEach { drawWinRatioSeries(area.points(it), it.color) }

        selected?.let { (si, pi) ->
            val s = series.getOrNull(si) ?: return@let
            val entry = s.entries.getOrNull(pi) ?: return@let
            val anchor = area.point(s, pi)
            drawCircle(color = s.color.copy(alpha = 0.25f), radius = 20f, center = anchor)
            drawCircle(color = s.color, radius = 9f, center = anchor)
            drawCircle(color = Color.White, radius = 4f, center = anchor)
            drawTooltip(s, entry, anchor, area)
        }
    }
}

private fun DrawScope.drawTooltip(series: WinRatioSeries, entry: PlayerSessionEntry, anchor: Offset, area: ChartArea) {
    fun paint(color: Int, size: Float, bold: Boolean) = android.graphics.Paint().apply {
        this.color = color
        textSize = size
        isFakeBoldText = bold
        textAlign = android.graphics.Paint.Align.CENTER
        isAntiAlias = true
    }

    val dateStr = try {
        LocalDate.parse(entry.date, DateTimeFormatter.ISO_LOCAL_DATE).format(tooltipDateFormat)
    } catch (_: Exception) { entry.date }

    val lines = listOfNotNull(
        series.playerName?.let { it to paint(playerColors(it, darkTheme = true).content.toArgb(), 38f, bold = true) },
        dateStr to paint(android.graphics.Color.argb(200, 255, 255, 255), 26f, bold = false),
        "${(entry.winRatio * 100).roundToInt()}%" to paint(android.graphics.Color.WHITE, 40f, bold = true)
    )

    val hPad = 24f
    val lineSpacing = 8f
    val margin = 14f
    val lineHeights = lines.map { (_, p) -> p.fontMetrics.let { -it.ascent + it.descent } }
    val tooltipW = lines.maxOf { (text, p) -> p.measureText(text) } + hPad * 2
    val tooltipH = hPad * 2 + lineHeights.sum() + lineSpacing * (lines.size - 1)
    val tooltipX = (anchor.x - tooltipW / 2)
        .coerceAtMost(area.width - PADDING_RIGHT - tooltipW)
        .coerceAtLeast(PADDING_LEFT)
    val tooltipY = if (anchor.y > area.height / 2) anchor.y - tooltipH - margin else anchor.y + margin
    val bgPaint = android.graphics.Paint().apply {
        color = android.graphics.Color.argb(230, 30, 30, 30)
        isAntiAlias = true
    }

    drawContext.canvas.nativeCanvas.apply {
        drawRoundRect(
            android.graphics.RectF(tooltipX, tooltipY, tooltipX + tooltipW, tooltipY + tooltipH),
            14f, 14f, bgPaint
        )
        val centerX = tooltipX + tooltipW / 2
        var top = tooltipY + hPad
        lines.forEachIndexed { i, (text, p) ->
            drawText(text, centerX, top - p.fontMetrics.ascent, p)
            top += lineHeights[i] + lineSpacing
        }
    }
}
