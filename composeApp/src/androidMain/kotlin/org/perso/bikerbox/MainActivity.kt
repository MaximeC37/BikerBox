package org.perso.bikerbox

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.perso.bikerbox.data.repository.AuthRepositoryFactory
import org.perso.bikerbox.data.repository.LockersProvider
import org.perso.bikerbox.data.repository.firebase.FirebaseLockersRepository
import org.perso.bikerbox.domain.usecases.CancelReservationUseCase
import org.perso.bikerbox.domain.usecases.GetAvailableLockersUseCase
import org.perso.bikerbox.domain.usecases.GetUserReservationsUseCase
import org.perso.bikerbox.domain.usecases.MakeReservationUseCase
import org.perso.bikerbox.ui.viewmodel.AuthViewModel
import org.perso.bikerbox.ui.viewmodel.ReservationViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LockersProvider.initialize(FirebaseLockersRepository())
        val app = applicationContext as BikerBoxApplication

        setContent {
            val lockersRepository = LockersProvider.repository
            val authRepository = AuthRepositoryFactory.createRepository()

            val getAvailableLockersUseCase = GetAvailableLockersUseCase(lockersRepository)
            val makeReservationUseCase = MakeReservationUseCase(lockersRepository)
            val getUserReservationsUseCase = GetUserReservationsUseCase(lockersRepository)
            val cancelReservationUseCase = CancelReservationUseCase(lockersRepository)

            val reservationViewModel = ReservationViewModel(
                getAvailableLockersUseCase = getAvailableLockersUseCase,
                makeReservationUseCase = makeReservationUseCase,
                getUserReservationsUseCase = getUserReservationsUseCase,
                cancelReservationUseCase = cancelReservationUseCase,
                locationProvider = app.locationProvider
            )
            val authViewModel = AuthViewModel(authRepository)

            App(
                reservationViewModel = reservationViewModel,
                authViewModel = authViewModel
            )
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val app = context.applicationContext as BikerBoxApplication

    LockersProvider.initialize(FirebaseLockersRepository())
    val lockersRepository = LockersProvider.repository
    val authRepository = AuthRepositoryFactory.createRepository()

    val getAvailableLockersUseCase = GetAvailableLockersUseCase(lockersRepository)
    val makeReservationUseCase = MakeReservationUseCase(lockersRepository)
    val getUserReservationsUseCase = GetUserReservationsUseCase(lockersRepository)
    val cancelReservationUseCase = CancelReservationUseCase(lockersRepository)

    val reservationViewModel = ReservationViewModel(
        getAvailableLockersUseCase,
        makeReservationUseCase,
        getUserReservationsUseCase,
        cancelReservationUseCase,
        app.locationProvider
    )
    val authViewModel = AuthViewModel(authRepository)

    App(reservationViewModel = reservationViewModel, authViewModel = authViewModel)
}