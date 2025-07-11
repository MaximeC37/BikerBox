package org.perso.bikerbox.ui.screens

import android.Manifest
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import bikerbox.composeapp.generated.resources.*
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
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
    val selectedLockerForDetails by viewModel.selectedLockerDetails.collectAsState()

    var showMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.BikerBox)) },
                actions = {
                    IconButton(onClick = { onNavigateToReservations() }) {
                        Icon(Icons.AutoMirrored.Filled.List, contentDescription = stringResource(Res.string.My_Reservations))
                    }
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.AccountCircle, contentDescription = "Profil")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            currentUser?.email?.let {
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                            DropdownMenuItem(onClick = { onNavigateToProfile(); showMenu = false }, text = { Text(stringResource(Res.string.My_Profil)) })
                            DropdownMenuItem(
                                onClick = { onSignOut(); showMenu = false },
                                text = {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
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
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val currentState = state) {
                is ReservationState.Loading -> LoadingState()
                is ReservationState.Error -> ErrorState(message = currentState.message)
                is ReservationState.LockerSelection -> {
                    LockerSelectionTabs(lockers = currentState.lockers, viewModel = viewModel)
                }
                else -> LoadingState()
            }
        }
    }

    selectedLockerForDetails?.let { locker ->
        LockerDetailsBottomSheet(
            locker = locker,
            onDismiss = { viewModel.dismissLockerDetails() },
            onReserveClick = {
                viewModel.startReservationFromDetails()
                onSelectLocker(locker.id)
            }
        )
    }
}

@Composable
private fun LockerSelectionTabs(
    lockers: List<Locker>,
    viewModel: ReservationViewModel
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf(stringResource(Res.string.List), stringResource(Res.string.Map))

    Column {
        TabRow(selectedTabIndex = selectedTabIndex) {
            tabs.forEachIndexed { index, title ->
                Tab(selected = selectedTabIndex == index, onClick = { selectedTabIndex = index }, text = { Text(title) })
            }
        }
        when (selectedTabIndex) {
            0 -> LockerList(lockers = lockers, viewModel = viewModel)
            1 -> LockerMap(lockers = lockers, viewModel = viewModel)
        }
    }
}

@Composable
private fun LockerList(
    lockers: List<Locker>,
    viewModel: ReservationViewModel
) {
    if (lockers.isNotEmpty()) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text(
                text = stringResource(Res.string.Lockers_available),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            LazyVerticalGrid(columns = GridCells.Fixed(2), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(lockers) { locker ->
                    LockerCard(
                        locker = locker,
                        onClick = {
                            viewModel.selectLocker(locker)
                        }
                    )
                }
            }
        }
    } else {
        EmptyState()
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun LockerMap(
    lockers: List<Locker>,
    viewModel: ReservationViewModel
) {
    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    if (locationPermissionState.status.isGranted) {
        LaunchedEffect(Unit) { viewModel.startLocationUpdates() }
    }

    if (locationPermissionState.status.isGranted) {
        val userLocation by viewModel.userLocation.collectAsState()
        val cameraPositionState = rememberCameraPositionState { position = CameraPosition.fromLatLngZoom(LatLng(48.8566, 2.3522), 10f) }
        var hasCenteredOnUser by remember { mutableStateOf(false) }

        LaunchedEffect(userLocation) {
            if (userLocation != null && !hasCenteredOnUser) {
                val userLatLng = LatLng(userLocation!!.latitude, userLocation!!.longitude)
                cameraPositionState.animate(update = CameraUpdateFactory.newLatLngZoom(userLatLng, 15f), durationMs = 1500)
                hasCenteredOnUser = true
            }
        }

        val mapProperties = MapProperties(isMyLocationEnabled = true)

        GoogleMap(modifier = Modifier.fillMaxSize(), cameraPositionState = cameraPositionState, properties = mapProperties) {
            lockers.forEach { locker ->
                Marker(
                    state = rememberMarkerState(position = LatLng(locker.latitude, locker.longitude)),
                    title = locker.name,
                    snippet = "Cliquez pour voir les détails",
                    onClick = {
                        viewModel.selectLocker(locker)
                        true
                    }
                )
            }
        }
    } else {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(stringResource(Res.string.Location_permission_required))
            Spacer(modifier = Modifier.height(8.dp))
            Text(stringResource(Res.string.Location_permission_explanation))
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { locationPermissionState.launchPermissionRequest() }) {
                Text(stringResource(Res.string.Grant_permission))
            }
        }
    }
}

@Composable
private fun LoadingState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Loading...")
        }
    }
}

@Composable
private fun ErrorState(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("${stringResource(Res.string.Error)}: $message")
    }
}

@Composable
private fun EmptyState() {
    Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        Text(text = stringResource(Res.string.No_lockers_available), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun LockerCard(locker: Locker, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = locker.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = locker.location, style = MaterialTheme.typography.bodyMedium)
        }
    }
}