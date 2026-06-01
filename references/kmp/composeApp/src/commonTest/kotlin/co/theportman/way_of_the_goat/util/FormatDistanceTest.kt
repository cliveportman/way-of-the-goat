package co.theportman.way_of_the_goat.util

import kotlin.test.Test
import kotlin.test.assertEquals

class FormatDistanceTest {

    @Test
    fun `whole number omits decimal`() {
        assertEquals("8", formatDistance(8.0))
    }

    @Test
    fun `one decimal place is shown`() {
        assertEquals("8.3", formatDistance(8.3))
    }

    @Test
    fun `value is rounded not truncated`() {
        assertEquals("9", formatDistance(8.97))
    }

    @Test
    fun `zero returns zero`() {
        assertEquals("0", formatDistance(0.0))
    }

    @Test
    fun `rounds up at midpoint`() {
        assertEquals("8.5", formatDistance(8.45))
    }

    @Test
    fun `large value formats correctly`() {
        assertEquals("86.6", formatDistance(86.6))
    }
}
