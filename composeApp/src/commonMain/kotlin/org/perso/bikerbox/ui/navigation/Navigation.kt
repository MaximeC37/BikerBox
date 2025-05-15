package org.perso.bikerbox.ui.navigation

import androidx.compose.runtime.*
import org.perso.bikerbox.data.models.LockerSize
import org.perso.bikerbox.ui.screens.Confirmation.ConfirmationScreen
import org.perso.bikerbox.ui.screens.DateSelection.DateSelectionScreen
import org.perso.bikerbox.ui.screens.HomeScreen
import org.perso.bikerbox.ui.screens.auth.LockerDetailsScreen
import org.perso.bikerbox.ui.screens.MyReservationsScreen
import org.perso.bikerbox.ui.screens.auth.ForgotPasswordScreen
import org.perso.bikerbox.ui.screens.auth.LoginScreen
import org.perso.bikerbox.ui.screens.auth.SignUpScreen
import org.perso.bikerbox.ui.screens.profile.ProfileScreen
import org.perso.bikerbox.ui.viewmodel.AuthViewModel
import org.perso.bikerbox.ui.viewmodel.ReservationViewModel

@Composable
fun Navigation(reservationViewModel: ReservationViewModel, authViewModel: AuthViewModel) {
    val navController = rememberNavigationController()

    // État pour stocker les paramètres nécessaires pour certains écrans
    var selectedLockerId by remember { mutableStateOf("") }
    var selectedSize by remember { mutableStateOf<LockerSize?>(null) }

    // Logique de navigation basée sur la route actuelle
    when (val route = navController.currentRoute) {
        "login" -> {
            LoginScreen(
                authViewModel = authViewModel,
                onNavigateToSignUp = { navController.navigateTo("signup") },
                onNavigateToMain = { navController.navigateAndClearBackStack("home") },
                onNavigateToForgotPassword = { navController.navigateTo("forgot_password") }
            )
        }

        "signup" -> {
            SignUpScreen(
                authViewModel = authViewModel,
                onNavigateToLogin = { navController.navigateTo("login") },
                onNavigateToMain = { navController.navigateAndClearBackStack("home") }
            )
        }

        "forgot_password" -> {
            ForgotPasswordScreen(
                authViewModel = authViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        "home" -> {
            HomeScreen(
                viewModel = reservationViewModel,
                authViewModel = authViewModel,
                onSelectLocker = { lockerId ->
                    selectedLockerId = lockerId
                    navController.navigateTo("locker_details")
                },
                onNavigateToReservations = {
                    navController.navigateTo("my_reservations")
                },
                onNavigateToProfile = {
                    navController.navigateTo("profile")
                },
                onSignOut = {
                    authViewModel.signOut()
                    navController.navigateAndClearBackStack("login")
                }
            )
        }

        "locker_details" -> {
            LockerDetailsScreen(
                lockerId = selectedLockerId,
                viewModel = reservationViewModel,
                onNavigateBack = { navController.popBackStack() },
                onSelectSize = { size ->
                    selectedSize = size
                    navController.navigateTo("date_selection")
                }
            )
        }

        "date_selection" -> {
            val locker = reservationViewModel.getLockerById(selectedLockerId)

            selectedSize?.let { size ->
                DateSelectionScreen(
                    lockerId = selectedLockerId,
                    lockerName = locker?.name ?: "",
                    selectedSize = size,
                    onConfirm = { startDate, endDate, price ->
                        reservationViewModel.setReservationDetails(selectedLockerId, size, startDate, endDate, price)
                        navController.navigateTo("confirmation")
                    },
                    onBack = { navController.popBackStack() }
                )
            }
        }

        "confirmation" -> {
            ConfirmationScreen(
                viewModel = reservationViewModel,
                onConfirm = {
                    reservationViewModel.confirmReservation()
                    navController.navigateAndClearBackStack("home")
                },
                onCancel = { navController.popBackStack() }
            )
        }

        "my_reservations" -> {
            MyReservationsScreen(
                viewModel = reservationViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        "profile" -> {
            ProfileScreen(
                authViewModel = authViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

    }
}


