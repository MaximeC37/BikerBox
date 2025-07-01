package org.perso.bikerbox.ui.screens.DateSelection


import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import bikerbox.composeapp.generated.resources.Back
import bikerbox.composeapp.generated.resources.Cancel
import bikerbox.composeapp.generated.resources.Choose
import bikerbox.composeapp.generated.resources.Choose_a_date
import bikerbox.composeapp.generated.resources.Date_and_Time_Selection
import bikerbox.composeapp.generated.resources.End_Date
import bikerbox.composeapp.generated.resources.End_Time
import bikerbox.composeapp.generated.resources.OK
import bikerbox.composeapp.generated.resources.Res
import bikerbox.composeapp.generated.resources.Start_Date
import bikerbox.composeapp.generated.resources.Start_Time
import kotlinx.datetime.*
import org.jetbrains.compose.resources.stringResource
import org.perso.bikerbox.data.models.LockerSize
import org.perso.bikerbox.data.models.basePricePerDay
import org.perso.bikerbox.data.services.PricingService
import kotlin.math.round

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

    if (calculatedPrice > 0) {
        Text(
            text = "Estimated price: ${calculatedPrice.round(2)}€",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }

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
                title = { Text(stringResource(Res.string.Date_and_Time_Selection)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.Back)
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
                text = "Locker: $lockerName - Size: ${selectedSize.name}",
                style = MaterialTheme.typography.titleMedium
            )

            // For start date
            DateSelectionComponent(
                title = stringResource(Res.string.Start_Date),
                selectedDate = selectedStartDate,
                onDateSelected = { selectedStartDate = it }
            )

            // For end date
            DateSelectionComponent(
                title = stringResource(Res.string.End_Date),
                selectedDate = selectedEndDate,
                onDateSelected = { selectedEndDate = it }
            )

            // Start and end times
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TimeSelectionComponent(
                    title = stringResource(Res.string.Start_Time),
                    selectedTime = startTime,
                    onTimeSelected = { startTime = it }
                )

                TimeSelectionComponent(
                    title = stringResource(Res.string.End_Time),
                    selectedTime = endTime,
                    onTimeSelected = { endTime = it }
                )
            }

            // Calculated price
            Text(
                text = "Estimated price: ${calculatedPrice.round(2)}€",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 16.dp)
            )

            // Confirmation button
            Button(
                onClick = {
                    selectedStartDate?.let { start ->
                        selectedEndDate?.let { end ->
                            // Convert LocalDate to LocalDateTime
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
                Text(stringResource(Res.string.OK))
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
                } ?: stringResource(Res.string.Choose_a_date),
                maxLines = 1
            )
        }

        if (showDatePicker) {
            // We can use null for initialization and let DatePicker use the current date
            val initialDateMillis = selectedDate?.let {
                // Get current timestamp
                val now = Clock.System.now().toEpochMilliseconds()

                // Calculate approximately the difference in days
                val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
                val daysDiff = (it.dayOfMonth - today.dayOfMonth) +
                        30 * (it.monthNumber - today.monthNumber) +
                        365 * (it.year - today.year)

                // Adjust timestamp (approximate, but sufficient for initialization)
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
                        Text(stringResource(Res.string.OK))
                    }
                },
                dismissButton = {
                    Button(onClick = { showDatePicker = false }) {
                        Text(stringResource(Res.string.Cancel))
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
        // Log error for debugging
        println("Date/time conversion error: ${e.message}")
        // Return default value or throw exception
        throw e
    }
}

// Extension to round a Double to n decimal places
private fun Double.round(decimals: Int): String {
    var multiplier = 1.0
    repeat(decimals) { multiplier *= 10 }
    val roundedValue = round(this * multiplier) / multiplier

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
                    text = selectedTime ?: stringResource(Res.string.Choose),
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

