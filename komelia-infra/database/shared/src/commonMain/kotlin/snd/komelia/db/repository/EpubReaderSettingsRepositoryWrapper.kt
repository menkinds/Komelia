package snd.komelia.db.repository

import kotlinx.serialization.json.JsonObject
import snd.komelia.db.EpubReaderSettings
import snd.komelia.db.SettingsStateWrapper
import snd.komelia.settings.EpubReaderSettingsRepository

class EpubReaderSettingsRepositoryWrapper(
    private val wrapper: SettingsStateWrapper<EpubReaderSettings>
) : EpubReaderSettingsRepository {

    override suspend fun getKomgaReaderSettings(): JsonObject {
        return wrapper.state.value.komgaReaderSettings
    }

    override suspend fun putKomgaReaderSettings(settings: JsonObject) {
        return wrapper.transform { it.copy(komgaReaderSettings = settings) }
    }
}
