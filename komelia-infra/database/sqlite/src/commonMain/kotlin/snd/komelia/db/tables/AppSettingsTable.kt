package snd.komelia.db.tables

import org.jetbrains.exposed.v1.core.Table

object AppSettingsTable : Table("AppSettings") {
    val version = integer("version")
    val username = text("username")
    val serverUrl = text("serverUrl")
    val cardWidth = integer("card_width")
    val seriesPageLoadSize = integer("series_page_load_size")

    val bookPageLoadSize = integer("book_page_load_size")
    val bookListLayout = text("book_list_layout")
    val appTheme = text("app_theme")

    val checkForUpdatesOnStartup = bool("check_for_updates_on_startup")

    //FIXME Android doesn't support JDBC 4.1.
    // timestamp field type uses java.sql.ResultSet.getObject(int columnIndex, Class<T> type)
    // which does not exist on Android. why???
    val updateLastCheckedTimestamp = text("update_last_checked_timestamp").nullable()
    val updateLastCheckedReleaseVersion = text("update_last_checked_release_version").nullable()
    val updateDismissedVersion = text("update_dismissed_version").nullable()
    // comma separated library ids. Old single id column fork_restricted_library_id is not used since V17
    val restrictedLibraryIds = text("fork_restricted_library_ids")
    val librarySeriesFilter = text("fork_library_series_filter")

    override val primaryKey = PrimaryKey(version)
}