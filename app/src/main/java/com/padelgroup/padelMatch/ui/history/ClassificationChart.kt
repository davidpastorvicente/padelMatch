package com.padelgroup.padelMatch.ui.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.padelgroup.padelMatch.data.db.dao.SessionPlayerWithName
import com.padelgroup.padelMatch.ui.theme.playerColors

@Composable
fun ClassificationChart(players: List<SessionPlayerWithName>) {
    val sorted = remember(players) {
        players.sortedWith(
            compareByDescending<SessionPlayerWithName> { it.winRatio }
                .thenBy { it.playerName }
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        sorted.forEach { player ->
            val (bg, fg) = remember(player.playerName) { playerColors(player.playerName) }
            val pct = remember(player.winRatio) { (player.winRatio * 100).toInt() }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = player.playerName,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = fg,
                    modifier = Modifier.width(72.dp)
                )
                LinearProgressIndicator(
                    progress = { player.winRatio },
                    modifier = Modifier.weight(1f).height(14.dp),
                    color = bg,
                    trackColor = bg.copy(alpha = 0.2f)
                )
                Text(
                    text = "$pct%",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = fg,
                    modifier = Modifier.width(32.dp)
                )
            }
        }
    }
}
