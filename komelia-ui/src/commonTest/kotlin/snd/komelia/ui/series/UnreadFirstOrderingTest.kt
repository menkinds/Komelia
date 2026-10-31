package snd.komelia.ui.series

import snd.komelia.ui.series.UnreadFirstOrdering.Group
import snd.komelia.ui.series.UnreadFirstOrdering.PageLocation
import snd.komelia.ui.series.UnreadFirstOrdering.sortedUnreadFirst
import kotlin.test.Test
import kotlin.test.assertEquals

class UnreadFirstOrderingTest {
    private enum class State { UNREAD, IN_PROGRESS, READ }
    private data class B(val number: Float, val state: State)

    private fun sort(books: List<B>) =
        books.sortedUnreadFirst(completed = { it.state == State.READ }, numberSort = { it.number })

    @Test
    fun notCompletedFirstThenCompleted_eachByNumber() {
        val books = listOf(
            B(1f, State.READ), B(5f, State.UNREAD), B(2f, State.READ),
            B(4f, State.IN_PROGRESS), B(3f, State.UNREAD),
        )
        assertEquals(
            listOf(3f, 4f, 5f, 1f, 2f),
            sort(books).map { it.number }
        )
    }

    @Test
    fun inProgressIsTreatedLikeUnread() {
        val sorted = sort(listOf(B(1f, State.READ), B(2f, State.IN_PROGRESS), B(3f, State.UNREAD)))
        assertEquals(listOf(State.IN_PROGRESS, State.UNREAD, State.READ), sorted.map { it.state })
    }

    @Test
    fun fractionalNumbersKeepNumericOrder() {
        val sorted = sort(listOf(B(10f, State.UNREAD), B(2.5f, State.UNREAD), B(2f, State.UNREAD)))
        assertEquals(listOf(2f, 2.5f, 10f), sorted.map { it.number })
    }

    @Test
    fun pagesFor() {
        assertEquals(0, UnreadFirstOrdering.pagesFor(0, 20))
        assertEquals(1, UnreadFirstOrdering.pagesFor(20, 20))
        assertEquals(2, UnreadFirstOrdering.pagesFor(21, 20))
    }

    @Test
    fun locateCrossesFromNotCompletedToCompleted() {
        // 45 not completed books, page size 20 -> 3 pages (20, 20, 5), then completed pages
        val pages = UnreadFirstOrdering.pagesFor(45, 20)
        assertEquals(PageLocation(Group.NOT_COMPLETED, 2), UnreadFirstOrdering.locate(2, pages))
        assertEquals(PageLocation(Group.COMPLETED, 0), UnreadFirstOrdering.locate(3, pages))
        assertEquals(PageLocation(Group.COMPLETED, 1), UnreadFirstOrdering.locate(4, pages))
    }

    @Test
    fun locateWithNoNotCompletedBooks() {
        assertEquals(PageLocation(Group.COMPLETED, 0), UnreadFirstOrdering.locate(0, 0))
    }
}
