package org.perso.bikerbox.utils

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

/**
 * Ajoute un nombre de jours à la date actuelle
 */
fun LocalDate.plusDays(days: Int): LocalDate {
    return this.plus(DatePeriod(days = days))
}

/**
 * Calcule la différence en jours entre deux dates
 */
fun LocalDate.daysBetween(other: LocalDate): Int {
    var days = 0
    var currentDate = this

    // Si this est après other, inverser
    if (this > other) {
        return other.daysBetween(this)
    }

    while (currentDate < other) {
        currentDate = currentDate.plusDays(1)
        days++
    }

    return days
}

/**
 * Formatte la date en format lisible (ex: "15 janvier 2023")
 */
fun LocalDate.toFormattedString(): String {
    val monthNames = listOf(
        "janvier", "février", "mars", "avril", "mai", "juin",
        "juillet", "août", "septembre", "octobre", "novembre", "décembre"
    )

    return "$dayOfMonth ${monthNames[monthNumber - 1]} $year"
}
