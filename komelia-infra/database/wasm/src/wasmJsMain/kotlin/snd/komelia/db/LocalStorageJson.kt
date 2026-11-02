package snd.komelia.db

import kotlinx.serialization.json.Json

object LocalStorageJson {
    val json = Json {
        ignoreUnknownKeys = true
        // values of removed enum entries (e.g. reader type "PANELS") fall back to property defaults
        coerceInputValues = true
    }
}