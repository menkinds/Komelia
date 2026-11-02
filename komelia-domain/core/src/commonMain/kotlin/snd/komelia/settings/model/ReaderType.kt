package snd.komelia.settings.model

enum class ReaderType {
    PAGED,
    CONTINUOUS;

    companion object {
        /** stored values of removed reader modes (e.g. "PANELS") fall back to [PAGED] */
        fun fromStored(name: String): ReaderType = entries.firstOrNull { it.name == name } ?: PAGED
    }
}
