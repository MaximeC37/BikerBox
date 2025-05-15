package org.perso.bikerbox.utils

/**
 * Validation simple d'email pour Kotlin Multiplatform
 */
fun isValidEmail(email: String): Boolean {
    val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
    return email.matches(emailRegex.toRegex())
}
