package snd.komelia.ui.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.Res
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.auto_turn_measurement
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.auto_turn_measuring
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

/**
 * Non-interactive bottom-left label (same size as the reader clock) with the measured amounts of the shown
 * page(s), the resulting delay and the seconds left until the page turns.
 */
@Composable
fun BoxScope.AutoTurnMeasurementOverlay(measurement: AutoTurnMeasurement?) {
    fun left() = measurement?.let { (-it.turnAt.elapsedNow().inWholeMilliseconds).coerceAtLeast(0) } ?: 0L
    var remainingMs by remember(measurement) { mutableLongStateOf(left()) }
    LaunchedEffect(measurement) {
        if (measurement == null) return@LaunchedEffect
        while (true) {
            remainingMs = left()
            if (remainingMs == 0L) break
            delay(remainingMs % 1000 + 1)
        }
    }
    val text = if (measurement == null) stringResource(Res.string.auto_turn_measuring)
    else stringResource(
        Res.string.auto_turn_measurement,
        measurement.linePercent?.let { "$it%" } ?: "-",
        measurement.darkPercent?.let { "$it%" } ?: "-",
        measurement.charCount?.toString() ?: "-",
        ((measurement.delayMs / 100.0).roundToInt() / 10.0).toString(),
        ((remainingMs + 999) / 1000).toString(),
    )
    Text(
        text = text,
        color = Color.White,
        fontSize = 12.sp,
        modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(8.dp)
            .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}
