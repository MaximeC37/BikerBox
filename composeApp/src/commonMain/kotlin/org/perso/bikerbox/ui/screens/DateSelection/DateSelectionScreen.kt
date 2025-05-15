package org.perso.bikerbox.ui.screens.DateSelection


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.*
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import org.perso.bikerbox.data.models.LockerSize
import org.perso.bikerbox.data.models.basePricePerDay
import org.perso.bikerbox.data.services.PricingService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateSelectionScreen(
    lockerId: String,
    lockerName: String,
    selectedSize: LockerSize,
    onConfirm: (startDate: LocalDateTime, endDate: LocalDateTime, price: Double) -> Unit,
    onBack: () -> Unit
) {
    var selectedStartDate by remember { mutableStateOf<LocalDate?>(null) }
    var selectedEndDate by remember { mutableStateOf<LocalDate?>(null) }
    var startTime by remember { mutableStateOf<String?>(null) }
    var endTime by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf("") }


    val calculatedPrice = if (selectedStartDate != null && startTime != null && selectedEndDate != null && endTime != null) {
        val startDateTime = combineDateTime(selectedStartDate!!, startTime!!)
        val endDateTime = combineDateTime(selectedEndDate!!, endTime!!)

        // Calculer la différence en heures
        val hoursDiff = PricingService.calculateTotalHoursBetween(startDateTime, endDateTime)

        val price = if (endDateTime <= startDateTime) {
            0.0
        } else {
            PricingService.calculatePrice(selectedSize, startDateTime, endDateTime)
        }

        if (price <= 0.0) {
            selectedSize.basePricePerDay
        } else {
            price
        }
    } else {
        0.0
    }

    // Afficher le prix calculé
    if (calculatedPrice > 0) {
        Text(
            text = "Prix estimé: ${calculatedPrice.round(2)}€",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }

    // Afficher le message d'erreur s'il y en a un
    if (errorMessage.isNotEmpty()) {
        Text(
            text = errorMessage,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium
        )
    }




    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sélection date et heure") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Retour"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "Locker: $lockerName - Taille: ${selectedSize.name}",
                style = MaterialTheme.typography.titleMedium
            )

            // Pour la date de début
            DateSelectionComponent(
                title = "Date de début",
                selectedDate = selectedStartDate,
                onDateSelected = { selectedStartDate = it }
            )

            // Pour la date de fin
            DateSelectionComponent(
                title = "Date de fin",
                selectedDate = selectedEndDate,
                onDateSelected = { selectedEndDate = it }
            )

            // Heures de début et fin
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TimeSelectionComponent(
                    title = "Heure de début",
                    selectedTime = startTime,
                    onTimeSelected = { startTime = it }
                )

                TimeSelectionComponent(
                    title = "Heure de fin",
                    selectedTime = endTime,
                    onTimeSelected = { endTime = it }
                )
            }

            // Prix calculé
            Text(
                text = "Prix estimé: ${calculatedPrice.round(2)}€",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 16.dp)
            )

            // Bouton de confirmation
            Button(
                onClick = {
                    selectedStartDate?.let { start ->
                        selectedEndDate?.let { end ->
                            // Conversion de LocalDate en LocalDateTime
                            val startDateTime = LocalDateTime(
                                start.year,
                                start.monthNumber,
                                start.dayOfMonth,
                                startTime?.split(":")?.get(0)?.toIntOrNull() ?: 0,
                                startTime?.split(":")?.get(1)?.toIntOrNull() ?: 0
                            )

                            val endDateTime = LocalDateTime(
                                end.year,
                                end.monthNumber,
                                end.dayOfMonth,
                                endTime?.split(":")?.get(0)?.toIntOrNull() ?: 0,
                                endTime?.split(":")?.get(1)?.toIntOrNull() ?: 0
                            )

                            onConfirm(startDateTime, endDateTime, calculatedPrice)
                        }
                    }
                },
                enabled = selectedStartDate != null && selectedEndDate != null &&
                        startTime != null && endTime != null,
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .padding(top = 16.dp)
            ) {
                Text("Confirmer")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateSelectionComponent(
    title: String,
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        OutlinedButton(
            onClick = { showDatePicker = true },
            modifier = Modifier.fillMaxWidth(0.7f)
        ) {
            Text(
                text = selectedDate?.let {
                    "${it.dayOfMonth}/${it.monthNumber}/${it.year}"
                } ?: "Choisir une date",
                maxLines = 1
            )
        }

        if (showDatePicker) {
            // Nous pouvons utiliser null pour l'initialisation et laisser DatePicker utiliser la date actuelle
            val initialDateMillis = selectedDate?.let {
                // Obtenir l'horodatage actuel
                val now = Clock.System.now().toEpochMilliseconds()

                // Calculer approximativement la différence en jours
                val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
                val daysDiff = (it.dayOfMonth - today.dayOfMonth) +
                        30 * (it.monthNumber - today.monthNumber) +
                        365 * (it.year - today.year)

                // Ajuster l'horodatage (approximatif, mais suffisant pour l'initialisation)
                now + (daysDiff * 24 * 60 * 60 * 1000L)
            }

            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = initialDateMillis
            )

            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    Button(onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val instant = Instant.fromEpochMilliseconds(millis)
                            val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
                            val date = localDateTime.date
                            onDateSelected(date)
                        }
                        showDatePicker = false
                    }) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    Button(onClick = { showDatePicker = false }) {
                        Text("Annuler")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }
}
private fun combineDateTime(date: LocalDate, timeString: String): LocalDateTime {
    try {
        val timeParts = timeString.split(":")
        val hour = timeParts[0].toInt()
        val minute = timeParts[1].toInt()

        return LocalDateTime(
            date.year, date.monthNumber, date.dayOfMonth,
            hour, minute, 0, 0
        )
    } catch (e: Exception) {
        // Log l'erreur pour débogage
        println("Erreur de conversion date/heure: ${e.message}")
        // Renvoyer une valeur par défaut ou lancer l'exception
        throw e
    }
}
// Extension pour arrondir un Double à n décimales
private fun Double.round(decimals: Int): String {
    var multiplier = 1.0
    repeat(decimals) { multiplier *= 10 }
    val roundedValue = kotlin.math.round(this * multiplier) / multiplier

    return if (decimals > 0) {
        val result = roundedValue.toString()
        if (result.contains('.')) {
            val parts = result.split('.')
            val decimalPart = parts[1].padEnd(decimals, '0').take(decimals)
            "${parts[0]}.$decimalPart"
        } else {
            "$result.${"0".repeat(decimals)}"
        }
    } else {
        roundedValue.toInt().toString()
    }
}

@Composable
fun TimeSelectionComponent(
    title: String,
    selectedTime: String?,
    onTimeSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val timeOptions = listOf(
        "08:00", "09:00", "10:00", "11:00",
        "12:00", "13:00", "14:00", "15:00",
        "16:00", "17:00", "18:00", "19:00"
    )

    Column(
        modifier = Modifier
            .width(160.dp)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Box {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = selectedTime ?: "Choisir",
                    maxLines = 1
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.width(160.dp)
            ) {
                timeOptions.forEach { time ->
                    DropdownMenuItem(
                        onClick = {
                            onTimeSelected(time)
                            expanded = false
                        },
                        text = { Text(time) }
                    )
                }
            }
        }
    }
}

