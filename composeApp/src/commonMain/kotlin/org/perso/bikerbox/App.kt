package org.perso.bikerbox

import android.annotation.SuppressLint
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
            val repository = try {
                LockersProvider.repository
            } catch (_: Exception) {
                throw IllegalStateException("Repository not properly initialized")
            }

            val authRepository = AuthRepositoryFactory.createRepository()

            val getAvailableLockersUseCase = GetAvailableLockersUseCase(repository)
            val makeReservationUseCase = MakeReservationUseCase(repository)
            val getUserReservationsUseCase = GetUserReservationsUseCase(repository)
            val cancelReservationUseCase = CancelReservationUseCase(repository)

            val reservationViewModel = ReservationViewModel(
                getAvailableLockersUseCase = getAvailableLockersUseCase,
                makeReservationUseCase = makeReservationUseCase,
                getUserReservationsUseCase = getUserReservationsUseCase,
                cancelReservationUseCase = cancelReservationUseCase
            )

            val authViewModel = AuthViewModel(authRepository)

            Navigation(reservationViewModel = reservationViewModel, authViewModel = authViewModel)
        }
    }
}
