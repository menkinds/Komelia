package snd.komelia.ui.settings.appearance

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import snd.komelia.adult.AdultLockState
import snd.komelia.komga.api.KomgaLibraryApi
import snd.komelia.settings.CommonSettingsRepository
import snd.komelia.settings.model.AppTheme
import snd.komelia.ui.LoadState
import snd.komelia.ui.common.cards.defaultCardWidth
import snd.komga.client.library.KomgaLibrary

class AppSettingsViewModel(
    private val settingsRepository: CommonSettingsRepository,
    private val libraryApi: KomgaLibraryApi,
    private val adultLockState: AdultLockState,
) : StateScreenModel<LoadState<Unit>>(LoadState.Uninitialized) {
    var cardWidth by mutableStateOf(defaultCardWidth.dp)
    var currentTheme by mutableStateOf(AppTheme.DARK)

    var libraries by mutableStateOf<List<KomgaLibrary>>(emptyList())
        private set
    val restrictedLibraryId: StateFlow<String?> = adultLockState.libraryId
    val unlocked: StateFlow<Boolean> = adultLockState.unlocked

    suspend fun initialize() {
        if (state.value !is LoadState.Uninitialized) return
        mutableState.value = LoadState.Loading
        cardWidth = settingsRepository.getCardWidth().map { it.dp }.first()
        currentTheme = settingsRepository.getAppTheme().first()
        libraries = runCatching { libraryApi.getLibraries() }.getOrDefault(emptyList())
        mutableState.value = LoadState.Success(Unit)
    }

    fun onCardWidthChange(cardWidth: Dp) {
        this.cardWidth = cardWidth
        screenModelScope.launch { settingsRepository.putCardWidth(cardWidth.value.toInt()) }
    }

    fun onRestrictedLibraryChange(libraryId: String?) {
        // changeable only when nothing is set yet or while unlocked
        if (adultLockState.libraryId.value != null && !adultLockState.unlocked.value) return
        screenModelScope.launch {
            settingsRepository.putRestrictedLibraryId(libraryId)
            // a new selection is locked at once
            if (libraryId != null) adultLockState.lock()
        }
    }

    fun onAppThemeChange(theme: AppTheme) {
        this.currentTheme = theme
        screenModelScope.launch { settingsRepository.putAppTheme(theme) }
    }

}