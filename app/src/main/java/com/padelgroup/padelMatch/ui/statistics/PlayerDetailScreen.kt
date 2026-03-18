package com.padelgroup.padelMatch.ui.statistics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.padelgroup.padelMatch.data.model.PlayerSessionEntry
import com.padelgroup.padelMatch.ui.theme.playerColors
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerDetailScreen(
    viewModel: PlayerDetailViewModel,
    onBack: () -> Unit,
    onSessionClick: (Long) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val name = (uiState as? PlayerDetailUiState.Success)?.data?.player?.name ?: ""
                    Text(name, fontWeight = FontWeight.Bold)
                },
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
    ) { innerPadding ->
        when (val state = uiState) {
            is PlayerDetailUiState.Loading -> {
                Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is PlayerDetailUiState.Error -> {
                Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                }
            }
            is PlayerDetailUiState.Success -> {
                PlayerDetailContent(
                    data = state.data,
                    onSessionClick = onSessionClick,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}

@Composable
private fun PlayerDetailContent(data: PlayerDetailData, onSessionClick: (Long) -> Unit, modifier: Modifier = Modifier) {
    val (badgeBg, _) = playerColors(data.player.name)
    val winPct = "%.1f%%".format(data.winRatio * 100)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Stats table
        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                StatsRow(
                    labels = listOf("Partidos", "Sets", "Victorias", "Ratio"),
                    values = listOf(
                        data.sessionsAttended.toString(),
                        data.totalGames.toString(),
                        data.wins.toString(),
                        winPct
                    )
                )
            }
        }

        // Chart + session list
        if (data.sessionHistory.isNotEmpty()) {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Historial de ratio",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(12.dp))
                    PlayerWinRatioChart(
                        history = data.sessionHistory,
                        lineColor = badgeBg,
                        modifier = Modifier.fillMaxWidth().height(160.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(thickness = 0.5.dp)
                    val reversedHistory = remember(data.sessionHistory) { data.sessionHistory.reversed() }
                    reversedHistory.forEach { entry ->
                        val onRowClick = remember(entry.sessionId) { { onSessionClick(entry.sessionId) } }
                        SessionHistoryRow(entry = entry, onClick = onRowClick)
                        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    }
                }
            }
        }
    }
}

