package snd.komelia.db

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject

@Serializable
data class EpubReaderSettings(
    val komgaReaderSettings: JsonObject = buildJsonObject { },
)
