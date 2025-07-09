package org.perso.bikerbox.permissions

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LocationPermissionManager(
    private val context: Context
) {
    private val _permissionState = MutableStateFlow(LocationPermissionState.NOT_REQUESTED)
    val permissionState: StateFlow<LocationPermissionState> = _permissionState.asStateFlow()

    companion object {
        private const val FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION
        private const val COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION
    }

    fun checkPermissionStatus() {
        val fineLocationGranted = ContextCompat.checkSelfPermission(
            context, FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocationGranted = ContextCompat.checkSelfPermission(
            context, COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        _permissionState.value = when {
            fineLocationGranted || coarseLocationGranted -> LocationPermissionState.GRANTED
            shouldShowRationale() -> LocationPermissionState.DENIED
            else -> LocationPermissionState.NOT_REQUESTED
        }
    }

    private fun shouldShowRationale(): Boolean {
        return if (context is ComponentActivity) {
            ActivityCompat.shouldShowRequestPermissionRationale(context, FINE_LOCATION) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(context, COARSE_LOCATION)
        } else {
            false
        }
    }

    fun updatePermissionState(state: LocationPermissionState) {
        _permissionState.value = state
    }

    fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    fun isLocationPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            context, FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context, COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }
}