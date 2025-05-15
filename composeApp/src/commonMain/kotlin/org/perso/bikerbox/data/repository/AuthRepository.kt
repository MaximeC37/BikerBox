package org.perso.bikerbox.data.repository

import kotlinx.coroutines.flow.Flow
import org.perso.bikerbox.data.models.User
import org.perso.bikerbox.data.models.Resource


interface AuthRepository {
    val currentUser: Flow<User?>

    suspend fun signUp(email: String, password: String): Resource<User>

    suspend fun signIn(email: String, password: String): Resource<User>

    suspend fun signOut(): Resource<Unit>

    suspend fun sendPasswordReset(email: String): Resource<Unit>

    suspend fun updateUserProfile(
        displayName: String? = null,
        phoneNumber: String? = null,
        profileImageUrl: String? = null
    ): Resource<User>
}

