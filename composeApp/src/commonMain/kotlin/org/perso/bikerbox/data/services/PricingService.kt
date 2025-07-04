package org.perso.bikerbox.data.services

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.perso.bikerbox.data.models.LockerSize
import kotlin.math.ceil

class PricingService {
    companion object {
        private const val BASE_PRICE_SMALL = 6.0
        private const val BASE_PRICE_MEDIUM = 10.0
        private const val BASE_PRICE_LARGE = 14.0

        private const val TIER_1_DAYS = 3
        private const val TIER_2_DAYS = 7
        private const val TIER_3_DAYS = 30

        // Discounts (in percentage)
        private const val TIER_1_DISCOUNT = 0.10
        private const val TIER_2_DISCOUNT = 0.20
        private const val TIER_3_DISCOUNT = 0.30

        fun calculatePrice(size: LockerSize, startDate: LocalDateTime, endDate: LocalDateTime): Double {
            val totalHours = calculateTotalHoursBetween(startDate, endDate)

            if (totalHours <= 0) return 0.0

            val basePrice = when (size) {
                LockerSize.SMALL -> BASE_PRICE_SMALL
                LockerSize.MEDIUM -> BASE_PRICE_MEDIUM
                LockerSize.LARGE -> BASE_PRICE_LARGE
            }

            val days = ceil(totalHours / 24.0).coerceAtLeast(1.0)

            val standardPrice = basePrice * days

            val priceWithDiscount = applyDiscount(standardPrice, days.toInt())

            return (kotlin.math.round(priceWithDiscount * 100) / 100)
        }

        private fun applyDiscount(standardPrice: Double, days: Int): Double {
            val discountRate = when {
                days >= TIER_3_DAYS -> TIER_3_DISCOUNT
                days >= TIER_2_DAYS -> TIER_2_DISCOUNT
                days >= TIER_1_DAYS -> TIER_1_DISCOUNT
                else -> 0.0
            }

            return standardPrice * (1.0 - discountRate)
        }

        fun calculateTotalHoursBetween(startDate: LocalDateTime, endDate: LocalDateTime): Double {
            val startInstant = startDate.toInstant(TimeZone.currentSystemDefault())
            val endInstant = endDate.toInstant(TimeZone.currentSystemDefault())
            val diffInMillis = endInstant.toEpochMilliseconds() - startInstant.toEpochMilliseconds()

            return diffInMillis / 3_600_000.0
        }
    }
}







