package co.theportman.way_of_the_goat.data.scoring

import co.theportman.way_of_the_goat.data.remote.models.Activity
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WeeklyActivityBuilderTest {

    private val fixedTimeZone = TimeZone.UTC
    // 2026-02-18 is a Wednesday
    private val fixedClock = object : Clock {
        override fun now(): Instant = Instant.parse("2026-02-18T12:00:00Z")
    }
    private val builder = WeeklyActivityBuilder(clock = fixedClock, timeZone = fixedTimeZone)

    private fun activityOn(
        dateLocal: String,
        distanceMetres: Double? = 10000.0,
        id: String = "act-$dateLocal-${distanceMetres?.toInt()}"
    ): Activity {
        return Activity(
            id = id,
            startDateLocal = "${dateLocal}T08:00:00",
            distance = distanceMetres
        )
    }

    @Test
    fun `given empty input when buildWeeks then returns null`() {
        assertNull(builder.buildWeeks(emptyList()))
    }

    @Test
    fun `given single activity on Wednesday when buildWeeks then produces week with null Mon Tue and data Wed`() {
        // 2026-02-18 is a Wednesday
        val activities = listOf(activityOn("2026-02-18", 15000.0))

        val weeks = builder.buildWeeks(activities)
        assertNotNull(weeks)
        assertEquals(1, weeks.size)

        val week = weeks.first()
        assertEquals("Feb 16-22", week.dateRangeLabel)

        // Mon and Tue should be null
        assertNull(week.dailyActivities[0])
        assertNull(week.dailyActivities[1])

        // Wednesday should have data
        val wed = week.dailyActivities[2]
        assertNotNull(wed)
        assertEquals("Wednesday", wed.dayName)
        assertEquals(15.0, wed.distance)
        assertEquals(1, wed.activityCount)

        // Thu-Sun should be null
        for (i in 3..6) {
            assertNull(week.dailyActivities[i])
        }
    }

    @Test
    fun `given multiple activities on same day when buildWeeks then sums distances and counts correctly`() {
        val activities = listOf(
            activityOn("2026-02-18", 10000.0, id = "act-1"),
            activityOn("2026-02-18", 5000.0, id = "act-2"),
            activityOn("2026-02-18", 3000.0, id = "act-3")
        )

        val weeks = builder.buildWeeks(activities)
        assertNotNull(weeks)

        val wed = weeks.first().dailyActivities[2]
        assertNotNull(wed)
        assertEquals(18.0, wed.distance)
        assertEquals(3, wed.activityCount)
    }

    @Test
    fun `given activity with null distance when buildWeeks then counts toward activityCount and contributes zero distance`() {
        val activities = listOf(
            activityOn("2026-02-18", null, id = "act-null"),
            activityOn("2026-02-18", 10000.0, id = "act-10k")
        )

        val weeks = builder.buildWeeks(activities)
        assertNotNull(weeks)

        val wed = weeks.first().dailyActivities[2]
        assertNotNull(wed)
        assertEquals(10.0, wed.distance) // 0.0 + 10.0
        assertEquals(2, wed.activityCount)
    }

    @Test
    fun `given week spanning month boundary when buildWeeks then formats date range correctly`() {
        // 2026-01-26 is a Monday, week ends 2026-02-01 (Sunday)
        // Use a clock on that week
        val janClock = object : Clock {
            override fun now(): Instant = Instant.parse("2026-01-28T12:00:00Z")
        }
        val janBuilder = WeeklyActivityBuilder(clock = janClock, timeZone = fixedTimeZone)

        val activities = listOf(activityOn("2026-01-28", 12000.0))
        val weeks = janBuilder.buildWeeks(activities)
        assertNotNull(weeks)

        assertEquals("Jan 26-Feb 1", weeks.first().dateRangeLabel)
    }

    @Test
    fun `given activities in multiple weeks when buildWeeks then most recent week appears first`() {
        val activities = listOf(
            activityOn("2026-02-10", 5000.0), // Week of Feb 9-15
            activityOn("2026-02-18", 8000.0)  // Week of Feb 16-22 (current)
        )

        val weeks = builder.buildWeeks(activities)
        assertNotNull(weeks)
        assertTrue(weeks.size >= 2)

        assertEquals("Feb 16-22", weeks.first().dateRangeLabel)
        assertEquals("Feb 9-15", weeks.last().dateRangeLabel)
    }

    @Test
    fun `given activities in a week when buildWeeks then weekly total sums daily distances`() {
        val activities = listOf(
            activityOn("2026-02-16", 10000.0), // Monday
            activityOn("2026-02-18", 15000.0)  // Wednesday
        )

        val weeks = builder.buildWeeks(activities)
        assertNotNull(weeks)

        val week = weeks.first()
        assertEquals(25.0, week.weeklyTotalKm)
    }

    @Test
    fun `given 7 entries per week when buildWeekData then always returns 7 daily entries`() {
        val monday = LocalDate(2026, 2, 16)
        val activities = listOf(activityOn("2026-02-17", 10000.0)) // Only Tuesday

        val weekData = builder.buildWeekData(monday, activities)
        assertEquals(7, weekData.dailyActivities.size)
    }
}
