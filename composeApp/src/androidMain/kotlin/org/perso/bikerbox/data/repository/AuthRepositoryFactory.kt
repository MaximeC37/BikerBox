package org.perso.bikerbox.data.repository

import org.perso.bikerbox.data.repository.firebase.FirebaseAuthRepository

actual object AuthRepositoryFactory {
    actual fun createRepository(): AuthRepository = FirebaseAuthRepository()
}
