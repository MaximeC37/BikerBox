package org.perso.bikerbox.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.perso.bikerbox.data.models.LockerSize
import org.perso.bikerbox.ui.viewmodel.ReservationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LockerDetailsScreen(
    lockerId: String,
    viewModel: ReservationViewModel,
    onNavigateBack: () -> Unit,
    onSelectSize: (LockerSize) -> Unit
) {
    // Retrieve Lockers from the ViewModel
    val availableLockers by viewModel.availableLockers.collectAsState()

    // Trouver le casier correspondant à l'ID
    val locker = availableLockers.find { it.id == lockerId }

    // Effet pour gérer le cas où le casier n'est pas trouvé
    LaunchedEffect(locker) {
        if (locker == null) {
            // Si le casier n'est pas trouvé, revenir en arrière
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(locker?.name ?: "Détails du casier") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { paddingValues ->
        locker?.let { currentLocker ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Top
            ) {
                // Informations sur le casier
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = currentLocker.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = "Emplacement",
                                tint = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = currentLocker.location,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }

                // Section des tailles disponibles
                Text(
                    text = "Sélectionnez une taille de casier",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                // Liste des tailles disponibles
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    currentLocker.availableSizes.forEach { size ->
                        val count = currentLocker.availableCount[size] ?: 0

                        if (count > 0) {
                            SizeCard(
                                size = size,
                                availableCount = count,
                                onClick = { onSelectSize(size) }
                            )
                        }
                    }
                }
            }
        } ?: run {
            // Affichage d'erreur si le casier n'est pas trouvé
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Casier non trouvé",
                    style = MaterialTheme.typography.headlineMedium
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = onNavigateBack) {
                    Text("Retour")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SizeCard(
    size: LockerSize,
    availableCount: Int,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = when (size) {
                    LockerSize.SINGLE -> "Petit (1 casque + 1 manteau)"
                    LockerSize.DOUBLE -> "Grand (2 casques + 2 manteaux)"
                },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Dimensions: ${
                    when (size) {
                        LockerSize.SINGLE -> "50cm x 50cm x 80cm"
                        LockerSize.DOUBLE -> "100cm x 100cm x 80cm"
                    }
                }",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Prix: ${
                    when (size) {
                        LockerSize.SINGLE -> "6€/journée"
                        LockerSize.DOUBLE -> "10€/journée"
                    }
                }",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "$availableCount disponible${if (availableCount > 1) "s" else ""}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
