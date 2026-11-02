package snd.komelia.db.settings

import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.upsert
import snd.komelia.db.EpubReaderSettings
import snd.komelia.db.ExposedRepository
import snd.komelia.db.defaultBookId
import snd.komelia.db.tables.EpubReaderSettingsTable

class ExposedEpubReaderSettingsRepository(database: Database) : ExposedRepository(database) {

    suspend fun get(): EpubReaderSettings? {
        return transaction {
            EpubReaderSettingsTable.selectAll()
                .where { EpubReaderSettingsTable.bookId.eq(defaultBookId) }
                .firstOrNull()
                ?.let {
                    EpubReaderSettings(
                        komgaReaderSettings = it[EpubReaderSettingsTable.komgaSettingsJson],
                    )
                }
        }
    }

    suspend fun save(settings: EpubReaderSettings) {
        transaction {
            EpubReaderSettingsTable.upsert {
                it[bookId] = defaultBookId
                it[readerType] = LEGACY_READER_TYPE
                it[komgaSettingsJson] = settings.komgaReaderSettings
                it[ttsuSettingsJson] = LEGACY_TTSU_SETTINGS
            }
        }
    }

    private companion object {
        const val LEGACY_READER_TYPE = "KOMGA_EPUB"
        const val LEGACY_TTSU_SETTINGS = "{}"
    }
}
