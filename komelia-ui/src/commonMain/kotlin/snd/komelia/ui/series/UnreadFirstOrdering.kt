package snd.komelia.ui.series

import kotlin.math.ceil

/**
 * "Unread first" ordering for series book lists: not completed books (unread and in progress) first,
 * then completed ones; inside each group by book number ascending.
 *
 * Komga pages are fetched with two queries (not completed / completed, both sorted by number),
 * so every page holds books of one group only. The first group's last page may be shorter than the page size.
 */
object UnreadFirstOrdering {

    enum class Group { NOT_COMPLETED, COMPLETED }

    data class PageLocation(val group: Group, val pageIndex: Int)

    fun pagesFor(totalElements: Long, pageSize: Int): Int =
        if (totalElements <= 0 || pageSize <= 0) 0 else ceil(totalElements.toDouble() / pageSize).toInt()

    /** Maps a combined zero-based page index to the group query and the page index inside that group. */
    fun locate(pageIndex: Int, notCompletedPages: Int): PageLocation =
        if (pageIndex < notCompletedPages) PageLocation(Group.NOT_COMPLETED, pageIndex)
        else PageLocation(Group.COMPLETED, pageIndex - notCompletedPages)

    fun <T> List<T>.sortedUnreadFirst(completed: (T) -> Boolean, numberSort: (T) -> Float): List<T> =
        sortedWith(compareBy<T> { completed(it) }.thenBy { numberSort(it) })
}
