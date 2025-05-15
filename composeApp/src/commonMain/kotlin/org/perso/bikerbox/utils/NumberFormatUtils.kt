package org.perso.bikerbox.utils

import kotlin.math.pow
import kotlin.math.roundToLong

/**
 * Formate un nombre décimal avec le nombre spécifié de décimales
 */
fun Double.formatDecimal(decimals: Int = 2): String {
    val factor = 10.0.pow(decimals.toDouble())
    val roundedValue = (this * factor).roundToLong() / factor

    // Convertir en chaîne et ajouter des zéros si nécessaire
    val stringValue = roundedValue.toString()
    val parts = stringValue.split('.')

    return if (parts.size == 1) {
        // Pas de partie décimale, ajouter .00
        "$stringValue.${"0".repeat(decimals)}"
    } else {
        // Compléter avec des zéros si nécessaire
        val decimalPart = parts[1]
        if (decimalPart.length < decimals) {
            "${parts[0]}.${decimalPart.padEnd(decimals, '0')}"
        } else {
            stringValue
        }
    }
}

