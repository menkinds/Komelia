package snd.komelia.ui.reader.image.common

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
import snd.komelia.ui.platform.rememberClockText

/** Small non-interactive clock in the bottom-right corner; touches pass through to the page. */
@Composable
fun BoxScope.ReaderClockOverlay() {
    val text = rememberClockText()
    if (text.isEmpty()) return
    Text(
        text = text,
        color = Color.White,
        fontSize = 12.sp,
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(8.dp)
            .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}
