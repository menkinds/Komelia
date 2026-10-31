package snd.komelia.ui.reader.image.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import snd.komelia.ui.common.components.AppSliderDefaults
import kotlin.math.roundToInt

@Composable
fun AutoTurnControls(
    enabled: Boolean,
    onToggle: () -> Unit,
    intervalSeconds: Int,
    onIntervalClick: () -> Unit,
) {
    Row(
        modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onToggle) {
            Icon(if (enabled) Icons.Default.Pause else Icons.Default.PlayArrow, null)
        }
        Text(
            "${intervalSeconds}s",
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .clickable(onClick = onIntervalClick)
                .padding(horizontal = 8.dp, vertical = 12.dp)
        )
    }
}

@Composable
fun AutoTurnIntervalRow(
    intervalSeconds: Int,
    onIntervalChange: (Int) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("${intervalSeconds}s", modifier = Modifier.padding(end = 12.dp))
        Slider(
            value = intervalSeconds.toFloat(),
            onValueChange = { onIntervalChange(it.roundToInt()) },
            steps = 56,
            valueRange = 3f..60f,
            modifier = Modifier.weight(1f),
            colors = AppSliderDefaults.colors()
        )
    }
}
