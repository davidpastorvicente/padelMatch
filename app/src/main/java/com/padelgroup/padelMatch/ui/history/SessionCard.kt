package com.padelgroup.padelMatch.ui.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.padelgroup.padelMatch.data.repository.SessionWithDetails
import com.padelgroup.padelMatch.ui.theme.playerColors
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SessionCard(session: SessionWithDetails, onClick: () -> Unit) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = formatDate(session.date),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                session.players.forEach { player ->
                    val (bg, fg) = playerColors(player.playerName)
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = bg,
                        contentColor = fg
                    ) {
                        Text(
                            text = player.playerName,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun formatDate(isoDate: String): String = try {
    val date = LocalDate.parse(isoDate)
    date.format(DateTimeFormatter.ofPattern("d 'de' MMMM yyyy", Locale("es")))
} catch (e: Exception) { isoDate }
