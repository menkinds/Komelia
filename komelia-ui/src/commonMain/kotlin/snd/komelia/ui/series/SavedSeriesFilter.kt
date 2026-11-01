package snd.komelia.ui.series

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import snd.komelia.ui.library.LibrarySeriesTabState.SeriesSort
import snd.komelia.ui.series.SeriesFilterState.Completion
import snd.komelia.ui.series.SeriesFilterState.Format
import snd.komelia.ui.series.SeriesFilterState.TagExclusionMode
import snd.komelia.ui.series.SeriesFilterState.TagInclusionMode
import snd.komga.client.book.KomgaReadStatus
import snd.komga.client.common.KomgaAuthor
import snd.komga.client.series.KomgaSeriesStatus

/**
 * Persisted form of [SeriesFilter] (sort and filters). The search term is not stored.
 * Enums are stored by name and unknown names are dropped when reading.
 */
@Serializable
data class SavedSeriesFilter(
    val sortOrder: String? = null,
    val readStatus: List<String> = emptyList(),
    val publicationStatus: List<String> = emptyList(),
    val includeGenres: List<String> = emptyList(),
    val includeTags: List<String> = emptyList(),
    val excludeGenres: List<String> = emptyList(),
    val excludeTags: List<String> = emptyList(),
    val inclusionMode: String? = null,
    val exclusionMode: String? = null,
    val authors: List<SavedAuthor> = emptyList(),
    val releaseDates: List<String> = emptyList(),
    val ageRatings: List<String> = emptyList(),
    val publishers: List<String> = emptyList(),
    val languages: List<String> = emptyList(),
    val complete: String? = null,
    val oneshot: String? = null,
) {

    @Serializable
    data class SavedAuthor(val name: String, val role: String)

    fun toJson(): String = json.encodeToString(serializer(), this)

    fun toFilter(): SeriesFilter {
        val default = SeriesFilter.DEFAULT
        return SeriesFilter(
            sortOrder = sortOrder.toEnum<SeriesSort>() ?: default.sortOrder,
            readStatus = readStatus.mapNotNull { it.toEnum<KomgaReadStatus>() },
            publicationStatus = publicationStatus.mapNotNull { it.toEnum<KomgaSeriesStatus>() },
            includeGenres = includeGenres,
            includeTags = includeTags,
            excludeGenres = excludeGenres,
            excludeTags = excludeTags,
            inclusionMode = inclusionMode.toEnum<TagInclusionMode>() ?: default.inclusionMode,
            exclusionMode = exclusionMode.toEnum<TagExclusionMode>() ?: default.exclusionMode,
            authors = authors.map { KomgaAuthor(it.name, it.role) },
            releaseDates = releaseDates,
            ageRatings = ageRatings,
            publishers = publishers,
            languages = languages,
            complete = complete.toEnum<Completion>() ?: default.complete,
            oneshot = oneshot.toEnum<Format>() ?: default.oneshot,
        )
    }

    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        fun fromFilter(filter: SeriesFilter) = SavedSeriesFilter(
            sortOrder = filter.sortOrder.name,
            readStatus = filter.readStatus.map { it.name },
            publicationStatus = filter.publicationStatus.map { it.name },
            includeGenres = filter.includeGenres,
            includeTags = filter.includeTags,
            excludeGenres = filter.excludeGenres,
            excludeTags = filter.excludeTags,
            inclusionMode = filter.inclusionMode.name,
            exclusionMode = filter.exclusionMode.name,
            authors = filter.authors.map { SavedAuthor(it.name, it.role) },
            releaseDates = filter.releaseDates,
            ageRatings = filter.ageRatings,
            publishers = filter.publishers,
            languages = filter.languages,
            complete = filter.complete.name,
            oneshot = filter.oneshot.name,
        )

        /** Returns null for empty or unreadable data (defaults are used then) */
        fun fromJson(value: String): SavedSeriesFilter? {
            if (value.isBlank()) return null
            return runCatching { json.decodeFromString(serializer(), value) }.getOrNull()
        }

        private inline fun <reified E : Enum<E>> String?.toEnum(): E? {
            if (this == null) return null
            return enumValues<E>().firstOrNull { it.name == this }
        }
    }
}
