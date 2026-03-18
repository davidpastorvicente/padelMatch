package com.padelgroup.padelMatch.ui.statistics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.padelgroup.padelMatch.data.model.PlayerStats
import com.padelgroup.padelMatch.ui.theme.playerColors
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

private data class PlayerLine(
    val stats: PlayerStats,
    val lineColor: Color,
    val xFractions: List<Float>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CombinedWinRatioChartScreen(
    viewModel: StatisticsViewModel,
    onBack: () -> Unit
) {
    val playerStats by viewModel.playerStats.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gráfico general", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        if (playerStats.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Sin datos todavía",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            RotatedLayout(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    CombinedWinRatioChart(
                        playerStats = playerStats,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                    Spacer(Modifier.height(12.dp))
                    PlayerLegend(playerStats = playerStats)
                }
            }
        }
    }
}

@Composable
private fun CombinedWinRatioChart(
    playerStats: List<PlayerStats>,
    modifier: Modifier = Modifier
) {
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val isoFmt = DateTimeFormatter.ISO_LOCAL_DATE
    val dateFmt = remember { DateTimeFormatter.ofPattern("d MMM yyyy", Locale.forLanguageTag("es")) }

    val globalEpochRange: Pair<Long, Long>? = remember(playerStats) {
        val allEpochs = playerStats.flatMap { it.history }.mapNotNull {
            try { LocalDate.parse(it.date, isoFmt).toEpochDay() } catch (_: Exception) { null }
        }
        if (allEpochs.isEmpty()) null else allEpochs.min() to allEpochs.max()
    }

    if (globalEpochRange == null) return

    val playerLines: List<PlayerLine> = remember(playerStats, globalEpochRange) {
        val (minEpoch, maxEpoch) = globalEpochRange
        val totalDays = (maxEpoch - minEpoch).coerceAtLeast(1).toFloat()
        playerStats.filter { it.history.isNotEmpty() }.map { stats ->
            val (bg, onColor) = playerColors(stats.player.name)
            val lineColor = lerp(bg, onColor, 0.45f)
            val fractions = stats.history.map { entry ->
                try {
                    val epoch = LocalDate.parse(entry.date, isoFmt).toEpochDay()
                    (epoch - minEpoch) / totalDays
                } catch (_: Exception) { 0.5f }
            }
            PlayerLine(stats, lineColor, fractions)
        }
    }

    val gridColorNormal = remember(onSurfaceVariant) { onSurfaceVariant.copy(alpha = 0.15f) }
    val gridColorMid = remember(onSurfaceVariant) { onSurfaceVariant.copy(alpha = 0.3f) }
    val axisColor = remember(onSurfaceVariant) { onSurfaceVariant.copy(alpha = 0.7f).toArgb() }
    val axisPaint = remember(axisColor) {
        android.graphics.Paint().apply {
            textSize = 30f
            color = axisColor
            textAlign = android.graphics.Paint.Align.RIGHT
        }
    }

    val paddingLeft = 88f
    val paddingRight = 16f
    val paddingTop = 8f

    var canvasSize by remember { mutableStateOf(Size.Zero) }
    // selected: lineIdx to pointIdx
    var selected by remember { mutableStateOf<Pair<Int, Int>?>(null) }

    Canvas(
        modifier = modifier
            .onSizeChanged { canvasSize = Size(it.width.toFloat(), it.height.toFloat()) }
            .pointerInput(playerLines, canvasSize) {
                detectTapGestures { offset ->
                    val w = canvasSize.width
                    val h = canvasSize.height
                    if (w == 0f || h == 0f) return@detectTapGestures
                    val chartW = w - paddingLeft - paddingRight
                    val chartH = h - paddingTop
                    var bestDist = Float.MAX_VALUE
                    var bestPair: Pair<Int, Int>? = null
                    playerLines.forEachIndexed { li, line ->
                        line.stats.history.forEachIndexed { pi, entry ->
                            val px = paddingLeft + line.xFractions[pi] * chartW
                            val py = paddingTop + chartH - entry.winRatio * chartH
                            val dist = (px - offset.x).let { it * it } + (py - offset.y).let { it * it }
                            if (dist < bestDist) { bestDist = dist; bestPair = li to pi }
                        }
                    }
                    selected = if (bestDist < 3600f) bestPair else null
                }
            }
    ) {
        val w = size.width
        val h = size.height
        val chartW = w - paddingLeft - paddingRight
        val chartH = h - paddingTop

        listOf(0, 25, 50, 75, 100).forEach { pct ->
            val ratio = pct / 100f
            val y = paddingTop + chartH - ratio * chartH
            drawLine(
                color = if (pct == 50) gridColorMid else gridColorNormal,
                start = Offset(paddingLeft, y),
                end = Offset(w - paddingRight, y),
                strokeWidth = if (pct == 50) 1.5f else 1f,
                pathEffect = if (pct == 50) PathEffect.dashPathEffect(floatArrayOf(8f, 8f)) else null
            )
            drawContext.canvas.nativeCanvas.drawText(
                "$pct%",
                paddingLeft - 28f,
                y + axisPaint.textSize / 3f,
                axisPaint
            )
        }

        playerLines.forEach { line ->
            val n = line.stats.history.size
            if (n == 0) return@forEach
            fun xPos(i: Int): Float = paddingLeft + line.xFractions[i] * chartW
            fun yPos(i: Int): Float = paddingTop + chartH - line.stats.history[i].winRatio * chartH
            for (i in 0 until n - 1) {
                drawLine(
                    color = line.lineColor,
                    start = Offset(xPos(i), yPos(i)),
                    end = Offset(xPos(i + 1), yPos(i + 1)),
                    strokeWidth = 7f
                )
            }
            for (i in 0 until n) {
                drawCircle(color = line.lineColor, radius = 7f, center = Offset(xPos(i), yPos(i)))
                drawCircle(color = Color.White, radius = 3f, center = Offset(xPos(i), yPos(i)))
            }
        }

        selected?.let { (li, pi) ->
            val line = playerLines.getOrNull(li) ?: return@let
            val entry = line.stats.history.getOrNull(pi) ?: return@let
            val tx = paddingLeft + line.xFractions[pi] * chartW
            val ty = paddingTop + chartH - entry.winRatio * chartH
            drawCircle(color = line.lineColor.copy(alpha = 0.25f), radius = 20f, center = Offset(tx, ty))
            drawCircle(color = line.lineColor, radius = 9f, center = Offset(tx, ty))
            drawCircle(color = Color.White, radius = 4f, center = Offset(tx, ty))

            val dateStr = try {
                LocalDate.parse(entry.date, isoFmt).format(dateFmt)
            } catch (_: Exception) { entry.date }

            drawContext.canvas.nativeCanvas.apply {
                val hPad = 24f
                val lineSpacing = 8f
                val margin = 14f
                val namePaint = android.graphics.Paint().apply {
                    color = line.lineColor.toArgb()
                    textSize = 38f
                    isFakeBoldText = true
                    textAlign = android.graphics.Paint.Align.CENTER
                    isAntiAlias = true
                }
                val datePaint = android.graphics.Paint().apply {
                    color = android.graphics.Color.argb(200, 255, 255, 255)
                    textSize = 26f
                    textAlign = android.graphics.Paint.Align.CENTER
                    isAntiAlias = true
                }
                val ratioPaint = android.graphics.Paint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = 40f
                    isFakeBoldText = true
                    textAlign = android.graphics.Paint.Align.CENTER
                    isAntiAlias = true
                }
                val fmN = namePaint.fontMetrics
                val fmD = datePaint.fontMetrics
                val fmR = ratioPaint.fontMetrics
                val ratioStr = "${(entry.winRatio * 100).roundToInt()}%"
                val tooltipW = maxOf(
                    namePaint.measureText(line.stats.player.name),
                    datePaint.measureText(dateStr),
                    ratioPaint.measureText(ratioStr)
                ) + hPad * 2
                val tooltipH = hPad + (-fmN.ascent) + fmN.descent + lineSpacing + (-fmD.ascent) + fmD.descent + lineSpacing + (-fmR.ascent) + fmR.descent + hPad
                val tooltipX = (tx - tooltipW / 2).coerceIn(paddingLeft, w - paddingRight - tooltipW)
                val tooltipY = if (ty > h / 2) ty - tooltipH - margin else ty + margin
                val bgPaint = android.graphics.Paint().apply {
                    color = android.graphics.Color.argb(230, 30, 30, 30)
                    isAntiAlias = true
                }
                drawRoundRect(
                    android.graphics.RectF(tooltipX, tooltipY, tooltipX + tooltipW, tooltipY + tooltipH),
                    14f, 14f, bgPaint
                )
                val centerX = tooltipX + tooltipW / 2
                val line1Y = tooltipY + hPad + (-fmN.ascent)
                val line2Y = line1Y + fmN.descent + lineSpacing + (-fmD.ascent)
                val line3Y = line2Y + fmD.descent + lineSpacing + (-fmR.ascent)
                drawText(line.stats.player.name, centerX, line1Y, namePaint)
                drawText(dateStr, centerX, line2Y, datePaint)
                drawText(ratioStr, centerX, line3Y, ratioPaint)
            }
        }
    }
}

@Composable
private fun RotatedLayout(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Layout(
        content = content,
        modifier = modifier.graphicsLayer { rotationZ = 90f }
    ) { measurables, constraints ->
        val placeable = measurables.first().measure(
            Constraints.fixed(constraints.maxHeight, constraints.maxWidth)
        )
        layout(constraints.maxWidth, constraints.maxHeight) {
            placeable.place(
                x = (constraints.maxWidth - placeable.width) / 2,
                y = (constraints.maxHeight - placeable.height) / 2
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PlayerLegend(playerStats: List<PlayerStats>) {
    val sortedPlayers = remember(playerStats) { playerStats.sortedBy { it.player.name } }
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        sortedPlayers.forEach { stats ->
            val (bg, onColor) = remember(stats.player.name) { playerColors(stats.player.name) }
            val dotColor = remember(bg, onColor) { lerp(bg, onColor, 0.45f) }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = dotColor,
                    modifier = Modifier.size(12.dp)
                ) {}
                Spacer(Modifier.width(6.dp))
                Text(
                    text = stats.player.name,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
