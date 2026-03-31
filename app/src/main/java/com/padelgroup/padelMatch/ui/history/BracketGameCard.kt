package com.davidpv.padelmatch.ui.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.davidpv.padelmatch.data.repository.GameWithPlayerNames

@Composable
fun BracketGameCard(
    game: GameWithPlayerNames,
    onTeamClick: ((pair: Int) -> Unit)? = null,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null
) {
    val pair1Won = game.winningPair == 1
    val pair2Won = game.winningPair == 2

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onEdit != null) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(40.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Editar",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Team A panel (left)
            TeamPanel(
                player1 = game.pair1Player1,
                player2 = game.pair1Player2,
                isWinner = pair1Won,
                alignEnd = false,
                modifier = Modifier.weight(1f).fillMaxHeight()
                    .then(if (onTeamClick != null) Modifier.clickable { onTeamClick(1) } else Modifier)
            )

            // Centre — game number only
            Text(
                text = "${game.gameNumber}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(32.dp)
            )

            // Team B panel (middle)
            TeamPanel(
                player1 = game.pair2Player1,
                player2 = game.pair2Player2,
                isWinner = pair2Won,
                alignEnd = true,
                modifier = Modifier.weight(1f).fillMaxHeight()
                    .then(if (onTeamClick != null) Modifier.clickable { onTeamClick(2) } else Modifier)
            )

            if (onDelete != null) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(40.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun TeamPanel(
    player1: String,
    player2: String,
    isWinner: Boolean,
    alignEnd: Boolean,
    modifier: Modifier = Modifier
) {
    val bgColor = if (isWinner) MaterialTheme.colorScheme.primaryContainer
                  else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (isWinner) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurfaceVariant
    val weight = if (isWinner) FontWeight.Bold else FontWeight.Normal

    Surface(
        color = bgColor,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = if (alignEnd) 10.dp else 14.dp,
                    top = 12.dp,
                    end = if (alignEnd) 14.dp else 10.dp,
                    bottom = 12.dp
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (alignEnd) {
                // Right panel: trophy on far left, names on far right
                if (isWinner) {
                    Text(
                        text = "🏆",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    Spacer(Modifier)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = player1, style = MaterialTheme.typography.bodySmall, fontWeight = weight, color = textColor, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.End)
                    Text(text = player2, style = MaterialTheme.typography.bodySmall, fontWeight = weight, color = textColor, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.End)
                }
            } else {
                // Left panel: names on far left, trophy on far right
                Column(horizontalAlignment = Alignment.Start) {
                    Text(text = player1, style = MaterialTheme.typography.bodySmall, fontWeight = weight, color = textColor, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Start)
                    Text(text = player2, style = MaterialTheme.typography.bodySmall, fontWeight = weight, color = textColor, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Start)
                }
                if (isWinner) {
                    Text(
                        text = "🏆",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
