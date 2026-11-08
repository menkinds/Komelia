package snd.komelia.ui.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

/** Non-interactive top-left label with the measured amounts of the shown page(s) and the resulting delay. */
@Composable
fun BoxScope.AutoTurnMeasurementOverlay(measurement: AutoTurnMeasurement?) {
    val text = if (measurement == null) stringResource(Res.string.auto_turn_measuring)
    else stringResource(
        Res.string.auto_turn_measurement,
        measurement.linePercent?.let { "$it%" } ?: "-",
        measurement.darkPercent?.let { "$it%" } ?: "-",
        measurement.charCount?.toString() ?: "-",
        ((measurement.delayMs / 100.0).roundToInt() / 10.0).toString(),
    )
    Text(
        text = text,
        color = Color.White,
        fontSize = 12.sp,
        modifier = Modifier
            .align(Alignment.TopStart)
            .padding(8.dp)
            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}
