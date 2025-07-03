package org.perso.bikerbox.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import bikerbox.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.perso.bikerbox.data.models.Reservation
import org.perso.bikerbox.data.models.Resource
import org.perso.bikerbox.ui.viewmodel.ReservationViewModel
import org.perso.bikerbox.utils.formatDecimal

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
                title = { Text(stringResource(Res.string.My_Reservations)) },
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
                                Text(stringResource(Res.string.OK))
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
                    Text(stringResource(Res.string.Loading_your_reservations))
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
                        text = "${stringResource(Res.string.Error)}: ${state.message}",
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
                            text = stringResource(Res.string.You_dont_have_reservations_yet),
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
    var showConfirmDialog by remember { mutableStateOf(false) }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = {showConfirmDialog = false },
            title = { Text(stringResource(Res.string.Confirm_Cancellation)) },
            text = { Text(stringResource(Res.string.Are_you_sure_cancel_reservation)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onCancelReservation(reservation.id)
                    }
                ) {
                    Text(stringResource(Res.string.Confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text(stringResource(Res.string.Cancel))
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${stringResource(Res.string.Locker)} #${reservation.lockerId}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                IconButton(
                    onClick = {showConfirmDialog = true }
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
                text = "${stringResource(Res.string.Size)}: ${reservation.size.name}",
                style = MaterialTheme.typography.bodyMedium
            )

            // Dates
            Text(
                text = "${stringResource(Res.string.From)}: ${formatDateTime(reservation.startDate)}",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "${stringResource(Res.string.To)}: ${formatDateTime(reservation.endDate)}",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${stringResource(Res.string.Code)}: ${reservation.code}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "${stringResource(Res.string.Price)}: ${reservation.price.formatDecimal(2)}€",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

private fun formatDateTime(dateTime: kotlinx.datetime.LocalDateTime): String {
    return "${dateTime.dayOfMonth}/${dateTime.monthNumber}/${dateTime.year} - ${dateTime.hour}:${dateTime.minute.toString().padStart(2, '0')}"
}