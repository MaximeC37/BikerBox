package org.perso.bikerbox.ui.viewmodel

import org.perso.bikerbox.data.models.Payment
import org.perso.bikerbox.data.models.PaymentCard

sealed class PaymentState {
    object Idle : PaymentState()
    object Loading : PaymentState()
    data class PaymentMethodSelection(
        val amount: Double,
        val reservationId: String,
        val availableCards: List<PaymentCard>
    ) : PaymentState()
    data class CardEntry(
        val amount: Double,
        val reservationId: String
    ) : PaymentState()
    data class Processing(
        val paymentId: String,
        val amount: Double
    ) : PaymentState()
    data class Success(
        val payment: Payment,
        val message: String = "Payment successful!"
    ) : PaymentState()
    data class Error(val message: String) : PaymentState()
}