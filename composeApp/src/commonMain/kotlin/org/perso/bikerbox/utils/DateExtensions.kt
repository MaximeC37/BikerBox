package org.perso.bikerbox.utils

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.plus

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

fun LocalDate.toFormattedString(): String {
    val monthNames = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    return "$dayOfMonth ${monthNames[monthNumber - 1]} $year"
}
fun LocalDateTime.toFormattedString(): String {
    val monthNames = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    val formattedTime = String.format("%02d:%02d", hour, minute)
    return "$dayOfMonth ${monthNames[monthNumber - 1]} $year at $formattedTime"
}

fun LocalDateTime.toShortFormattedString(): String {
    val formattedTime = String.format("%02d:%02d", hour, minute)
    return "${String.format("%02d", dayOfMonth)}/${String.format("%02d", monthNumber)}/$year $formattedTime"
}

