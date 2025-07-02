package org.perso.bikerbox.ui.screens.Confirmation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import bikerbox.composeapp.generated.resources.*
import kotlinx.datetime.LocalDateTime
import org.jetbrains.compose.resources.stringResource
import org.perso.bikerbox.data.models.LockerSize
import org.perso.bikerbox.data.models.Reservation
import org.perso.bikerbox.data.models.displayName
import org.perso.bikerbox.ui.components.CodeDisplay
import org.perso.bikerbox.ui.components.DetailRow
import org.perso.bikerbox.ui.viewmodel.ReservationState
import org.perso.bikerbox.ui.viewmodel.ReservationViewModel
import org.perso.bikerbox.utils.formatDecimal

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
                    onConfirm()
                },
                onBack = onCancel
            )
        }

        is ReservationState.Success -> {
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
                    Text(stringResource(Res.string.Loading_reservation_details))
                }
            }
        }

        is ReservationState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${stringResource(Res.string.Error)}: ${currentState.message}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onCancel) {
                        Text(stringResource(Res.string.Back))
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
                    Text(stringResource(Res.string.Loading))

                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = onCancel) {
                        Text(stringResource(Res.string.Back))
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
                text = if (isPreConfirmation) stringResource(Res.string.Confirm_your_reservation) else stringResource(Res.string.Reservation_confirmed),
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
                        text = stringResource(Res.string.Reservation_details),
                        style = MaterialTheme.typography.titleLarge
                    )

                    HorizontalDivider()

                    if (isPreConfirmation) {
                        DetailRow(label = stringResource(Res.string.Locker), value = lockerName ?: "")
                        DetailRow(label = stringResource(Res.string.Size), value = size?.displayName ?: "")
                        DetailRow(label = stringResource(Res.string.Start_Date), value = "$startDate")
                        DetailRow(label = stringResource(Res.string.End_Date), value = "$endDate")
                        DetailRow(label = stringResource(Res.string.Total_price), value = "${price?.formatDecimal(2)} €")
                    } else {
                        reservation?.let {
                            DetailRow(label = stringResource(Res.string.Reservation_number), value = it.id)
                            DetailRow(label = stringResource(Res.string.Size), value = it.size.displayName)
                            DetailRow(label = stringResource(Res.string.Start_Date), value = "${it.startDate}")
                            DetailRow(label = stringResource(Res.string.End_Date), value = "${it.endDate}")
                            DetailRow(label = stringResource(Res.string.Total_price), value = "${it.price.formatDecimal(2)} €")

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = stringResource(Res.string.Access_code),
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
                        Text(stringResource(Res.string.Back))
                    }

                    Button(
                        onClick = { onConfirm?.invoke() }
                    ) {
                        Text(stringResource(Res.string.Proceed_to_payment))
                    }
                }
            } else {
                Button(
                    onClick = { onDone?.invoke() },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(stringResource(Res.string.Done))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}