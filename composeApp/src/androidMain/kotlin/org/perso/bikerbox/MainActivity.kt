package org.perso.bikerbox

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.FirebaseApp
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

        setContent {
            val app = applicationContext as BikerBoxApplication

            val reservationViewModel: ReservationViewModel = viewModel(
                factory = ReservationViewModelFactory(app)
            )
            val authViewModel: AuthViewModel = viewModel(
                factory = AuthViewModelFactory(app)
            )

            App(
                reservationViewModel = reservationViewModel,
                authViewModel = authViewModel
            )
        }
    }
}

class ReservationViewModelFactory(
    private val app: BikerBoxApplication
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReservationViewModel::class.java)) {
            val lockersRepository = LockersProvider.repository
            return ReservationViewModel(
                getAvailableLockersUseCase = GetAvailableLockersUseCase(lockersRepository),
                makeReservationUseCase = MakeReservationUseCase(lockersRepository),
                getUserReservationsUseCase = GetUserReservationsUseCase(lockersRepository),
                cancelReservationUseCase = CancelReservationUseCase(lockersRepository),
                locationProvider = app.locationProvider
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class AuthViewModelFactory(
    private val app: BikerBoxApplication
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            return AuthViewModel(app.authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview
@Composable
fun AppAndroidPreview() {
    val context = LocalContext.current

    try {
        FirebaseApp.initializeApp(context)
    } catch (_: Exception) {
    }

    val app = context.applicationContext as BikerBoxApplication
    LockersProvider.initialize(FirebaseLockersRepository())

    val mockReservationViewModel = ReservationViewModel(
        GetAvailableLockersUseCase(LockersProvider.repository),
        MakeReservationUseCase(LockersProvider.repository),
        GetUserReservationsUseCase(LockersProvider.repository),
        CancelReservationUseCase(LockersProvider.repository),
        app.locationProvider
    )
    val mockAuthViewModel = AuthViewModel(app.authRepository)

    App(
        reservationViewModel = mockReservationViewModel,
        authViewModel = mockAuthViewModel
    )
}