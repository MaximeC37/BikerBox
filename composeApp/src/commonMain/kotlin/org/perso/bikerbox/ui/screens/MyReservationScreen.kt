package org.perso.bikerbox.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.perso.bikerbox.data.models.Reservation
import org.perso.bikerbox.data.models.Resource
import org.perso.bikerbox.ui.viewmodel.ReservationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyReservationsScreen(
    viewModel: ReservationViewModel,
    onNavigateBack: () -> Unit
) {
    val reservationsState by viewModel.userReservations.collectAsState()
    val uiMessage by viewModel.uiMessage.collectAsState()

    uiMessage?.let { message ->
        LaunchedEffect(message) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearUiMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Reservations") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = {
            uiMessage?.let { message ->
                SnackbarHost(
                    hostState = remember { SnackbarHostState() }
                ) {
                    Snackbar(
                        action = {
                            TextButton(onClick = { viewModel.clearUiMessage() }) {
                                Text("OK")
                            }
                        }
                    ) {
                        Text(message)
                    }
                }
            }
        }
    ) { padding ->
        when (val state = reservationsState) {
            is Resource.Loading -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Loading your reservations...")
                }
            }
            is Resource.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Error: ${state.message}",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            is Resource.Success<*> -> {
                val reservations = when {
                    state.data is List<*> -> state.data
                    else -> emptyList<Any>()
                }

                if (reservations.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "You don't have any reservations yet",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(reservations.size) { index ->
                            val reservation = reservations[index] as Reservation
                            ReservationCard(
                                reservation = reservation,
                                onCancelReservation = { reservationId ->
                                    println(" UI: Cancellation requested for: $reservationId")
                                    viewModel.cancelReservation(reservationId)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReservationCard(
    reservation: Reservation,
    onCancelReservation: (String) -> Unit
) {
    //  NOUVEAU : État pour confirmer la suppression
    var showConfirmDialog by remember { mutableStateOf(false) }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Confirm Cancellation") },
            text = { Text("Are you sure you want to cancel this reservation?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirmDialog = false
                        onCancelReservation(reservation.id)
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Reservation information
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Locker #${reservation.lockerId}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                IconButton(
                    onClick = {
                        println(" Delete button clicked for: ${reservation.id}")
                        showConfirmDialog = true
                    }
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Cancel",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Size: ${reservation.size.name}",
                style = MaterialTheme.typography.bodyMedium
            )

            // Dates
            Text(
                text = "From: ${formatDateTime(reservation.startDate)}",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "To: ${formatDateTime(reservation.endDate)}",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Code: ${reservation.code}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Price: ${reservation.price.round(2)}€",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

// Extension to format dates
private fun formatDateTime(dateTime: kotlinx.datetime.LocalDateTime): String {
    return "${dateTime.dayOfMonth}/${dateTime.monthNumber}/${dateTime.year} - ${dateTime.hour}:${dateTime.minute.toString().padStart(2, '0')}"
}

// Reuse extension to round price
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