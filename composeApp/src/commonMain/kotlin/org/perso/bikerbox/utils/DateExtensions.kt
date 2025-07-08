package org.perso.bikerbox.utils

import androidx.compose.runtime.Composable
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.plus
import org.jetbrains.compose.resources.stringResource
import bikerbox.composeapp.generated.resources.Res
import bikerbox.composeapp.generated.resources.april
import bikerbox.composeapp.generated.resources.august
import bikerbox.composeapp.generated.resources.december
import bikerbox.composeapp.generated.resources.february
import bikerbox.composeapp.generated.resources.january
import bikerbox.composeapp.generated.resources.july
import bikerbox.composeapp.generated.resources.june
import bikerbox.composeapp.generated.resources.march
import bikerbox.composeapp.generated.resources.may
import bikerbox.composeapp.generated.resources.november
import bikerbox.composeapp.generated.resources.october
import bikerbox.composeapp.generated.resources.september

fun LocalDate.plusDays(days: Int): LocalDate {
    return this.plus(DatePeriod(days = days))
}

fun LocalDate.daysBetween(other: LocalDate): Int {
    var days = 0
    var currentDate = this

    if (this > other) {
        return other.daysBetween(this)
    }

    while (currentDate < other) {
        currentDate = currentDate.plusDays(1)
        days++
    }

    return days
}
@Composable
fun LocalDateTime.toFormattedString(): String {
    val monthName = getLocalizedMonthName(monthNumber)
    val formattedTime = String.format("%02d:%02d", hour, minute)
    return "$dayOfMonth $monthName $year à $formattedTime"
}
@Composable
private fun getLocalizedMonthName(monthNumber: Int): String {
    return when (monthNumber) {
        1 -> stringResource(Res.string.january)
        2 -> stringResource(Res.string.february)
        3 -> stringResource(Res.string.march)
        4 -> stringResource(Res.string.april)
        5 -> stringResource(Res.string.may)
        6 -> stringResource(Res.string.june)
        7 -> stringResource(Res.string.july)
        8 -> stringResource(Res.string.august)
        9 -> stringResource(Res.string.september)
        10 -> stringResource(Res.string.october)
        11 -> stringResource(Res.string.november)
        12 -> stringResource(Res.string.december)
        else -> monthNumber.toString()
    }
}

