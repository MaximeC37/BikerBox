package org.perso.bikerbox.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.*

@Composable
fun DatePicker(
    initialDate: LocalDate,
    minDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    var currentYear by remember { mutableStateOf(initialDate.year) }
    var currentMonth by remember { mutableStateOf(initialDate.month) }

    Column(modifier = Modifier.padding(16.dp)) {
        // Affichage du mois et de l'année avec les boutons de navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                if (currentMonth == Month.JANUARY) {
                    currentYear--
                    currentMonth = Month.DECEMBER
                } else {
                    currentMonth = (currentMonth.number - 1).toMonth()
                }
            }) {
                Text("<")
            }

            Text(
                text = "${currentMonth.name} $currentYear",
                style = MaterialTheme.typography.titleMedium
            )

            IconButton(onClick = {
                if (currentMonth == Month.DECEMBER) {
                    currentYear++
                    currentMonth = Month.JANUARY
                } else {
                    currentMonth =  (currentMonth.number - 1).toMonth()
                }
            }) {
                Text(">")
            }
        }

        // Affichage des jours de la semaine
        Row(modifier = Modifier.fillMaxWidth()) {
            val daysOfWeek = listOf("Lu", "Ma", "Me", "Je", "Ve", "Sa", "Di")
            daysOfWeek.forEach { day ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(day, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        // Grille des jours du mois
        val daysInMonth = getDaysInMonth(currentYear, currentMonth)
        val firstDayOfMonth = LocalDate(currentYear, currentMonth, 1).dayOfWeek.isoDayNumber // 1 (Lundi) à 7 (Dimanche)

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.height(300.dp)
        ) {
            // Cases vides pour l'alignement du premier jour du mois
            items((1 until firstDayOfMonth).toList()) { _ ->
                Box(modifier = Modifier.size(40.dp))
            }

            // Jours du mois
            items((1..daysInMonth).toList()) { day ->
                val date = LocalDate(currentYear, currentMonth, day)
                val isSelectable = date >= minDate

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .padding(4.dp)
                        .background(
                            if (date == initialDate) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surface
                        )
                        .clickable(enabled = isSelectable) {
                            onDateSelected(date)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = day.toString(),
                        color = when {
                            date == initialDate -> MaterialTheme.colorScheme.onPrimary
                            !isSelectable -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
            }
        }
    }
}

private fun getDaysInMonth(year: Int, month: Month): Int {
    return when (month) {
        Month.FEBRUARY -> if (isLeapYear(year)) 29 else 28
        Month.APRIL, Month.JUNE, Month.SEPTEMBER, Month.NOVEMBER -> 30
        else -> 31
    }
}

private fun isLeapYear(year: Int): Boolean {
    return year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)
}

fun Int.toMonth(): Month {
    return when (this) {
        1 -> Month.JANUARY
        2 -> Month.FEBRUARY
        3 -> Month.MARCH
        4 -> Month.APRIL
        5 -> Month.MAY
        6 -> Month.JUNE
        7 -> Month.JULY
        8 -> Month.AUGUST
        9 -> Month.SEPTEMBER
        10 -> Month.OCTOBER
        11 -> Month.NOVEMBER
        12 -> Month.DECEMBER
        else -> throw IllegalArgumentException("Invalid month number: $this")
    }
}



