package snd.komelia.settings

import kotlinx.serialization.json.JsonObject

interface EpubReaderSettingsRepository {
    suspend fun getKomgaReaderSettings(): JsonObject
    suspend fun putKomgaReaderSettings(settings: JsonObject)
}
