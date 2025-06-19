package org.perso.bikerbox.ui.screens.Confirmation

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDateTime
import org.perso.bikerbox.data.models.LockerSize
import org.perso.bikerbox.data.models.Reservation
import org.perso.bikerbox.data.models.displayName
import org.perso.bikerbox.ui.components.CodeDisplay
import org.perso.bikerbox.ui.components.DetailRow
import org.perso.bikerbox.ui.viewmodel.ReservationState
import org.perso.bikerbox.utils.formatDecimal
import org.perso.bikerbox.ui.viewmodel.ReservationViewModel

@Composable
fun ConfirmationScreen(
    viewModel: ReservationViewModel,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    when (val currentState = state) {
        is ReservationState.ConfirmationNeeded -> {
            ConfirmationScreenContent(
                lockerName = currentState.lockerName,
                size = currentState.size,
                startDate = currentState.startDate,
                endDate = currentState.endDate,
                price = currentState.price,
                onConfirm = {
                    Log.d("ConfirmationScreen", "Confirmation button clicked - Navigate to payment")
                    // Directly navigate to payment
                    onConfirm()
                },
                onBack = onCancel
            )
        }

        is ReservationState.Success -> {
            // Display confirmed reservation screen with code
            ConfirmationScreenContent(
                reservation = currentState.reservation,
                onDone = onConfirm
            )
        }

        is ReservationState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Loading reservation details...")
                }
            }
        }

        is ReservationState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Error: ${currentState.message}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onCancel) {
                        Text("Back")
                    }
                }
            }
        }

        else -> {
            // Default state
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Loading...")

                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = onCancel) {
                        Text("Back")
                    }
                }
            }
        }
    }
}

@Composable
fun ConfirmationScreenContent(
    reservation: Reservation? = null,
    lockerName: String? = null,
    size: LockerSize? = null,
    startDate: LocalDateTime? = null,
    endDate: LocalDateTime? = null,
    price: Double? = null,
    onConfirm: (() -> Unit)? = null,
    onBack: (() -> Unit)? = null,
    onDone: (() -> Unit)? = null
) {
    val isPreConfirmation = reservation == null
    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (isPreConfirmation) "Confirm your reservation" else "Reservation confirmed!",
                style = MaterialTheme.typography.headlineMedium
            )

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Reservation details",
                        style = MaterialTheme.typography.titleLarge
                    )

                    HorizontalDivider()

                    if (isPreConfirmation) {
                        DetailRow(label = "Locker", value = lockerName ?: "")
                        DetailRow(label = "Size", value = size?.displayName ?: "")
                        DetailRow(label = "Start date", value = "$startDate")
                        DetailRow(label = "End date", value = "$endDate")
                        DetailRow(label = "Total price", value = "${price?.formatDecimal(2)} €")
                    } else {
                        reservation?.let {
                            DetailRow(label = "Reservation number", value = it.id)
                            DetailRow(label = "Size", value = it.size.displayName)
                            DetailRow(label = "Start date", value = "${it.startDate}")
                            DetailRow(label = "End date", value = "${it.endDate}")
                            DetailRow(label = "Total price", value = "${it.price.formatDecimal(2)} €")

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Your access code",
                                style = MaterialTheme.typography.titleMedium
                            )

                            CodeDisplay(code = it.code)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (isPreConfirmation) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = { onBack?.invoke() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Text("Back")
                    }

                    Button(
                        onClick = { onConfirm?.invoke() }
                    ) {
                        Text("Proceed to payment")
                    }
                }
            } else {
                Button(
                    onClick = { onDone?.invoke() },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Done")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}