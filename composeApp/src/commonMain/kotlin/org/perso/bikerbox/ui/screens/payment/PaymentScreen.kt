package org.perso.bikerbox.ui.screens.payment

import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import org.perso.bikerbox.ui.viewmodel.PaymentState
import org.perso.bikerbox.ui.viewmodel.PaymentViewModel

@Composable
fun PaymentScreen(
    reservationId: String,
    amount: Double,
    onPaymentSuccess: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val viewModel: PaymentViewModel = viewModel()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(reservationId, amount) {
        viewModel.initializePayment(reservationId, amount)
    }

    when (val currentState = state) {
        is PaymentState.Idle -> {
            // État initial
        }

        is PaymentState.Loading -> {
            PaymentProcessingScreen(
                state = PaymentState.Processing(
                    paymentId = "loading",
                    amount = amount
                )
            )
        }

        is PaymentState.PaymentMethodSelection -> {
            PaymentMethodScreen(
                viewModel = viewModel,
                state = currentState,
                onNavigateBack = onNavigateBack
            )
        }

        is PaymentState.CardEntry -> {
            AddCardScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    viewModel.initializePayment(reservationId, amount)
                }
            )
        }

        is PaymentState.Processing -> {
            PaymentProcessingScreen(state = currentState)
        }

        is PaymentState.Success -> {
            PaymentSuccessScreen(
                state = currentState,
                onContinue = {
                    viewModel.resetState()
                    onPaymentSuccess()
                }
            )
        }

        is PaymentState.Error -> {
            PaymentErrorScreen(
                state = currentState,
                onRetry = {
                    viewModel.initializePayment(reservationId, amount)
                },
                onCancel = {
                    viewModel.resetState()
                    onNavigateBack()
                }
            )
        }
    }
}
