package snd.komelia.ui.series

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.datetime.until
import snd.komelia.AppNotifications
import snd.komelia.komga.api.KomgaReferentialApi
import snd.komelia.ui.library.LibrarySeriesTabState.SeriesSort
import snd.komelia.ui.library.SeriesScreenFilter
import snd.komelia.ui.series.SeriesFilter.Companion.DEFAULT
import snd.komelia.ui.series.SeriesFilterState.Completion
import snd.komelia.ui.series.SeriesFilterState.Format
import snd.komelia.ui.series.SeriesFilterState.TagExclusionMode
import snd.komelia.ui.series.SeriesFilterState.TagInclusionMode
import snd.komga.client.book.KomgaReadStatus
import snd.komga.client.common.KomgaAuthor
import snd.komga.client.library.KomgaLibrary
import snd.komga.client.search.KomgaSearchCondition
import snd.komga.client.search.SeriesConditionBuilder
import snd.komga.client.series.KomgaSeriesStatus

data class SeriesFilter(
    val isChanged: Boolean = false,
    val searchTerm: String = "",
    val sortOrder: SeriesSort = SeriesSort.TITLE_ASC,
    val readStatus: List<KomgaReadStatus> = emptyList(),
    val publicationStatus: List<KomgaSeriesStatus> = emptyList(),

    val includeGenres: List<String> = emptyList(),
    val includeTags: List<String> = emptyList(),
    val excludeGenres: List<String> = emptyList(),
    val excludeTags: List<String> = emptyList(),
    val inclusionMode: TagInclusionMode = TagInclusionMode.INCLUDE_IF_ALL_MATCH,
    val exclusionMode: TagExclusionMode = TagExclusionMode.EXCLUDE_IF_ANY_MATCH,

    val authors: List<KomgaAuthor> = emptyList(),
    val releaseDates: List<String> = emptyList(),
    val ageRatings: List<String> = emptyList(),
    val publishers: List<String> = emptyList(),
    val languages: List<String> = emptyList(),
    val complete: Completion = Completion.ANY,
    val oneshot: Format = Format.ANY,
) {

    companion object {
        val DEFAULT = SeriesFilter()
    }

    fun addConditionTo(builder: SeriesConditionBuilder) {
        if (publicationStatus.isNotEmpty()) {
            builder.anyOf {
                publicationStatus.forEach { seriesStatus { isEqualTo(it) } }
            }
        }

        if (readStatus.isNotEmpty()) {
            builder.anyOf {
                readStatus.forEach { readStatus { isEqualTo(it) } }
            }
        }

        if (publishers.isNotEmpty()) {
            builder.anyOf {
                publishers.forEach { publisher { isEqualTo(it) } }
            }
        }

        if (languages.isNotEmpty()) {
            builder.anyOf {
                languages.forEach { language { isEqualTo(it) } }
            }
        }

        if (includeTags.isNotEmpty() || includeGenres.isNotEmpty()) {
            when (inclusionMode) {
                TagInclusionMode.INCLUDE_IF_ALL_MATCH -> builder.allOf {
                    includeGenres.forEach { genre { isEqualTo(it) } }
                    includeTags.forEach { tag { isEqualTo(it) } }
                }

                TagInclusionMode.INCLUDE_IF_ANY_MATCH -> builder.anyOf {
                    includeGenres.forEach { genre { isEqualTo(it) } }
                    includeTags.forEach { tag { isEqualTo(it) } }
                }
            }
        }
        if (excludeTags.isNotEmpty() || excludeGenres.isNotEmpty()) {
            when (exclusionMode) {
                TagExclusionMode.EXCLUDE_IF_ANY_MATCH -> builder.allOf {
                    excludeGenres.forEach { genre { isNotEqualTo(it) } }
                    excludeTags.forEach { tag { isNotEqualTo(it) } }
                }

                TagExclusionMode.EXCLUDE_IF_ALL_MATCH -> builder.anyOf {
                    excludeGenres.forEach { genre { isNotEqualTo(it) } }
                    excludeTags.forEach { tag { isNotEqualTo(it) } }
                }
            }
        }

        if (ageRatings.isNotEmpty()) {
            builder.anyOf {
                ageRatings.forEach {
                    ageRating {
                        if (it == "None") isNull()
                        else isEqualTo(it.toInt())
                    }
                }
            }
        }

        if (releaseDates.isNotEmpty()) {
            builder.anyOf {
                releaseDates.forEach {
                    allOf {
                        releaseDate {
                            isAfter(dateAtLastDayInYear(it.toInt() - 1).atStartOfDayIn(TimeZone.UTC))
                        }
                        releaseDate {
                            isBefore(LocalDate(it.toInt() + 1, 1, 1).atStartOfDayIn(TimeZone.UTC))
                        }

                    }
                }

            }
        }

        authors.forEach {
            builder.author { isEqualTo(KomgaSearchCondition.AuthorMatch(it.name, null)) }
        }
        when (complete) {
            Completion.ANY -> {}
            Completion.COMPLETE -> builder.isCompleted()
            Completion.INCOMPLETE -> builder.isNotCompleted()
        }
        when (oneshot) {
            Format.ANY -> {}
            Format.ONESHOT -> builder.isOneshot()
            Format.NOT_ONESHOT -> builder.isNotOneshot()
        }
    }

    private fun dateAtLastDayInYear(year: Int): LocalDate {
        val start = LocalDate(year, 12, 1)
        val end = start.plus(1, DateTimeUnit.MONTH)
        val day = start.until(end, DateTimeUnit.DAY)
        return LocalDate(year, 12, day.toInt())
    }
}

