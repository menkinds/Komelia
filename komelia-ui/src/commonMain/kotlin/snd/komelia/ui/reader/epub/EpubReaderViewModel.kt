package snd.komelia.ui.reader.epub

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import cafe.adriel.voyager.navigator.Navigator
import kotlinx.coroutines.flow.StateFlow
import snd.komelia.AppNotifications
import snd.komelia.AppWindowState
import snd.komelia.komga.api.KomgaBookApi
import snd.komelia.komga.api.KomgaReadListApi
import snd.komelia.komga.api.KomgaSeriesApi
import snd.komelia.komga.api.model.KomeliaBook
import snd.komelia.settings.CommonSettingsRepository
import snd.komelia.settings.EpubReaderSettingsRepository
import snd.komelia.ui.BookSiblingsContext
import snd.komelia.ui.LoadState
import snd.komelia.ui.platform.PlatformType
import snd.komga.client.book.KomgaBookId
import snd.webview.KomeliaWebview

class EpubReaderViewModel(
    private val bookId: KomgaBookId,
    private val book: KomeliaBook?,
    private val markReadProgress: Boolean,
    private val bookApi: KomgaBookApi,
    private val seriesApi: KomgaSeriesApi,
    private val readListApi: KomgaReadListApi,
    private val settingsRepository: CommonSettingsRepository,
    private val epubSettingsRepository: EpubReaderSettingsRepository,
    private val notifications: AppNotifications,
    private val windowState: AppWindowState,
    private val platformType: PlatformType,
    private val bookSiblingsContext: BookSiblingsContext,
) : StateScreenModel<LoadState<EpubReaderState>>(LoadState.Uninitialized) {

    suspend fun initialize(navigator: Navigator) {
        when (val state = state.value) {
            LoadState.Loading, is LoadState.Error -> {}
            is LoadState.Success<EpubReaderState> -> state.value.initialize(navigator)
            LoadState.Uninitialized -> {

                val komgaState = KomgaEpubReaderState(
                    bookId = bookId,
                    book = book,
                    bookApi = bookApi,
                    seriesApi = seriesApi,
                    readListApi = readListApi,
                    settingsRepository = settingsRepository,
                    notifications = notifications,
                    markReadProgress = markReadProgress,
                    epubSettingsRepository = epubSettingsRepository,
                    windowState = windowState,
                    platformType = platformType,
                    coroutineScope = screenModelScope,
                    bookSiblingsContext = bookSiblingsContext,
                )
                komgaState.initialize(navigator)
                when (val res = komgaState.state.value) {
                    is LoadState.Error -> mutableState.value = LoadState.Error(res.exception)
                    is LoadState.Success<Unit> -> mutableState.value = LoadState.Success(komgaState)
                    LoadState.Loading, LoadState.Uninitialized -> error("Unreachable. failed to initialize komga epub reader")
                }
            }
        }
    }
}

interface EpubReaderState {
    val state: StateFlow<LoadState<Unit>>
    val book: StateFlow<KomeliaBook?>
    suspend fun initialize(navigator: Navigator)
    fun onWebviewCreated(webview: KomeliaWebview)
    fun onBackButtonPress()
    fun closeWebview()
}