@Composable
private fun StatsRow(labels: List<String>, values: List<String>) {
    Column {
        Row(modifier = Modifier.fillMaxWidth()) {
            labels.forEach { label ->
                Text(
                    text = label,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            values.forEach { value ->
                Text(
                    text = value,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun SessionHistoryRow(entry: PlayerSessionEntry, onClick: () -> Unit) {
    val dateStr = remember(entry.date) {
        val formatter = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy", Locale("es"))
        try {
            LocalDate.parse(entry.date, DateTimeFormatter.ISO_LOCAL_DATE).format(formatter)
                .replaceFirstChar { it.uppercase() }
        } catch (_: Exception) { entry.date }
    }
    val ratioPct = remember(entry.winRatio) { (entry.winRatio * 100).roundToInt() }
    val (badgeColor, textColor) = remember(entry.winRatio) { winRatioBadgeColor(entry.winRatio) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = dateStr,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(8.dp))
        Surface(shape = MaterialTheme.shapes.small, color = badgeColor) {
            Text(
                text = "$ratioPct%",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = textColor,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
    }
}

private fun winRatioBadgeColor(ratio: Float): Pair<Color, Color> {
    val red = Color(0xFFEF5350)        // Material Red 400 — 0%
    val orange = Color(0xFFFFA726)     // Material Orange 400 — 40%
    val lightGreen = Color(0xFFA5D6A7) // Material Green 200 — 50%
    val darkGreen = Color(0xFF2E7D32)  // Material Green 800 — 100%
    val bg = when {
        ratio < 0.3f -> lerp(red, orange, ratio / 0.3f)
        ratio < 0.5f -> lerp(orange, lightGreen, (ratio - 0.3f) / 0.2f)
        else -> lerp(lightGreen, darkGreen, (ratio - 0.5f) * 2f)
    }
    return Pair(bg, Color.White)
}

@Composable
private fun PlayerWinRatioChart(
    history: List<PlayerSessionEntry>,
    lineColor: Color,
    modifier: Modifier = Modifier
) {
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val darkerLineColor = remember(lineColor) {
        Color(
            red = lineColor.red * 0.65f,
            green = lineColor.green * 0.65f,
            blue = lineColor.blue * 0.65f,
            alpha = 1f
        )
    }

    val xFractions = remember(history) {
        if (history.size == 1) return@remember listOf(0.5f)
        val isoFmt = DateTimeFormatter.ISO_LOCAL_DATE
        val dates = history.map {
            try { LocalDate.parse(it.date, isoFmt) } catch (_: Exception) { LocalDate.now() }
        }
        val minEpoch = dates.first().toEpochDay()
        val maxEpoch = dates.last().toEpochDay()
        val totalDays = (maxEpoch - minEpoch).coerceAtLeast(1).toFloat()
        dates.map { (it.toEpochDay() - minEpoch) / totalDays }
    }

    val formattedDates = remember(history) {
        val fmt = DateTimeFormatter.ofPattern("d MMM yyyy", Locale("es"))
        history.map { entry ->
            try { LocalDate.parse(entry.date, DateTimeFormatter.ISO_LOCAL_DATE).format(fmt) }
            catch (_: Exception) { entry.date }
        }
    }

    val axisColor = remember(onSurfaceVariant) { onSurfaceVariant.copy(alpha = 0.7f).toArgb() }
    val axisPaint = remember(axisColor) {
        android.graphics.Paint().apply {
            textSize = 30f
            color = axisColor
            textAlign = android.graphics.Paint.Align.RIGHT
        }
    }
    val gridColorNormal = remember(onSurfaceVariant) { onSurfaceVariant.copy(alpha = 0.15f) }
    val gridColorMid = remember(onSurfaceVariant) { onSurfaceVariant.copy(alpha = 0.3f) }

    val paddingLeft = 88f
    val paddingRight = 16f
    val paddingTop = 8f

    var canvasSize by remember { mutableStateOf(Size.Zero) }
    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    Canvas(
        modifier = modifier
            .onSizeChanged { canvasSize = Size(it.width.toFloat(), it.height.toFloat()) }
            .pointerInput(history, canvasSize) {
                detectTapGestures { offset ->
                    val w = canvasSize.width
                    val h = canvasSize.height
                    if (w == 0f || h == 0f) return@detectTapGestures
                    val chartW = w - paddingLeft - paddingRight
                    val chartH = h - paddingTop
                    fun xPos(i: Int) = paddingLeft + xFractions[i] * chartW
                    fun yPos(i: Int) = paddingTop + chartH - history[i].winRatio * chartH
                    val nearest = (0 until history.size).minByOrNull { i ->
                        val dx = xPos(i) - offset.x
                        val dy = yPos(i) - offset.y
                        dx * dx + dy * dy
                    }
                    selectedIndex = if (nearest != null) {
                        val dx = xPos(nearest) - offset.x
                        val dy = yPos(nearest) - offset.y
                        if (dx * dx + dy * dy < 3600f) nearest else null
                    } else null
                }
            }
    ) {
        val w = size.width
        val h = size.height
        val chartW = w - paddingLeft - paddingRight
        val chartH = h - paddingTop
        val n = history.size

        fun xPos(i: Int) = paddingLeft + xFractions[i] * chartW
        fun yPos(i: Int) = paddingTop + chartH - history[i].winRatio * chartH

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

        for (i in 0 until n - 1) {
            drawLine(
                color = darkerLineColor,
                start = Offset(xPos(i), yPos(i)),
                end = Offset(xPos(i + 1), yPos(i + 1)),
                strokeWidth = 7f
            )
        }

        for (i in 0 until n) {
            drawCircle(color = darkerLineColor, radius = 7f, center = Offset(xPos(i), yPos(i)))
            drawCircle(color = Color.White, radius = 3f, center = Offset(xPos(i), yPos(i)))
        }

        selectedIndex?.let { idx ->
            val tx = xPos(idx)
            val ty = yPos(idx)
            drawCircle(color = darkerLineColor.copy(alpha = 0.25f), radius = 20f, center = Offset(tx, ty))
            drawCircle(color = darkerLineColor, radius = 9f, center = Offset(tx, ty))
            drawCircle(color = Color.White, radius = 4f, center = Offset(tx, ty))

            drawContext.canvas.nativeCanvas.apply {
                val hPad = 24f
                val lineSpacing = 8f
                val margin = 14f
                val datePaint = android.graphics.Paint().apply {
                    color = android.graphics.Color.argb(200, 255, 255, 255)
                    textSize = 28f
                    textAlign = android.graphics.Paint.Align.CENTER
                    isAntiAlias = true
                }
                val ratioPaint = android.graphics.Paint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = 32f
                    isFakeBoldText = true
                    textAlign = android.graphics.Paint.Align.CENTER
                    isAntiAlias = true
                }
                val fm1 = datePaint.fontMetrics
                val fm2 = ratioPaint.fontMetrics
                val ratioStr = "${(history[idx].winRatio * 100).roundToInt()}%"
                val tooltipW = maxOf(
                    datePaint.measureText(formattedDates[idx]),
                    ratioPaint.measureText(ratioStr)
                ) + hPad * 2
                val tooltipH = hPad + (-fm1.ascent) + fm1.descent + lineSpacing + (-fm2.ascent) + fm2.descent + hPad
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
                val line1Y = tooltipY + hPad + (-fm1.ascent)
                val line2Y = line1Y + fm1.descent + lineSpacing + (-fm2.ascent)
                drawText(formattedDates[idx], centerX, line1Y, datePaint)
                drawText(ratioStr, centerX, line2Y, ratioPaint)
            }
        }
    }
}
