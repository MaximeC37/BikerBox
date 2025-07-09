package org.perso.bikerbox

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.perso.bikerbox.ui.navigation.Navigation
import org.perso.bikerbox.ui.theme.AppTheme
import org.perso.bikerbox.ui.viewmodel.AuthViewModel
import org.perso.bikerbox.ui.viewmodel.ReservationViewModel

@Composable
fun App(reservationViewModel: ReservationViewModel, authViewModel: AuthViewModel) {
    AppTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Navigation(reservationViewModel = reservationViewModel, authViewModel = authViewModel)
        }
    }
}