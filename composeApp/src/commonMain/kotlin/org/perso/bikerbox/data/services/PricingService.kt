package org.perso.bikerbox.data.services

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.perso.bikerbox.data.models.LockerSize
import kotlin.math.ceil

class PricingService {
    companion object {
        // Base price per day according to size (per 24h period)
        private const val BASE_PRICE_SINGLE = 6.0
        private const val BASE_PRICE_DOUBLE = 10.0

        // Tiers for progressive pricing (in days)
        private const val TIER_1_DAYS = 3  // First tier: 3 days and more
        private const val TIER_2_DAYS = 7  // Second tier: 7 days and more
        private const val TIER_3_DAYS = 30 // Third tier: 30 days and more (monthly)

        // Discounts (in percentage)
        private const val TIER_1_DISCOUNT = 0.10 // 10% discount
        private const val TIER_2_DISCOUNT = 0.20 // 20% discount
        private const val TIER_3_DISCOUNT = 0.30 // 30% discount

        fun calculatePrice(size: LockerSize, startDate: LocalDateTime, endDate: LocalDateTime): Double {
            // Calculate total hours
            val totalHours = calculateTotalHoursBetween(startDate, endDate)

            // If duration is null or negative, return 0
            if (totalHours <= 0) return 0.0

            // Determine base price according to size
            val basePrice = when (size) {
                LockerSize.SINGLE -> BASE_PRICE_SINGLE
                LockerSize.DOUBLE -> BASE_PRICE_DOUBLE
                // Other sizes if needed
            }

            // Calculate price for each started 24h period
            val days = ceil(totalHours / 24.0).coerceAtLeast(1.0)

            // Calculate standard price
            val standardPrice = basePrice * days

            // Apply discount according to number of days
            val priceWithDiscount = applyDiscount(standardPrice, days.toInt())

            // Round to 2 decimal places
            return (kotlin.math.round(priceWithDiscount * 100) / 100)
        }

        private fun applyDiscount(standardPrice: Double, days: Int): Double {
            val discountRate = when {
                days >= TIER_3_DAYS -> TIER_3_DISCOUNT // 30% for 30 days and more
                days >= TIER_2_DAYS -> TIER_2_DISCOUNT // 20% for 7 days and more
                days >= TIER_1_DAYS -> TIER_1_DISCOUNT // 10% for 3 days and more
                else -> 0.0                           // No discount
            }

            return standardPrice * (1.0 - discountRate)
        }

        fun calculateTotalHoursBetween(startDate: LocalDateTime, endDate: LocalDateTime): Double {
            // Convert to Instant (machine time) for precise calculation
            val startInstant = startDate.toInstant(TimeZone.currentSystemDefault())
            val endInstant = endDate.toInstant(TimeZone.currentSystemDefault())

            // Calculate difference in milliseconds
            val diffInMillis = endInstant.toEpochMilliseconds() - startInstant.toEpochMilliseconds()

            // Convert to hours (1 hour = 3,600,000 milliseconds)
            return diffInMillis / 3_600_000.0
        }
    }
}







