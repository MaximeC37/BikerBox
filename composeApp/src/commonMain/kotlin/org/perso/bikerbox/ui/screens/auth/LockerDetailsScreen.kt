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
import bikerbox.composeapp.generated.resources.*
import bikerbox.composeapp.generated.resources.Size_locker_large
import bikerbox.composeapp.generated.resources.Size_locker_medium
import bikerbox.composeapp.generated.resources.Size_locker_small
import org.jetbrains.compose.resources.stringResource
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
    val availableLockers by viewModel.availableLockers.collectAsState()

    val locker = availableLockers.find { it.id == lockerId }

    LaunchedEffect(locker) {
        if (locker == null) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(locker?.name ?: stringResource(Res.string.Locker_Details)) },
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
                                contentDescription = "Location",
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

                Text(
                    text = stringResource(Res.string.Select_locker_size),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 16.dp)
                )

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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(Res.string.Locker_not_found),
                    style = MaterialTheme.typography.headlineMedium
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = onNavigateBack) {
                    Text(stringResource(Res.string.Back))
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
                    LockerSize.SMALL -> "${stringResource(Res.string.Small)}: ${stringResource(Res.string.one_helmet_and_one_coat)}"
                    LockerSize.MEDIUM -> "${stringResource(Res.string.Medium)}: ${stringResource(Res.string.double_helmet_and_double_coat)}"
                    LockerSize.LARGE -> "${stringResource(Res.string.Large)}: ${stringResource(Res.string.double_helmet_and_double_coat)}"
                },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${stringResource(Res.string.Size)}: ${
                    when (size) {
                        LockerSize.SMALL -> stringResource(Res.string.Size_locker_small)
                        LockerSize.MEDIUM -> stringResource(Res.string.Size_locker_medium)
                        LockerSize.LARGE -> stringResource(Res.string.Size_locker_large)
                    }
                }",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${stringResource(Res.string.Price)}: ${
                    when (size) {
                        LockerSize.SMALL -> stringResource(Res.string.Price_small)
                        LockerSize.MEDIUM -> stringResource(Res.string.Price_medium)
                        LockerSize.LARGE -> stringResource(Res.string.Price_large)
                    }
                }",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "$availableCount ${stringResource(Res.string.Available).lowercase()}${if (availableCount > 1) "s" else ""}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