class SeriesFilterState(
    defaultSort: SeriesSort,
    private val library: StateFlow<KomgaLibrary?>,
    private val referentialApi: KomgaReferentialApi,
    private val appNotifications: AppNotifications,
) {

    private val mutableFilterState = MutableStateFlow(SeriesFilter(sortOrder = defaultSort))
    val state = mutableFilterState.asStateFlow()

    var isChanged by mutableStateOf(false)
        private set
    var genresOptions by mutableStateOf<List<String>>(emptyList())
        private set
    var tagOptions by mutableStateOf<List<String>>(emptyList())
        private set
    var authorsOptions by mutableStateOf<List<KomgaAuthor>>(emptyList())
        private set
    var releaseDateOptions by mutableStateOf<List<String>>(emptyList())
        private set
    var ageRatingsOptions by mutableStateOf<List<String>>(emptyList())
        private set
    var publishersOptions by mutableStateOf<List<String>>(emptyList())
        private set
    var languagesOptions by mutableStateOf<List<String>>(emptyList())
        private set

    // referential option lists were fetched successfully, so saved values can be checked against them
    private var optionsLoaded = false

    // saved values that don't exist in the current library. They are not applied but are kept when saving
    private var unavailable = SavedSeriesFilter()

    suspend fun initialize() {
        appNotifications.runCatchingToNotifications {
            genresOptions = referentialApi.getGenres(libraryIds = library.value?.id?.let { listOf(it) }.orEmpty())
            tagOptions = referentialApi.getTags(libraryIds = library.value?.id?.let { listOf(it) }.orEmpty())
            releaseDateOptions = referentialApi.getSeriesReleaseDates(
                libraryIds = library.value?.id?.let { listOf(it) }.orEmpty()
            )
            ageRatingsOptions = referentialApi.getAgeRatings(
                libraryIds = library.value?.id?.let { listOf(it) }.orEmpty()
            )
            publishersOptions = referentialApi.getPublishers(
                libraryIds = library.value?.id?.let { listOf(it) }.orEmpty()
            )
            languagesOptions = referentialApi.getLanguages(
                libraryIds = library.value?.id?.let { listOf(it) }.orEmpty()
            )
            optionsLoaded = true
        }
    }

    /** Applies a previously saved filter. Values that are not available in the current library are skipped. */
    suspend fun restore(saved: SavedSeriesFilter) {
        if (!optionsLoaded) {
            mutableFilterState.value = saved.toFilter()
            unavailable = SavedSeriesFilter()
            checkIfAllDefault()
            return
        }

        val knownAuthors = saved.authors.map { it.name }.distinct().filter { name ->
            // lookup failure keeps the value
            runCatching { referentialApi.getAuthors(name).content.any { it.name == name } }.getOrDefault(true)
        }.toSet()

        val skipped = SavedSeriesFilter(
            includeGenres = saved.includeGenres.filter { it !in genresOptions },
            excludeGenres = saved.excludeGenres.filter { it !in genresOptions },
            includeTags = saved.includeTags.filter { it !in tagOptions },
            excludeTags = saved.excludeTags.filter { it !in tagOptions },
            releaseDates = saved.releaseDates.filter { it !in releaseDateOptions },
            ageRatings = saved.ageRatings.filter { it !in ageRatingsOptions },
            publishers = saved.publishers.filter { it !in publishersOptions },
            languages = saved.languages.filter { it !in languagesOptions },
            authors = saved.authors.filter { it.name !in knownAuthors },
        )
        unavailable = skipped
        mutableFilterState.value = saved.copy(
            includeGenres = saved.includeGenres - skipped.includeGenres.toSet(),
            excludeGenres = saved.excludeGenres - skipped.excludeGenres.toSet(),
            includeTags = saved.includeTags - skipped.includeTags.toSet(),
            excludeTags = saved.excludeTags - skipped.excludeTags.toSet(),
            releaseDates = saved.releaseDates - skipped.releaseDates.toSet(),
            ageRatings = saved.ageRatings - skipped.ageRatings.toSet(),
            publishers = saved.publishers - skipped.publishers.toSet(),
            languages = saved.languages - skipped.languages.toSet(),
            authors = saved.authors - skipped.authors.toSet(),
        ).toFilter()
        checkIfAllDefault()
    }

    /** Current sort and filters (without search term) plus saved values that were skipped as unavailable */
    fun toSaved(): SavedSeriesFilter {
        val current = SavedSeriesFilter.fromFilter(state.value)
        val skipped = unavailable
        return current.copy(
            includeGenres = (current.includeGenres + skipped.includeGenres).distinct(),
            excludeGenres = (current.excludeGenres + skipped.excludeGenres).distinct(),
            includeTags = (current.includeTags + skipped.includeTags).distinct(),
            excludeTags = (current.excludeTags + skipped.excludeTags).distinct(),
            releaseDates = (current.releaseDates + skipped.releaseDates).distinct(),
            ageRatings = (current.ageRatings + skipped.ageRatings).distinct(),
            publishers = (current.publishers + skipped.publishers).distinct(),
            languages = (current.languages + skipped.languages).distinct(),
            authors = (current.authors + skipped.authors).distinct(),
        )
    }

    fun applyFilter(filter: SeriesScreenFilter) {
        mutableFilterState.value = SeriesFilter(
            publicationStatus = filter.publicationStatus ?: DEFAULT.publicationStatus,
            ageRatings = filter.ageRating?.map { it.toString() } ?: DEFAULT.ageRatings,
            languages = filter.language ?: DEFAULT.languages,
            publishers = filter.publisher ?: DEFAULT.publishers,
            includeGenres = filter.genres ?: DEFAULT.includeGenres,
            includeTags = filter.tags ?: DEFAULT.includeTags,
            authors = filter.authors ?: DEFAULT.authors
        )
        checkIfAllDefault()
    }

    fun onSortOrderChange(sortOrder: SeriesSort) {
        mutableFilterState.update { it.copy(sortOrder = sortOrder) }
        checkIfAllDefault()
    }

    fun onSearchTermChange(searchTerm: String) {
        mutableFilterState.update { current -> current.copy(searchTerm = searchTerm) }
        checkIfAllDefault()
    }

    fun onReadStatusSelect(readStatus: KomgaReadStatus) {
        mutableFilterState.update { current ->
            current.copy(
                readStatus = if (current.readStatus.contains(readStatus)) {
                    current.readStatus.minus(readStatus)
                } else {
                    current.readStatus.plus(readStatus)
                }
            )
        }

        checkIfAllDefault()
    }

    fun onPublicationStatusSelect(publicationStatus: KomgaSeriesStatus) {
        mutableFilterState.update { current ->
            current.copy(
                publicationStatus = if (current.publicationStatus.contains(publicationStatus)) {
                    current.publicationStatus.minus(publicationStatus)
                } else {
                    current.publicationStatus.plus(publicationStatus)
                }
            )
        }
        checkIfAllDefault()
    }


    fun onGenreSelect(genre: String) {
        mutableFilterState.update { current ->
            if (current.includeGenres.contains(genre)) {
                current.copy(
                    includeGenres = current.includeGenres.minus(genre),
                    excludeGenres = current.excludeGenres.plus(genre)
                )
            } else if (current.excludeGenres.contains(genre)) {
                current.copy(
                    excludeGenres = current.excludeGenres.minus(genre)
                )
            } else current.copy(
                includeGenres = current.includeGenres.plus(genre)
            )
        }
        checkIfAllDefault()
    }

    fun onTagSelect(tag: String) {
        mutableFilterState.update { current ->
            if (current.includeTags.contains(tag)) {
                current.copy(
                    includeTags = current.includeTags.minus(tag),
                    excludeTags = current.excludeTags.plus(tag)
                )
            } else if (current.excludeTags.contains(tag)) {
                current.copy(
                    excludeTags = current.excludeTags.minus(tag)
                )
            } else current.copy(
                includeTags = current.includeTags.plus(tag)
            )
        }
        checkIfAllDefault()
    }

    fun onInclusionModeChange(mode: TagInclusionMode) {
        mutableFilterState.update { current -> current.copy(inclusionMode = mode) }
        checkIfAllDefault()
    }

    fun onExclusionModeChange(mode: TagExclusionMode) {
        mutableFilterState.update { current -> current.copy(exclusionMode = mode) }
        checkIfAllDefault()
    }

    suspend fun onAuthorsSearch(search: String) {
        if (search.isBlank()) this.authorsOptions = emptyList()
        else this.authorsOptions = referentialApi.getAuthors(search).content
    }

    fun onAuthorSelect(author: KomgaAuthor) {
        val authorsByName = authorsOptions.filter { it.name == author.name }
        mutableFilterState.update { current ->
            current.copy(
                authors = if (current.authors.contains(author))
                    current.authors.filter { it.name != author.name }
                else current.authors.plus(authorsByName)
            )
        }

        checkIfAllDefault()
    }

    fun onAgeRatingSelect(ageRating: String) {
        mutableFilterState.update { current ->
            current.copy(
                ageRatings = if (current.ageRatings.contains(ageRating))
                    current.ageRatings.minus(ageRating)
                else current.ageRatings.plus(ageRating)

            )
        }

        checkIfAllDefault()
    }

    fun onPublisherSelect(publisher: String) {
        mutableFilterState.update { current ->
            current.copy(
                publishers = if (current.publishers.contains(publisher))
                    current.publishers.minus(publisher)
                else current.publishers.plus(publisher)
            )
        }

        checkIfAllDefault()
    }

    fun onLanguageSelect(language: String) {
        mutableFilterState.update { current ->
            current.copy(
                languages = if (current.languages.contains(language)) current.languages.minus(language)
                else current.languages.plus(language)
            )
        }

        checkIfAllDefault()
    }

    fun onReleaseDateSelect(releaseDate: String) {
        mutableFilterState.update { current ->
            current.copy(
                releaseDates = if (current.releaseDates.contains(releaseDate))
                    current.releaseDates.minus(releaseDate)
                else current.releaseDates.plus(releaseDate)
            )
        }
        checkIfAllDefault()
    }

    fun onCompletionToggle() {
        mutableFilterState.update {
            it.copy(
                complete = when (it.complete) {
                    Completion.ANY -> Completion.COMPLETE
                    Completion.COMPLETE -> Completion.INCOMPLETE
                    Completion.INCOMPLETE -> Completion.ANY
                }
            )
        }
        checkIfAllDefault()
    }

    fun onFormatToggle() {
        mutableFilterState.update {
            it.copy(
                oneshot = when (it.oneshot) {
                    Format.ANY -> Format.ONESHOT
                    Format.ONESHOT -> Format.NOT_ONESHOT
                    Format.NOT_ONESHOT -> Format.ANY
                }
            )
        }
        checkIfAllDefault()
    }

    fun reset() {
        unavailable = SavedSeriesFilter()
        isChanged = false
        mutableFilterState.value = DEFAULT
    }

    fun resetTagFilters() {
        unavailable = unavailable.copy(
            includeGenres = emptyList(),
            includeTags = emptyList(),
            excludeGenres = emptyList(),
            excludeTags = emptyList(),
        )
        mutableFilterState.update {
            it.copy(
                includeGenres = DEFAULT.includeGenres,
                includeTags = DEFAULT.includeTags,
                inclusionMode = DEFAULT.inclusionMode,
                excludeGenres = DEFAULT.excludeGenres,
                excludeTags = DEFAULT.excludeTags,
                exclusionMode = DEFAULT.exclusionMode

            )
        }
    }

    private fun checkIfAllDefault() {
        isChanged = state.value != DEFAULT
    }

    enum class Completion {
        ANY, COMPLETE, INCOMPLETE
    }

    enum class Format {
        ANY, ONESHOT, NOT_ONESHOT
    }

    enum class TagInclusionMode {
        INCLUDE_IF_ALL_MATCH, INCLUDE_IF_ANY_MATCH
    }

    enum class TagExclusionMode {
        EXCLUDE_IF_ANY_MATCH, EXCLUDE_IF_ALL_MATCH
    }
}
