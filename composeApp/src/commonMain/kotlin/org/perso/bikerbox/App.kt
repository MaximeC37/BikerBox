package org.perso.bikerbox

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.perso.bikerbox.data.repository.AuthRepositoryFactory
import org.perso.bikerbox.data.repository.LockersProvider
import org.perso.bikerbox.domain.usecases.CancelReservationUseCase
import org.perso.bikerbox.domain.usecases.GetAvailableLockersUseCase
import org.perso.bikerbox.domain.usecases.GetUserReservationsUseCase
import org.perso.bikerbox.domain.usecases.MakeReservationUseCase
import org.perso.bikerbox.ui.navigation.Navigation
import org.perso.bikerbox.ui.theme.AppTheme
import org.perso.bikerbox.ui.viewmodel.AuthViewModel
import org.perso.bikerbox.ui.viewmodel.ReservationViewModel

@SuppressLint("ViewModelConstructorInComposable")
@Composable
fun App() {
    AppTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            // Use repository provided by LockersProvider with verification
            val repository = try {
                LockersProvider.repository
            } catch (e: Exception) {
                Log.e("App", "Error retrieving repository: ${e.message}")
                throw IllegalStateException("Repository not properly initialized")
            }

            // Initialize authentication repository
            val authRepository = AuthRepositoryFactory.createRepository()

            // Initialize use cases
            val getAvailableLockersUseCase = GetAvailableLockersUseCase(repository)
            val makeReservationUseCase = MakeReservationUseCase(repository)
            val getUserReservationsUseCase = GetUserReservationsUseCase(repository)
            val cancelReservationUseCase = CancelReservationUseCase(repository)

            // Initialize ViewModels
            val reservationViewModel = ReservationViewModel(
                getAvailableLockersUseCase = getAvailableLockersUseCase,
                makeReservationUseCase = makeReservationUseCase,
                getUserReservationsUseCase = getUserReservationsUseCase,
                cancelReservationUseCase = cancelReservationUseCase
            )

            val authViewModel = AuthViewModel(authRepository)

            // Navigation
            Navigation(reservationViewModel = reservationViewModel, authViewModel = authViewModel)
        }
    }
}
