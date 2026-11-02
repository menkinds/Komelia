package snd.komelia.ui.pages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import snd.komelia.AppNotification
import snd.komelia.AppNotifications
import snd.komelia.komga.api.KomgaBookApi
import snd.komelia.komga.api.KomgaSeriesApi
import snd.komelia.komga.api.model.KomeliaBook
import snd.komelia.ui.platform.cursorForHand
import snd.komga.client.common.patchLists
import snd.komga.client.series.KomgaSeriesMetadataUpdateRequest

/**
 * PAGES: series star rating (1..5) stored as a Komga series tag `★N`.
 * Nothing else stores the rating; it is always read from the series metadata.
 */
object SeriesRating {
    const val STAR = '★'
    const val MAX = 5

    fun tag(rating: Int) = "$STAR$rating"

    private fun parse(tag: String): Int? {
        if (tag.length != 2 || tag[0] != STAR) return null
        return tag[1].digitToIntOrNull()?.takeIf { it in 1..MAX }
    }

    private fun isRatingTag(tag: String) = tag.startsWith(STAR)

    /** Highest `★N` of the tags, null if there is none. */
    fun fromTags(tags: Collection<String>): Int? = tags.mapNotNull { parse(it) }.maxOrNull()

    /** Tags with every `★*` removed and `★rating` added (rating null = cleared). Other tags are kept. */
    fun withRating(tags: Collection<String>, rating: Int?): List<String> {
        val others = tags.filterNot { isRatingTag(it) }
        return if (rating == null) others else (others + tag(rating)).sorted()
    }

    /** Saves the rating on the server. Returns the tags that were written. Throws on failure. */
    suspend fun save(
        seriesApi: KomgaSeriesApi,
        seriesId: snd.komga.client.series.KomgaSeriesId,
        currentTags: List<String>,
        rating: Int?,
    ): List<String> {
        val newTags = withRating(currentTags, rating)
        seriesApi.update(seriesId, KomgaSeriesMetadataUpdateRequest(tags = patchLists(currentTags, newTags)))
        return newTags
    }
}

/** A request to ask the user for a rating. Shown by [SeriesRatingPromptHost]. */
class SeriesRatingPromptRequest(val onRate: suspend (Int) -> Unit)

object SeriesRatingPrompts {
    val requests = MutableSharedFlow<SeriesRatingPromptRequest>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    /**
     * Call after the reader marked [book] progress. Shows the prompt if the book is completed,
     * is the last book of its series, and the series has no rating. Never throws.
     */
    suspend fun onBookProgress(
        bookApi: KomgaBookApi,
        seriesApi: KomgaSeriesApi,
        notifications: AppNotifications,
        book: KomeliaBook,
        page: Int,
    ) {
        if (book.oneshot) return
        // Komga marks a book completed on its last page; skip the lookups for other pages
        if (page < book.media.pagesCount - 1) return
        runCatching {
            val updated = bookApi.getOne(book.id)
            if (updated.readProgress?.completed != true) return
            if (bookApi.getBookSiblingNext(book.id) != null) return
            val series = seriesApi.getOneSeries(book.seriesId)
            if (SeriesRating.fromTags(series.metadata.tags) != null) return
            requests.tryEmit(SeriesRatingPromptRequest { rating ->
                runCatching {
                    // read the tags again, they may have changed since
                    val tags = seriesApi.getOneSeries(book.seriesId).metadata.tags
                    SeriesRating.save(seriesApi, book.seriesId, tags, rating)
                }.onFailure {
                    if (it is kotlin.coroutines.cancellation.CancellationException) throw it
                    notifications.add(AppNotification.Error(it.message ?: "별점을 저장하지 못했어요"))
                }
            })
        }.onFailure { if (it is kotlin.coroutines.cancellation.CancellationException) throw it }
    }
}

@Composable
fun SeriesRatingStars(
    rating: Int?,
    onRate: (Int) -> Unit,
    modifier: Modifier = Modifier,
    fontSize: Int = 28,
) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
        for (star in 1..SeriesRating.MAX) {
            val filled = rating != null && star <= rating
            Text(
                text = if (filled) "★" else "☆",
                fontSize = fontSize.sp,
                color = if (filled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .cursorForHand()
                    .clickable { onRate(star) }
                    .padding(horizontal = 2.dp, vertical = 2.dp)
            )
        }
    }
}

/** Shows the "rate this series" dialog when the reader asks for it. */
@Composable
fun SeriesRatingPromptHost() {
    var request by remember { mutableStateOf<SeriesRatingPromptRequest?>(null) }
    // declared before the early return so the scope outlives the dialog
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) { SeriesRatingPrompts.requests.collect { request = it } }

    val current = request ?: return
    AlertDialog(
        onDismissRequest = { request = null },
        title = { Text("이 시리즈 어땠어요?") },
        text = {
            SeriesRatingStars(
                rating = null,
                onRate = { star ->
                    request = null
                    scope.launch { current.onRate(star) }
                },
                fontSize = 36
            )
        },
        confirmButton = {
            TextButton(onClick = { request = null }, modifier = Modifier.cursorForHand()) {
                Text("나중에")
            }
        }
    )
}
