package snd.komelia.db.tables

import kotlinx.serialization.json.JsonObject
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.json.json
import snd.komelia.db.JsonDbDefault

object EpubReaderSettingsTable : Table("EpubReaderSettings") {
    val bookId = text("book_id")

    // reader_type and ttsu_settings_json are legacy NOT NULL columns of the removed ttsu reader.
    // They are never read; save() writes constants so that no migration is needed.
    val readerType = text("reader_type")
    val komgaSettingsJson = json<JsonObject>("komga_settings_json", JsonDbDefault)
    val ttsuSettingsJson = text("ttsu_settings_json")

    override val primaryKey = PrimaryKey(bookId)
}
