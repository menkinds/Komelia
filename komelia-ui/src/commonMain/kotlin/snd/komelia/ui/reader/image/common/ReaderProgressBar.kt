package snd.komelia.ui.reader.image.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Thin full-width line at the bottom edge: the white part grows with the book progress and reaches the right
 * edge on the last page. A faint dark track keeps it visible on white pages. Touches pass through.
 */
@Composable
fun BoxScope.ReaderProgressBar(page: Int, pageCount: Int) {
    if (pageCount <= 0) return
    val fraction = (page.toFloat() / pageCount).coerceIn(0f, 1f)
    Box(
        Modifier
            .align(Alignment.BottomStart)
            .fillMaxWidth()
            .height(3.dp)
            .background(Color.Black.copy(alpha = 0.35f))
    ) {
        Box(Modifier.fillMaxHeight().fillMaxWidth(fraction).background(Color.White))
    }
}
