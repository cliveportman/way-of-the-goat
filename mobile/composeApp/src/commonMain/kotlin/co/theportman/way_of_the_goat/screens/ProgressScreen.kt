package co.theportman.way_of_the_goat.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn

@Composable
fun ProgressScreen(
    onDateClick: (LocalDate) -> Unit = {}
) {
    // Get today's date
    val today = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }

    // Find the Monday of the current week
    val currentWeekMonday = remember {
        val daysFromMonday = when (today.dayOfWeek) {
            DayOfWeek.MONDAY -> 0
            DayOfWeek.TUESDAY -> 1
            DayOfWeek.WEDNESDAY -> 2
            DayOfWeek.THURSDAY -> 3
            DayOfWeek.FRIDAY -> 4
            DayOfWeek.SATURDAY -> 5
            DayOfWeek.SUNDAY -> 6
            else -> 0
        }
        today.minus(daysFromMonday, DateTimeUnit.DAY)
    }

    // Number of weeks to show (e.g., last 52 weeks up to current week)
    val numberOfWeeks = 52

    // Create pager state starting at the last page (current week)
    val pagerState = rememberPagerState(
        initialPage = numberOfWeeks - 1,
        pageCount = { numberOfWeeks }
    )

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            // Calculate the Monday of this week
            val weeksAgo = numberOfWeeks - 1 - page
            val weekMonday = currentWeekMonday.minus(weeksAgo * 7, DateTimeUnit.DAY)
            val weekSunday = weekMonday.plus(6, DateTimeUnit.DAY)

            ProgressWeekContent(
                weekMonday = weekMonday,
                weekSunday = weekSunday,
                onDateClick = onDateClick
            )
        }
    }
}

@Composable
private fun ProgressWeekContent(
    weekMonday: LocalDate,
    weekSunday: LocalDate,
    onDateClick: (LocalDate) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Week date range heading
        Text(
            text = formatWeekRange(weekMonday, weekSunday),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        // 7 bars for days of the week
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            for (dayOffset in 0..6) {
                val currentDay = weekMonday.plus(dayOffset, DateTimeUnit.DAY)
                DayBar(
                    date = currentDay,
                    onClick = { onDateClick(currentDay) }
                )
            }
        }
    }
}

@Composable
private fun DayBar(date: LocalDate, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Day label (e.g., "Mon 21")
        Text(
            text = formatDayLabel(date),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(70.dp)
        )

        // Bar (empty for now, can be filled based on progress data)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(4.dp)
                )
        ) {
            // Empty bar for now - can be filled with progress data later
        }
    }
}

private fun formatWeekRange(monday: LocalDate, sunday: LocalDate): String {
    val mondayMonth = getMonthAbbreviation(monday.monthNumber)
    val sundayMonth = getMonthAbbreviation(sunday.monthNumber)

    return if (monday.year == sunday.year) {
        if (monday.monthNumber == sunday.monthNumber) {
            // Same month and year: "Jan 15 - 21, 2025"
            "$mondayMonth ${monday.dayOfMonth} - ${sunday.dayOfMonth}, ${monday.year}"
        } else {
            // Different months, same year: "Jan 29 - Feb 4, 2025"
            "$mondayMonth ${monday.dayOfMonth} - $sundayMonth ${sunday.dayOfMonth}, ${monday.year}"
        }
    } else {
        // Different years: "Dec 30, 2024 - Jan 5, 2025"
        "$mondayMonth ${monday.dayOfMonth}, ${monday.year} - $sundayMonth ${sunday.dayOfMonth}, ${sunday.year}"
    }
}

private fun formatDayLabel(date: LocalDate): String {
    val dayOfWeek = when (date.dayOfWeek) {
        DayOfWeek.MONDAY -> "Mon"
        DayOfWeek.TUESDAY -> "Tue"
        DayOfWeek.WEDNESDAY -> "Wed"
        DayOfWeek.THURSDAY -> "Thu"
        DayOfWeek.FRIDAY -> "Fri"
        DayOfWeek.SATURDAY -> "Sat"
        DayOfWeek.SUNDAY -> "Sun"
        else -> "?"
    }

    return "$dayOfWeek ${date.dayOfMonth}"
}

private fun getMonthAbbreviation(monthNumber: Int): String {
    return when (monthNumber) {
        1 -> "Jan"
        2 -> "Feb"
        3 -> "Mar"
        4 -> "Apr"
        5 -> "May"
        6 -> "Jun"
        7 -> "Jul"
        8 -> "Aug"
        9 -> "Sep"
        10 -> "Oct"
        11 -> "Nov"
        12 -> "Dec"
        else -> ""
    }
}
