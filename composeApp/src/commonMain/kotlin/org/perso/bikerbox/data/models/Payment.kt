package org.perso.bikerbox.data.models

import kotlinx.datetime.LocalDateTime

data class Payment(
    val id: String,
    val reservationId: String,
    val amount: Double,
    val paymentMethod: PaymentMethod,
    val status: PaymentStatus,
    val timestamp: LocalDateTime,
    val transactionId: String,
    val cardLast4Digits: String? = null
)

enum class PaymentMethod(val displayName: String, val icon: String) {
    CREDIT_CARD("Credit Card", "💳"),
    PAYPAL("PayPal", "🅿️"),
    APPLE_PAY("Apple Pay", "🍎"),
    GOOGLE_PAY("Google Pay", "🇬"),
    BANK_TRANSFER("Bank Transfer", "🏦")
}

enum class PaymentStatus(val displayName: String) {
    PENDING("Pending"),
    PROCESSING("Processing"),
    SUCCESS("Success"),
    FAILED("Failed"),
    REFUNDED("Refunded")
}

data class PaymentCard(
    val id: String,
    val cardNumber: String, //"****-****-****-1234"
    val cardHolderName: String,
    val expiryMonth: Int,
    val expiryYear: Int,
    val cardType: CardType,
    val isDefault: Boolean = false
)

enum class CardType(val displayName: String, val icon: String) {
    VISA("Visa", "💳"),
    MASTERCARD("Mastercard", "💳"),
    AMERICAN_EXPRESS("American Express", "💳"),
    DISCOVER("Discover", "💳")
}

data class PaymentRequest(
    val reservationId: String,
    val amount: Double,
    val paymentMethod: PaymentMethod,
    val cardId: String? = null
)
