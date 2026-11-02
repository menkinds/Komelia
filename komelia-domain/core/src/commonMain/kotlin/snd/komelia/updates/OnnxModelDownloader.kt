package snd.komelia.updates

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow

interface OnnxModelDownloader {
    val downloadCompletionEvents: SharedFlow<CompletionEvent>
    fun mangaJaNaiDownload(): Flow<UpdateProgress>

    sealed interface CompletionEvent {
        data object MangaJaNaiDownloaded : CompletionEvent
    }
}