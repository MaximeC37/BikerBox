package org.perso.bikerbox.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.os.Bundle
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import bikerbox.composeapp.generated.resources.*
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import org.jetbrains.compose.resources.stringResource
import org.maplibre.android.location.LocationComponent
import org.maplibre.android.location.LocationComponentActivationOptions
import org.maplibre.android.location.modes.CameraMode
import org.maplibre.android.location.modes.RenderMode
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.perso.bikerbox.data.models.Locker
import org.perso.bikerbox.data.models.Resource
import org.perso.bikerbox.ui.components.LockerDetailsBottomSheet
import org.perso.bikerbox.ui.viewmodel.AuthViewModel
import org.perso.bikerbox.ui.viewmodel.ReservationState
import org.perso.bikerbox.ui.viewmodel.ReservationViewModel
import androidx.core.graphics.createBitmap
import androidx.core.graphics.toColorInt

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
                            Icon(Icons.Default.AccountCircle, contentDescription = stringResource(Res.string.Profile))
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
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val userLocation by viewModel.userLocation.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.startLocationUpdates()
    }

    var selectedStyleIndex by remember { mutableIntStateOf(0) }
    val mapStyles = listOf(
        "Détaillé" to stringResource(Res.string.openstreetmap_style_url_detaille),
        "Standard" to stringResource(Res.string.openstreetmap_style_url_standard),
        "Sombre" to stringResource(Res.string.openstreetmap_style_url_sombre),
        "Clair" to stringResource(Res.string.openstreetmap_style_url_clair),
        "Minimal" to stringResource(Res.string.openstreetmap_style_url_minimal),
    )

    var showStyleSelector by remember { mutableStateOf(false) }
    var mapLibreMap by remember { mutableStateOf<MapLibreMap?>(null) }
    var locationComponent by remember { mutableStateOf<LocationComponent?>(null) }

    val mapView = remember {
        MapView(context)
    }

    // Fonction pour vérifier les permissions
    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    // Fonction pour configurer le composant de localisation
    fun configureLocationComponent(locationComp: LocationComponent) {
        if (hasLocationPermission()) {
            try {
                locationComp.isLocationComponentEnabled = true
                locationComp.cameraMode = CameraMode.TRACKING
                locationComp.renderMode = RenderMode.COMPASS
            } catch (e: SecurityException) {
                // Gestion d'erreur si les permissions sont révoquées
                locationComp.isLocationComponentEnabled = false
            }
        } else {
            locationComp.isLocationComponentEnabled = false
        }
    }

    DisposableEffect(lifecycleOwner) {
        val lifecycle = lifecycleOwner.lifecycle
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> mapView.onCreate(Bundle())
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> {}
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(selectedStyleIndex) {
        mapLibreMap?.setStyle(mapStyles[selectedStyleIndex].second)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (userLocation == null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
                    .background(
                        Color.Black.copy(alpha = 0.7f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                    Text(
                        text = stringResource(Res.string.Location_in_progress),
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        AndroidView(
            factory = { _ ->
                mapView.apply {
                    getMapAsync { map ->
                        mapLibreMap = map
                        map.setStyle(mapStyles[selectedStyleIndex].second) { style ->
                            val lockerBitmap = createLockerIcon(context)
                            style.addImage("locker-icon", lockerBitmap)

                            locationComponent = map.locationComponent.apply {
                                activateLocationComponent(
                                    LocationComponentActivationOptions
                                        .builder(context, style)
                                        .build()
                                )
                                // Utiliser la fonction locale pour configurer
                                configureLocationComponent(this)
                            }

                            map.addOnMapClickListener { point ->
                                val features = map.queryRenderedFeatures(
                                    map.projection.toScreenLocation(point),
                                    "locker-layer"
                                )

                                if (features.isNotEmpty()) {
                                    val feature = features[0]
                                    val lockerName = feature.getStringProperty("name")

                                    val selectedLocker = lockers.find { it.name == lockerName }
                                    selectedLocker?.let {
                                        viewModel.selectLocker(it)
                                    }
                                }
                                true
                            }
                        }

                        map.uiSettings.apply {
                            isCompassEnabled = true
                            isRotateGesturesEnabled = true
                            isTiltGesturesEnabled = false
                            isAttributionEnabled = true
                            isLogoEnabled = true
                        }
                    }
                }
            },
            update = { view ->
                view.getMapAsync { map ->
                    map.getStyle { style ->
                        map.clear()

                        lockers.forEach { locker ->
                            val markerOptions = org.maplibre.android.annotations.MarkerOptions()
                                .position(org.maplibre.android.geometry.LatLng(locker.latitude, locker.longitude))
                                .title(locker.name)
                                .snippet(locker.location)

                            val marker = map.addMarker(markerOptions)
                        }

                        map.setOnMarkerClickListener { marker ->
                            val clickedLocker = lockers.find { locker ->
                                locker.latitude == marker.position.latitude &&
                                        locker.longitude == marker.position.longitude
                            }

                            clickedLocker?.let { locker ->
                                viewModel.selectLocker(locker)
                            }

                            true
                        }

                        userLocation?.let { location ->
                            map.animateCamera(
                                org.maplibre.android.camera.CameraUpdateFactory.newLatLngZoom(
                                    org.maplibre.android.geometry.LatLng(location.latitude, location.longitude),
                                    15.0
                                )
                            )
                        } ?: run {
                            if (lockers.isNotEmpty()) {
                                val firstLocker = lockers.first()
                                map.animateCamera(
                                    org.maplibre.android.camera.CameraUpdateFactory.newLatLngZoom(
                                        org.maplibre.android.geometry.LatLng(firstLocker.latitude, firstLocker.longitude),
                                        12.0
                                    )
                                )
                            }
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        FloatingActionButton(
            onClick = { showStyleSelector = true },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Settings, contentDescription = stringResource(Res.string.Change_map_style))
        }

        if (showStyleSelector) {
            Card(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = stringResource(Res.string.Map_style),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    mapStyles.forEachIndexed { index, (name, _) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedStyleIndex = index
                                    showStyleSelector = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedStyleIndex == index,
                                onClick = {
                                    selectedStyleIndex = index
                                    showStyleSelector = false
                                }
                            )
                            Text(
                                text = name,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { showStyleSelector = false },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(stringResource(Res.string.Close))
                    }
                }
            }
        }
    }
}

private fun createLockerIcon(context: android.content.Context): Bitmap {
    val size = 48
    val bitmap = createBitmap(size, size)
    val canvas = Canvas(bitmap)

    val paint = Paint().apply {
        isAntiAlias = true
        color = "#FF4444".toColorInt()
        style = Paint.Style.FILL
    }

    val radius = size / 2f - 2f
    canvas.drawCircle(size / 2f, size / 2f, radius, paint)

    paint.apply {
        color = android.graphics.Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }
    canvas.drawCircle(size / 2f, size / 2f, radius, paint)

    paint.apply {
        color = android.graphics.Color.WHITE
        style = Paint.Style.FILL
        textSize = 20f
        textAlign = Paint.Align.CENTER
    }

    val lockSymbol = "🔒"
    val bounds = Rect()
    paint.getTextBounds(lockSymbol, 0, lockSymbol.length, bounds)
    canvas.drawText(lockSymbol, size / 2f, size / 2f + bounds.height() / 2f, paint)

    return bitmap
}

@Composable
private fun LoadingState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(stringResource(Res.string.Loading))
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