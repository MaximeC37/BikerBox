package org.perso.bikerbox.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.perso.bikerbox.data.models.PaymentCard
import org.perso.bikerbox.data.models.PaymentMethod
import org.perso.bikerbox.data.models.PaymentRequest
import org.perso.bikerbox.data.services.PaymentService

class PaymentViewModel : ViewModel() {
    private val paymentService = PaymentService()

    private val _state = MutableStateFlow<PaymentState>(PaymentState.Idle)
    val state: StateFlow<PaymentState> = _state.asStateFlow()

    fun initializePayment(reservationId: String, amount: Double) {
        _state.value = PaymentState.Loading
        viewModelScope.launch {
            try {
                val availableCards = paymentService.getPaymentCards()
                _state.value = PaymentState.PaymentMethodSelection(
                    amount = amount,
                    reservationId = reservationId,
                    availableCards = availableCards
                )
            } catch (e: Exception) {
                _state.value = PaymentState.Error("Error loading payment methods")
            }
        }
    }

    fun selectPaymentMethod(method: PaymentMethod, cardId: String? = null) {
        val currentState = _state.value
        if (currentState is PaymentState.PaymentMethodSelection) {
            if (method == PaymentMethod.CREDIT_CARD && cardId == null) {
                // Redirect to card addition
                _state.value = PaymentState.CardEntry(
                    amount = currentState.amount,
                    reservationId = currentState.reservationId
                )
            } else {
                processPayment(
                    PaymentRequest(
                        reservationId = currentState.reservationId,
                        amount = currentState.amount,
                        paymentMethod = method,
                        cardId = cardId
                    )
                )
            }
        }
    }

    private fun processPayment(request: PaymentRequest) {
        _state.value = PaymentState.Processing(
            paymentId = "temp_${System.currentTimeMillis()}",
            amount = request.amount
        )

        viewModelScope.launch {
            try {
                val payment = paymentService.processPayment(request)
                if (payment.status == org.perso.bikerbox.data.models.PaymentStatus.SUCCESS) {
                    _state.value = PaymentState.Success(payment)
                } else {
                    _state.value = PaymentState.Error("Payment declined. Please try again.")
                }
            } catch (e: Exception) {
                _state.value = PaymentState.Error("Error processing payment: ${e.message}")
            }
        }
    }

    fun addNewCard(card: PaymentCard) {
        viewModelScope.launch {
            try {
                val newCard = paymentService.addPaymentCard(card)
                // Return to payment method selection
                val currentState = _state.value
                if (currentState is PaymentState.CardEntry) {
                    val availableCards = paymentService.getPaymentCards()
                    _state.value = PaymentState.PaymentMethodSelection(
                        amount = currentState.amount,
                        reservationId = currentState.reservationId,
                        availableCards = availableCards
                    )
                }
            } catch (e: Exception) {
                _state.value = PaymentState.Error("Error adding card")
            }
        }
    }

    fun resetState() {
        _state.value = PaymentState.Idle
    }
}