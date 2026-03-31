package com.davidpv.padelmatch.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun DayCell(
    day: Int,
    hasSession: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = modifier
            .padding(2.dp)
            .clip(CircleShape)
            .then(
                if (isSelected) Modifier.background(primaryColor) else Modifier
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = day.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (hasSession) FontWeight.Bold else FontWeight.Normal,
                color = when {
                    isSelected -> onPrimaryColor
                    hasSession -> primaryColor
                    else -> onSurfaceVariantColor
                }
            )
            if (hasSession && !isSelected) {
                Spacer(Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(primaryColor)
                )
            }
        }
    }
}
