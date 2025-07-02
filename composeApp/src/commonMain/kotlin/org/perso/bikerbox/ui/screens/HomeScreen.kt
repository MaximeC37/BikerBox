package org.perso.bikerbox.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import bikerbox.composeapp.generated.resources.*
import formatToString
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import org.perso.bikerbox.data.models.Locker
import org.perso.bikerbox.data.models.Resource
import org.perso.bikerbox.ui.viewmodel.AuthViewModel
import org.perso.bikerbox.ui.viewmodel.ReservationState
import org.perso.bikerbox.ui.viewmodel.ReservationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: ReservationViewModel,
    authViewModel: AuthViewModel,
    onSelectLocker: (String) -> Unit,
    onNavigateToReservations: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onSignOut: () -> Unit
) {
    val authState by authViewModel.authState.collectAsState()
    val currentUser = if (authState is Resource.Success) (authState as Resource.Success).data else null
    val state by viewModel.state.collectAsState()
    val availableLockers by viewModel.availableLockers.collectAsState()

    availableLockers.forEachIndexed { index, locker ->
        android.util.Log.d("HomeScreen", "Locker $index in availableLockers: ${locker.name}, ${locker.availableSizes}")
    }

    var showMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.BikerBox)) },
                actions = {
                    IconButton(onClick = { onNavigateToReservations() }) {
                        Icon(Icons.AutoMirrored.Filled.List, contentDescription = "My réservations")
                    }
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Profil")

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { }
                        ) {
                            currentUser?.let { user ->
                                Text(
                                    text = user.email,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }

                            DropdownMenuItem(
                                onClick = {
                                    onNavigateToProfile()
                                },
                                text = { Text(stringResource(Res.string.My_Profil)) }
                            )

                            DropdownMenuItem(
                                onClick = {
                                    onSignOut()
                                },
                                text = {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                                        Text(stringResource(Res.string.Log_out))
                                    }
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val currentState = state) {
                is ReservationState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Loading...")

                            var showRefreshButton by remember { mutableStateOf(false) }
                            LaunchedEffect(Unit) {
                                delay(10000)
                                showRefreshButton = true
                            }

                            AnimatedVisibility(
                                visible = showRefreshButton,
                                enter = fadeIn(),
                                exit = fadeOut()
                            ) {
                                Column {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        stringResource(Res.string.Loading_longer),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(onClick = { viewModel.loadUserReservations() }) {
                                        Text(stringResource(Res.string.Refresh))
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedButton(onClick = { viewModel.reset() }) {
                                        Text(stringResource(Res.string.Back_to_home))
                                    }
                                }
                            }
                        }
                    }
                }

                is ReservationState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("${stringResource(Res.string.Error)}: ${currentState.message}")
                    }
                }

                is ReservationState.LockerSelection -> {
                    currentState.lockers.forEachIndexed { index, locker ->
                        android.util.Log.d("HomeScreen", "Locker $index in the State: ${locker.name}, size: ${locker.availableSizes}")
                    }

                    if (currentState.lockers.isNotEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = stringResource(Res.string.Lockers_available),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(currentState.lockers) { locker ->
                                    LockerCard(
                                        locker = locker,
                                        onClick = {
                                            viewModel.selectLocker(locker)
                                            onSelectLocker(locker.id)
                                        }
                                    )
                                }
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(stringResource(Res.string.No_lockers_available))
                        }
                    }
                }
                is ReservationState.Success -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = stringResource(Res.string.Reservation_confirmed),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "${stringResource(Res.string.Your_locker)} ${currentState.reservation.lockerName} ${stringResource(Res.string.Is_reserved)}",
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Du: ${currentState.reservation.startDate.formatToString()}",
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Text(
                                text = "Au: ${currentState.reservation.endDate.formatToString()}",
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Card(
                                modifier = Modifier.padding(vertical = 8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = stringResource(Res.string.Access_code),
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                    Text(
                                        text = currentState.reservation.code,
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = { onNavigateToReservations() },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(stringResource(Res.string.View_my_reservations))
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedButton(
                                onClick = { viewModel.reset() },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(stringResource(Res.string.Back_to_home))
                            }
                        }
                    }
                }
                else -> {
                    if (availableLockers.isNotEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = stringResource(Res.string.Lockers_available),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(availableLockers) { locker ->
                                    LockerCard(
                                        locker = locker,
                                        onClick = {
                                            viewModel.selectLocker(locker)
                                            onSelectLocker(locker.id)
                                        }
                                    )
                                }
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(stringResource(Res.string.No_lockers_available))
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun LockerCard(locker: Locker, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = locker.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = locker.location,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
