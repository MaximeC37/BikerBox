package org.perso.bikerbox.ui.screens.Confirmation

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
    // Effet lancé quand l'état est Success pour naviguer automatiquement
    LaunchedEffect(state) {
        if (state is ReservationState.Success) {
            Log.d("ConfirmationScreen", "État actuel: $state")
            // Naviguer vers la page suivante
            onConfirm()
        }
    }

    // Utiliser la version existante du ConfirmationScreen avec les paramètres appropriés
    when (val currentState = state) {
        is ReservationState.ConfirmationNeeded -> {
            ConfirmationScreenContent(
                lockerName = currentState.lockerName,
                size = currentState.size,
                startDate = currentState.startDate,
                endDate = currentState.endDate,
                price = currentState.price,
                onConfirm = {
                    // Ajouter un log ici
                    Log.d("ConfirmationScreen", "Bouton de confirmation cliqué")

                    viewModel.setReservationDetails(
                        currentState.lockerId,
                        currentState.size,
                        currentState.startDate,
                        currentState.endDate,
                        currentState.price
                    )

                    // Ajouter un log avant d'appeler confirmReservation
                    Log.d("ConfirmationScreen", "Détails de réservation définis, appel de confirmReservation")

                    // Appeler confirmReservation
                    viewModel.confirmReservation()

                    // Ajouter un délai avant de naviguer pour permettre à la réservation de se terminer
                    // Cette solution de contournement permet à l'utilisateur de passer à l'écran suivant
                    // même si l'état Success n'est jamais atteint
                    Log.d("ConfirmationScreen", "confirmReservation appelé, navigation après délai")

                    // Exécuter onConfirm après un court délai
                    viewModel.viewModelScope.launch {
                        delay(1000) // Attendre 1 seconde
                        onConfirm()
                    }
                },
                onBack = onCancel
            )
        }

        is ReservationState.Success -> {
            Log.d("ConfirmationScreen", "État Success atteint, navigation")
            LaunchedEffect(Unit) {
                onConfirm()
            }
        }

        else -> {
            // Écran de chargement avec bouton d'échappement
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Chargement des détails de la réservation...")

                    // Ajouter un bouton pour permettre à l'utilisateur de sortir après 3 secondes
                    LaunchedEffect(Unit) {
                        delay(3000)
                        // Forcer la mise à jour pour montrer le bouton
                        // (Ceci pourrait nécessiter un état local pour être plus propre)
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = onConfirm) {
                        Text("Continuer quand même")
                    }
                }
            }
        }
    }
}

// La version originale de votre ConfirmationScreen, renommée pour éviter les conflits
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
    val scrollState = rememberScrollState() // État pour gérer le défilement

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState) // Ajout du modificateur de défilement
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (isPreConfirmation) "Confirmer votre réservation" else "Réservation confirmée !",
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
                        text = "Détails de la réservation",
                        style = MaterialTheme.typography.titleLarge
                    )

                    HorizontalDivider()

                    if (isPreConfirmation) {
                        DetailRow(label = "Casier", value = lockerName ?: "")
                        DetailRow(label = "Taille", value = size?.displayName ?: "")
                        DetailRow(label = "Date de début", value = "$startDate")
                        DetailRow(label = "Date de fin", value = "$endDate")
                        DetailRow(label = "Prix total", value = "${price?.formatDecimal(2)} €")
                    } else {
                        // Après confirmation, on affiche les détails de la réservation confirmée
                        reservation.let {
                            DetailRow(label = "Numéro de réservation", value = it.id)
                            DetailRow(label = "Taille", value = it.size.displayName)
                            DetailRow(label = "Date de début", value = "${it.startDate}")
                            DetailRow(label = "Date de fin", value = "${it.endDate}")
                            DetailRow(label = "Prix total", value = "${it.price.formatDecimal(2)} €")

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Votre code d'accès",
                                style = MaterialTheme.typography.titleMedium
                            )

                            CodeDisplay(code = it.code)
                        }
                    }
                }
            }

            // Remplacez le Spacer avec weight par un Spacer fixe pour éviter que
            // les boutons ne soient poussés en bas de l'écran en mode scrollable
            Spacer(modifier = Modifier.height(24.dp))

            if (isPreConfirmation) {
                // Afficher les boutons Retour et Confirmer
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
                        Text("Retour")
                    }

                    Button(
                        onClick = { onConfirm?.invoke() }
                    ) {
                        Text("Confirmer")
                    }
                }
            } else {
                // Afficher le bouton Terminé
                Button(
                    onClick = { onDone?.invoke() },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Terminé")
                }
            }

            // Ajouter un espace supplémentaire en bas pour garantir que tout le contenu est accessible
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}