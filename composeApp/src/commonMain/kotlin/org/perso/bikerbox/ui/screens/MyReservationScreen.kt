package org.perso.bikerbox.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mes réservations") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
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
                    Text("Chargement de vos réservations...")
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
                        text = "Erreur: ${state.message}",
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
                            text = "Vous n'avez pas encore de réservations",
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
                                onCancelReservation = { viewModel.cancelReservation(it) }
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Informations sur la réservation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Casier #${reservation.lockerId}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                IconButton(onClick = { onCancelReservation(reservation.id) }) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Annuler",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Taille du casier
            Text(
                text = "Taille: ${reservation.size.name}",
                style = MaterialTheme.typography.bodyMedium
            )

            // Dates
            Text(
                text = "Du: ${formatDateTime(reservation.startDate)}",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "Au: ${formatDateTime(reservation.endDate)}",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Code et prix
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
                    text = "Prix: ${reservation.price.round(2)}€",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

// Extension pour formater les dates
private fun formatDateTime(dateTime: kotlinx.datetime.LocalDateTime): String {
    return "${dateTime.dayOfMonth}/${dateTime.monthNumber}/${dateTime.year} - ${dateTime.hour}:${dateTime.minute.toString().padStart(2, '0')}"
}

// Réutilisation de l'extension pour arrondir le prix
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
