package org.perso.bikerbox.data.services

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.perso.bikerbox.data.models.LockerSize
import kotlin.math.ceil

class PricingService {
    companion object {
        // Prix de base par jour selon la taille (par tranche de 24h)
        private const val BASE_PRICE_SINGLE = 6.0
        private const val BASE_PRICE_DOUBLE = 10.0

        // Paliers pour tarifs dégressifs (en jours)
        private const val TIER_1_DAYS = 3  // Premier palier : 3 jours et plus
        private const val TIER_2_DAYS = 7  // Second palier : 7 jours et plus
        private const val TIER_3_DAYS = 30 // Troisième palier : 30 jours et plus (mensuel)

        // Réductions (en pourcentage)
        private const val TIER_1_DISCOUNT = 0.10 // 10% de réduction
        private const val TIER_2_DISCOUNT = 0.20 // 20% de réduction
        private const val TIER_3_DISCOUNT = 0.30 // 30% de réduction


        fun calculatePrice(size: LockerSize, startDate: LocalDateTime, endDate: LocalDateTime): Double {
            // Calcul des heures totales
            val totalHours = calculateTotalHoursBetween(startDate, endDate)

            // Si la durée est nulle ou négative, retourner 0
            if (totalHours <= 0) return 0.0

            // Déterminer le prix de base selon la taille
            val basePrice = when (size) {
                LockerSize.SINGLE -> BASE_PRICE_SINGLE
                LockerSize.DOUBLE -> BASE_PRICE_DOUBLE
                // Autres tailles si nécessaire
            }

            // Calculer le prix pour chaque tranche de 24h entamée
            val days = ceil(totalHours / 24.0).coerceAtLeast(1.0)

            // Calculer le prix standard
            val standardPrice = basePrice * days

            // Appliquer la réduction selon le nombre de jours
            val priceWithDiscount = applyDiscount(standardPrice, days.toInt())

            // Arrondir à 2 chiffres après la virgule
            return (kotlin.math.round(priceWithDiscount * 100) / 100)
        }
        private fun applyDiscount(standardPrice: Double, days: Int): Double {
            val discountRate = when {
                days >= TIER_3_DAYS -> TIER_3_DISCOUNT // 30% pour 30 jours et plus
                days >= TIER_2_DAYS -> TIER_2_DISCOUNT // 20% pour 7 jours et plus
                days >= TIER_1_DAYS -> TIER_1_DISCOUNT // 10% pour 3 jours et plus
                else -> 0.0                           // Pas de réduction
            }

            return standardPrice * (1.0 - discountRate)
        }
        fun calculateTotalHoursBetween(startDate: LocalDateTime, endDate: LocalDateTime): Double {
            // Convertir en Instant (temps machine) pour un calcul précis
            val startInstant = startDate.toInstant(TimeZone.currentSystemDefault())
            val endInstant = endDate.toInstant(TimeZone.currentSystemDefault())

            // Calculer la différence en millisecondes
            val diffInMillis = endInstant.toEpochMilliseconds() - startInstant.toEpochMilliseconds()

            // Convertir en heures (1 heure = 3 600 000 millisecondes)
            return diffInMillis / 3_600_000.0
        }

    }
}







