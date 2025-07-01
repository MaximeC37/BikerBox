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
import org.perso.bikerbox.ui.screens.payment.PaymentScreen
import org.perso.bikerbox.ui.viewmodel.AuthViewModel
import org.perso.bikerbox.ui.viewmodel.ReservationViewModel

@Composable
fun Navigation(reservationViewModel: ReservationViewModel, authViewModel: AuthViewModel) {
    val navController = rememberNavigationController()

    var selectedLockerId by remember { mutableStateOf("") }
    var selectedSize by remember { mutableStateOf<LockerSize?>(null) }
    var paymentAmount by remember { mutableStateOf(0.0) }
    var reservationIdForPayment by remember { mutableStateOf("") }

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
                    val state = reservationViewModel.state.value
                    if (state is org.perso.bikerbox.ui.viewmodel.ReservationState.ConfirmationNeeded) {
                        paymentAmount = state.price
                        reservationIdForPayment = "reservation_${System.currentTimeMillis()}"
                        navController.navigateTo("payment")
                    } else if (state is org.perso.bikerbox.ui.viewmodel.ReservationState.Success) {
                        navController.navigateAndClearBackStack("home")
                    }
                },
                onCancel = { navController.popBackStack() }
            )
        }

        "payment" -> {
            PaymentScreen(
                reservationId = reservationIdForPayment,
                amount = paymentAmount,
                onPaymentSuccess = {
                    reservationViewModel.confirmReservation()
                    navController.navigateAndReplace("my_reservations")
                },
                onNavigateBack = { navController.popBackStack() }
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


