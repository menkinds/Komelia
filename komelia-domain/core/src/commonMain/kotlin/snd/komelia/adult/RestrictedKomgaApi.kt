package snd.komelia.adult

import snd.komelia.komga.api.KomgaApi
import snd.komelia.komga.api.KomgaBookApi
import snd.komelia.komga.api.KomgaCollectionsApi
import snd.komelia.komga.api.KomgaLibraryApi
import snd.komelia.komga.api.KomgaReadListApi
import snd.komelia.komga.api.KomgaReferentialApi
import snd.komelia.komga.api.KomgaSeriesApi
import snd.komelia.komga.api.model.KomeliaBook
import snd.komelia.komga.api.model.KomeliaSeries
import snd.komga.client.book.KomgaBookId
import snd.komga.client.book.KomgaBookSearch
import snd.komga.client.collection.KomgaCollection
import snd.komga.client.collection.KomgaCollectionId
import snd.komga.client.collection.KomgaCollectionQuery
import snd.komga.client.common.KomgaPageRequest
import snd.komga.client.common.Page
import snd.komga.client.library.KomgaLibrary
import snd.komga.client.library.KomgaLibraryId
import snd.komga.client.readlist.KomgaReadList
import snd.komga.client.readlist.KomgaReadListId
import snd.komga.client.readlist.KomgaReadListQuery
import snd.komga.client.search.BookConditionBuilder
import snd.komga.client.search.SeriesConditionBuilder
import snd.komga.client.series.KomgaSeriesId
import snd.komga.client.series.KomgaSeriesSearch

/** Thrown instead of returning content that belongs to the hidden library. */
class NotFoundException : RuntimeException("Not found")

/**
 * Central filter for the restricted library. Wraps the server api so that every view model
 * gets data without the hidden library. The lock state is read on each call, so lock changes
 * need no new instance (screens only have to reload).
 */
class RestrictedKomgaApi(
    private val delegate: KomgaApi,
    lock: AdultLockState,
) : KomgaApi by delegate {
    private val scope = LibraryScope(lock, delegate.libraryApi)

    override val libraryApi: KomgaLibraryApi = RestrictedLibraryApi(delegate.libraryApi, scope)
    override val seriesApi: KomgaSeriesApi = RestrictedSeriesApi(delegate.seriesApi, scope)
    override val bookApi: KomgaBookApi = RestrictedBookApi(delegate.bookApi, scope)
    override val collectionsApi: KomgaCollectionsApi = RestrictedCollectionsApi(delegate.collectionsApi, scope)
    override val readListApi: KomgaReadListApi = RestrictedReadListApi(delegate.readListApi, scope)
    override val referentialApi: KomgaReferentialApi = RestrictedReferentialApi(delegate.referentialApi, scope)
}

internal class LibraryScope(
    private val lock: AdultLockState,
    private val libraryApi: KomgaLibraryApi,
) {
    fun isHidden(libraryId: KomgaLibraryId) = lock.isHidden(libraryId)
    fun isActive() = lock.currentHiddenLibraryId() != null

    /**
     * Library ids for a server side `library_id` filter: every library except the hidden one.
     * Returns [requested] unchanged when nothing is hidden or when no other library is left
     * (an empty id list would mean "all libraries" on the server; the client side filter still applies then).
     */
    suspend fun restrict(requested: List<KomgaLibraryId>?): List<KomgaLibraryId>? {
        if (!isActive()) return requested
        val candidates = requested?.takeIf { it.isNotEmpty() } ?: libraryApi.getLibraries().map { it.id }
        val allowed = candidates.filter { !isHidden(it) }
        return if (allowed.isEmpty()) requested else allowed
    }

    suspend fun restrictList(requested: List<KomgaLibraryId>): List<KomgaLibraryId> =
        restrict(requested) ?: requested

    fun checkVisible(libraryId: KomgaLibraryId) {
        if (isHidden(libraryId)) throw NotFoundException()
    }

    fun <T> Page<T>.withoutHidden(libraryIdOf: (T) -> KomgaLibraryId): Page<T> {
        if (!isActive()) return this
        val filtered = content.filter { !isHidden(libraryIdOf(it)) }
        if (filtered.size == content.size) return this
        return Page(
            content = filtered,
            pageable = pageable,
            totalElements = (totalElements - (content.size - filtered.size)).coerceAtLeast(0),
            totalPages = totalPages,
            last = last,
            number = number,
            sort = sort,
            first = first,
            numberOfElements = filtered.size,
            size = size,
            empty = filtered.isEmpty()
        )
    }
}

