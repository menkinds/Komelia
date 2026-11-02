package snd.komelia.updates

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emptyFlow
import snd.komelia.updates.OnnxModelDownloader.CompletionEvent

class AndroidOnnxModelDownloader : OnnxModelDownloader {
    override val downloadCompletionEvents = MutableSharedFlow<CompletionEvent>()

    override fun mangaJaNaiDownload(): Flow<UpdateProgress> {
        return emptyFlow()
    }
}
