package kz.rmr.chatmachinist.widget

import kz.rmr.chatmachinist.model.ButtonDefinition
import kz.rmr.chatmachinist.model.ButtonRowDefinition
import java.time.DayOfWeek
import java.time.Month
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

fun calendarRows(
    year: Int,
    month: Int,
    locale: Locale = Locale("ru")
): List<ButtonRowDefinition> {
    val yearMonth = YearMonth.of(year, month)
    val rows = mutableListOf<ButtonRowDefinition>()

    val prevMonth = yearMonth.minusMonths(1)
    val nextMonth = yearMonth.plusMonths(1)

    // Row 1: Year navigation
    rows.add(
        ButtonRowDefinition(
            listOf(
                ButtonDefinition("<<", null, CalendarButtonType.CALENDAR_PREV_YEAR, null, (year - 1).toString()),
                ButtonDefinition(year.toString(), null, CalendarButtonType.CALENDAR_SELECT_YEAR, null),
                ButtonDefinition(">>", null, CalendarButtonType.CALENDAR_NEXT_YEAR, null, (year + 1).toString())
            )
        )
    )

    // Row 2: Month navigation
    val monthName = yearMonth.month.getDisplayName(TextStyle.FULL_STANDALONE, locale)
        .replaceFirstChar { it.uppercaseChar() }
    rows.add(
        ButtonRowDefinition(
            listOf(
                ButtonDefinition("<", null, CalendarButtonType.CALENDAR_PREV_MONTH, null, "${prevMonth.year}:${prevMonth.monthValue}"),
                ButtonDefinition(monthName, null, CalendarButtonType.CALENDAR_SELECT_MONTH, null),
                ButtonDefinition(">", null, CalendarButtonType.CALENDAR_NEXT_MONTH, null, "${nextMonth.year}:${nextMonth.monthValue}")
            )
        )
    )

    // Row 3: Day-of-week headers (Mon..Sun)
    val daysOfWeek = DayOfWeek.entries.map { it.getDisplayName(TextStyle.SHORT, locale) }
    rows.add(
        ButtonRowDefinition(
            daysOfWeek.map { ButtonDefinition(it, null, CalendarButtonType.CALENDAR_EMPTY, null) }
        )
    )

    // Rows 4+: Day grid
    val firstDayOfWeek = yearMonth.atDay(1).dayOfWeek
    val offsetDays = (firstDayOfWeek.value - DayOfWeek.MONDAY.value + 7) % 7
    val totalDays = yearMonth.lengthOfMonth()

    var dayCounter = 1
    var cellIndex = 0

    while (dayCounter <= totalDays) {
        val rowButtons = mutableListOf<ButtonDefinition>()
        for (col in 0 until 7) {
            if (cellIndex < offsetDays || dayCounter > totalDays) {
                rowButtons.add(ButtonDefinition(" ", null, CalendarButtonType.CALENDAR_EMPTY, null))
            } else {
                rowButtons.add(ButtonDefinition(dayCounter.toString(), null, CalendarButtonType.CALENDAR_DAY, null))
                dayCounter++
            }
            cellIndex++
        }
        rows.add(ButtonRowDefinition(rowButtons))
    }

    return rows
}

fun yearPickerRows(currentYear: Int): List<ButtonRowDefinition> {
    val startYear = currentYear - 4
    return (0 until 3).map { row ->
        ButtonRowDefinition(
            (0 until 3).map { col ->
                val year = startYear + row * 3 + col
                ButtonDefinition(year.toString(), null, CalendarButtonType.CALENDAR_YEAR, null)
            }
        )
    }
}

fun monthPickerRows(year: Int, locale: Locale = Locale("ru")): List<ButtonRowDefinition> {
    return (0 until 4).map { row ->
        ButtonRowDefinition(
            (0 until 3).map { col ->
                val monthValue = row * 3 + col + 1
                val monthName = Month.of(monthValue).getDisplayName(TextStyle.SHORT_STANDALONE, locale)
                    .replaceFirstChar { it.uppercaseChar() }
                ButtonDefinition(monthName, null, CalendarButtonType.CALENDAR_MONTH, null, monthValue.toString())
            }
        )
    }
}