internal class RestrictedLibraryApi(
    private val delegate: KomgaLibraryApi,
    private val scope: LibraryScope,
) : KomgaLibraryApi by delegate {
    override suspend fun getLibraries(): List<KomgaLibrary> =
        delegate.getLibraries().filter { !scope.isHidden(it.id) }

    override suspend fun getLibrary(libraryId: KomgaLibraryId): KomgaLibrary {
        scope.checkVisible(libraryId)
        return delegate.getLibrary(libraryId)
    }
}

internal class RestrictedSeriesApi(
    private val delegate: KomgaSeriesApi,
    private val scope: LibraryScope,
) : KomgaSeriesApi by delegate {

    override suspend fun getOneSeries(seriesId: KomgaSeriesId): KomeliaSeries {
        val series = delegate.getOneSeries(seriesId)
        scope.checkVisible(series.libraryId)
        return series
    }

    override suspend fun getSeriesList(
        conditionBuilder: SeriesConditionBuilder,
        fulltextSearch: String?,
        pageRequest: KomgaPageRequest?
    ): Page<KomeliaSeries> = with(scope) {
        delegate.getSeriesList(conditionBuilder, fulltextSearch, pageRequest).withoutHidden { it.libraryId }
    }

    override suspend fun getSeriesList(
        search: KomgaSeriesSearch,
        pageRequest: KomgaPageRequest?
    ): Page<KomeliaSeries> = with(scope) {
        delegate.getSeriesList(search, pageRequest).withoutHidden { it.libraryId }
    }

    override suspend fun getNewSeries(
        libraryIds: List<KomgaLibraryId>?,
        oneshot: Boolean?,
        deleted: Boolean?,
        pageRequest: KomgaPageRequest?
    ): Page<KomeliaSeries> = with(scope) {
        delegate.getNewSeries(restrict(libraryIds), oneshot, deleted, pageRequest).withoutHidden { it.libraryId }
    }

    override suspend fun getUpdatedSeries(
        libraryIds: List<KomgaLibraryId>?,
        oneshot: Boolean?,
        deleted: Boolean?,
        pageRequest: KomgaPageRequest?
    ): Page<KomeliaSeries> = with(scope) {
        delegate.getUpdatedSeries(restrict(libraryIds), oneshot, deleted, pageRequest).withoutHidden { it.libraryId }
    }
}

internal class RestrictedBookApi(
    private val delegate: KomgaBookApi,
    private val scope: LibraryScope,
) : KomgaBookApi by delegate {

    override suspend fun getOne(bookId: KomgaBookId): KomeliaBook {
        val book = delegate.getOne(bookId)
        scope.checkVisible(book.libraryId)
        return book
    }

    override suspend fun getBookList(
        conditionBuilder: BookConditionBuilder,
        fullTextSearch: String?,
        pageRequest: KomgaPageRequest?
    ): Page<KomeliaBook> = with(scope) {
        delegate.getBookList(conditionBuilder, fullTextSearch, pageRequest).withoutHidden { it.libraryId }
    }

    override suspend fun getBookList(
        search: KomgaBookSearch,
        pageRequest: KomgaPageRequest?
    ): Page<KomeliaBook> = with(scope) {
        delegate.getBookList(search, pageRequest).withoutHidden { it.libraryId }
    }

    override suspend fun getLatestBooks(pageRequest: KomgaPageRequest?): Page<KomeliaBook> = with(scope) {
        delegate.getLatestBooks(pageRequest).withoutHidden { it.libraryId }
    }

    override suspend fun getBooksOnDeck(
        libraryIds: List<KomgaLibraryId>?,
        pageRequest: KomgaPageRequest?
    ): Page<KomeliaBook> = with(scope) {
        delegate.getBooksOnDeck(restrict(libraryIds), pageRequest).withoutHidden { it.libraryId }
    }

    override suspend fun getDuplicateBooks(pageRequest: KomgaPageRequest?): Page<KomeliaBook> = with(scope) {
        delegate.getDuplicateBooks(pageRequest).withoutHidden { it.libraryId }
    }

    override suspend fun getBookSiblingPrevious(bookId: KomgaBookId): KomeliaBook? =
        delegate.getBookSiblingPrevious(bookId)?.takeIf { !scope.isHidden(it.libraryId) }

    override suspend fun getBookSiblingNext(bookId: KomgaBookId): KomeliaBook? =
        delegate.getBookSiblingNext(bookId)?.takeIf { !scope.isHidden(it.libraryId) }
}

