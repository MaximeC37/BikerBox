package org.perso.bikerbox.permissions

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import bikerbox.composeapp.generated.resources.*
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocationPermissionHandler(
    onPermissionGranted: @Composable () -> Unit,
    onPermissionDenied: @Composable () -> Unit
) {
    val context = LocalContext.current
    val permissionManager = remember { LocationPermissionManager(context) }
    val permissionState by permissionManager.permissionState.collectAsState()

    val locationPermissionsState = rememberMultiplePermissionsState(
        listOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    LaunchedEffect(locationPermissionsState.allPermissionsGranted) {
        if (locationPermissionsState.allPermissionsGranted) {
            permissionManager.updatePermissionState(LocationPermissionState.GRANTED)
        } else {
            permissionManager.checkPermissionStatus()
        }
    }

    LaunchedEffect(Unit) {
        permissionManager.checkPermissionStatus()
    }

    when (permissionState) {
        LocationPermissionState.GRANTED -> {
            onPermissionGranted()
        }
        LocationPermissionState.NOT_REQUESTED -> {
            LocationPermissionExplanation(
                onRequestPermission = {
                    permissionManager.updatePermissionState(LocationPermissionState.REQUESTING)
                    locationPermissionsState.launchMultiplePermissionRequest()
                }
            )
        }
        LocationPermissionState.DENIED -> {
            LocationPermissionRationale(
                onRequestPermission = {
                    permissionManager.updatePermissionState(LocationPermissionState.REQUESTING)
                    locationPermissionsState.launchMultiplePermissionRequest()
                },
                onDeny = {
                    permissionManager.updatePermissionState(LocationPermissionState.PERMANENTLY_DENIED)
                }
            )
        }
        LocationPermissionState.PERMANENTLY_DENIED -> {
            var userHasAcknowledgedDenial by remember { mutableStateOf(false) }

            if (userHasAcknowledgedDenial) {
                onPermissionDenied()
            } else {
                LocationPermissionDenied(
                    onOpenSettings = {
                        permissionManager.openAppSettings()
                    },
                    onDeny = {
                        userHasAcknowledgedDenial = true
                    }
                )
            }
        }
        LocationPermissionState.REQUESTING -> {
            LocationPermissionLoading()
        }
    }
}


@Composable
private fun LocationPermissionExplanation(onRequestPermission: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(64.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(Res.string.Location_permission_required),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(Res.string.Location_permission_explanation),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onRequestPermission, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(Res.string.Continue))
            }
        }
    }
}

@Composable
private fun LocationPermissionRationale(onRequestPermission: () -> Unit, onDeny: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(Res.string.Location_permission_denied),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(Res.string.Location_rationale),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(onClick = onDeny, modifier = Modifier.weight(1f)) {
                    Text(stringResource(Res.string.Cancel))
                }
                Button(onClick = onRequestPermission, modifier = Modifier.weight(1f)) {
                    Text(stringResource(Res.string.Retry))
                }
            }
        }
    }
}

@Composable
private fun LocationPermissionDenied(
    onOpenSettings: () -> Unit,
    onDeny: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(Res.string.Location_denied_permanently),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(Res.string.Location_settings_message),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onDeny, // onDeny est maintenant un simple () -> Unit
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(Res.string.Continue_without_location))
                }
                Button(
                    onClick = onOpenSettings,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(Res.string.Open_settings))
                }
            }
        }
    }
}

@Composable
private fun LocationPermissionLoading() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(Res.string.Verifying_permissions),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}