package org.perso.bikerbox.data.services

import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.perso.bikerbox.data.models.*
import kotlin.random.Random

class PaymentService {

    private val mockCards = mutableListOf(
        PaymentCard(
            id = "card_1",
            cardNumber = "****-****-****-1234",
            cardHolderName = "Jean Dupont",
            expiryMonth = 12,
            expiryYear = 2027,
            cardType = CardType.VISA,
            isDefault = true
        ),
        PaymentCard(
            id = "card_2",
            cardNumber = "****-****-****-5678",
            cardHolderName = "Jean Dupont",
            expiryMonth = 8,
            expiryYear = 2026,
            cardType = CardType.MASTERCARD,
            isDefault = false
        )
    )

    private val paymentHistory = mutableListOf<Payment>()

    suspend fun processPayment(request: PaymentRequest): Payment {
        // Simulate processing delay
        delay(2000)

        // Simulate occasional failure (10% chance)
        val isSuccess = Random.nextFloat() > 0.1f

        val payment = Payment(
            id = "pay_${System.currentTimeMillis()}",
            reservationId = request.reservationId,
            amount = request.amount,
            paymentMethod = request.paymentMethod,
            status = if (isSuccess) PaymentStatus.SUCCESS else PaymentStatus.FAILED,
            timestamp = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
            transactionId = "txn_${Random.nextLong(100000, 999999)}",
            cardLast4Digits = request.cardId?.let {
                mockCards.find { card -> card.id == it }?.cardNumber?.takeLast(4)
            }
        )

        paymentHistory.add(payment)
        return payment
    }

    suspend fun addPaymentCard(card: PaymentCard): PaymentCard {
        delay(1000) // Simulate card addition
        val newCard = card.copy(id = "card_${System.currentTimeMillis()}")
        mockCards.add(newCard)
        return newCard
    }

    fun getPaymentCards(): List<PaymentCard> = mockCards.toList()

    fun getPaymentHistory(): List<Payment> = paymentHistory.toList()

    suspend fun refundPayment(paymentId: String): Payment? {
        delay(1500)
        val payment = paymentHistory.find { it.id == paymentId }
        return payment?.let {
            val refundedPayment = it.copy(
                status = PaymentStatus.REFUNDED,
                timestamp = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            )
            paymentHistory[paymentHistory.indexOf(it)] = refundedPayment
            refundedPayment
        }
    }
}