internal class RestrictedCollectionsApi(
    private val delegate: KomgaCollectionsApi,
    private val scope: LibraryScope,
) : KomgaCollectionsApi by delegate {

    override suspend fun getAll(
        search: String?,
        libraryIds: List<KomgaLibraryId>?,
        pageRequest: KomgaPageRequest?
    ): Page<KomgaCollection> = delegate.getAll(search, scope.restrict(libraryIds), pageRequest)

    override suspend fun getSeriesForCollection(
        id: KomgaCollectionId,
        query: KomgaCollectionQuery?,
        pageRequest: KomgaPageRequest?
    ): Page<KomeliaSeries> = with(scope) {
        delegate.getSeriesForCollection(id, query, pageRequest).withoutHidden { it.libraryId }
    }
}

internal class RestrictedReadListApi(
    private val delegate: KomgaReadListApi,
    private val scope: LibraryScope,
) : KomgaReadListApi by delegate {

    override suspend fun getAll(
        search: String?,
        libraryIds: List<KomgaLibraryId>?,
        pageRequest: KomgaPageRequest?
    ): Page<KomgaReadList> = delegate.getAll(search, scope.restrict(libraryIds), pageRequest)

    override suspend fun getBooksForReadList(
        id: KomgaReadListId,
        query: KomgaReadListQuery?,
        pageRequest: KomgaPageRequest?
    ): Page<KomeliaBook> = with(scope) {
        delegate.getBooksForReadList(id, query, pageRequest).withoutHidden { it.libraryId }
    }

    override suspend fun getBookSiblingNext(readListId: KomgaReadListId, bookId: KomgaBookId): KomeliaBook? =
        delegate.getBookSiblingNext(readListId, bookId)?.takeIf { !scope.isHidden(it.libraryId) }

    override suspend fun getBookSiblingPrevious(readListId: KomgaReadListId, bookId: KomgaBookId): KomeliaBook? =
        delegate.getBookSiblingPrevious(readListId, bookId)?.takeIf { !scope.isHidden(it.libraryId) }
}

/** Filter suggestions (genres, tags, publishers...) must not reveal values of the hidden library. */
internal class RestrictedReferentialApi(
    private val delegate: KomgaReferentialApi,
    private val scope: LibraryScope,
) : KomgaReferentialApi by delegate {

    override suspend fun getAuthors(
        search: String?,
        role: String?,
        libraryIds: List<KomgaLibraryId>,
        collectionId: KomgaCollectionId?,
        seriesId: KomgaSeriesId?,
        readListId: KomgaReadListId?,
        pageRequest: KomgaPageRequest?
    ) = delegate.getAuthors(
        search, role, scope.restrictList(libraryIds), collectionId, seriesId, readListId, pageRequest
    )

    override suspend fun getGenres(libraryIds: List<KomgaLibraryId>, collectionId: KomgaCollectionId?) =
        delegate.getGenres(scope.restrictList(libraryIds), collectionId)

    override suspend fun getSharingLabels(libraryIds: List<KomgaLibraryId>, collectionId: KomgaCollectionId?) =
        delegate.getSharingLabels(scope.restrictList(libraryIds), collectionId)

    override suspend fun getTags(libraryIds: List<KomgaLibraryId>, collectionId: KomgaCollectionId?) =
        delegate.getTags(scope.restrictList(libraryIds), collectionId)

    override suspend fun getBookTags(
        seriesId: KomgaSeriesId?,
        readListId: KomgaReadListId?,
        libraryIds: List<KomgaLibraryId>
    ) = delegate.getBookTags(seriesId, readListId, scope.restrictList(libraryIds))

    override suspend fun getLanguages(libraryIds: List<KomgaLibraryId>, collectionId: KomgaCollectionId?) =
        delegate.getLanguages(scope.restrictList(libraryIds), collectionId)

    override suspend fun getPublishers(libraryIds: List<KomgaLibraryId>, collectionId: KomgaCollectionId?) =
        delegate.getPublishers(scope.restrictList(libraryIds), collectionId)

    override suspend fun getAgeRatings(libraryIds: List<KomgaLibraryId>, collectionId: KomgaCollectionId?) =
        delegate.getAgeRatings(scope.restrictList(libraryIds), collectionId)

    override suspend fun getSeriesReleaseDates(libraryIds: List<KomgaLibraryId>, collectionId: KomgaCollectionId?) =
        delegate.getSeriesReleaseDates(scope.restrictList(libraryIds), collectionId)
}
