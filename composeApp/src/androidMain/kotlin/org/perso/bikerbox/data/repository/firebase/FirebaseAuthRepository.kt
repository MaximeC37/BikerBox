package org.perso.bikerbox.data.repository.firebase

import androidx.core.net.toUri
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import org.perso.bikerbox.data.models.Resource
import org.perso.bikerbox.data.models.User
import org.perso.bikerbox.data.repository.AuthRepository

class FirebaseAuthRepository : AuthRepository {
    private val auth by lazy { Firebase.auth }
    private val firestore by lazy { Firebase.firestore }
    private val usersCollection by lazy { firestore.collection("users") }



    override val currentUser: Flow<User?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser?.toUser())
        }

        auth.addAuthStateListener(listener)

        awaitClose {
            auth.removeAuthStateListener(listener)
        }
    }

    override suspend fun signUp(email: String, password: String): Resource<User> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user

            if (firebaseUser != null) {
                val user = firebaseUser.toUser()

                // Sauvegarder les informations complémentaires dans Firebase
                usersCollection.document(user.id).set(user).await()

                Resource.Success(user)
            } else {
                Resource.Error("Échec de création d'utilisateur")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Erreur inconnue", e)
        }
    }

    override suspend fun signIn(email: String, password: String): Resource<User> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user

            if (firebaseUser != null) {
                Resource.Success(firebaseUser.toUser())
            } else {
                Resource.Error("Échec de connexion")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Erreur inconnue", e)
        }
    }

    override suspend fun signOut(): Resource<Unit> {
        return try {
            auth.signOut()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Échec de déconnexion", e)
        }
    }


    override suspend fun sendPasswordReset(email: String): Resource<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Échec d'envoi du mail de réinitialisation", e)
        }
    }

    override suspend fun updateUserProfile(
        displayName: String?,
        phoneNumber: String?,
        profileImageUrl: String?
    ): Resource<User> {
        val firebaseUser = auth.currentUser ?: return Resource.Error("Aucun utilisateur connecté")

        return try {
            // Mise à jour des données dans Firebase Auth
            val profileUpdates = UserProfileChangeRequest.Builder().apply {
                displayName?.let { setDisplayName(it) }
                profileImageUrl?.let { this.photoUri = it.toUri() }
            }.build()

            firebaseUser.updateProfile(profileUpdates).await()

            // Mise à jour des données dans Firestore
            val userData = hashMapOf<String, Any>()
            displayName?.let { userData["displayName"] = it }
            phoneNumber?.let { userData["phoneNumber"] = it }
            profileImageUrl?.let { userData["profileImageUrl"] = it }

            if (userData.isNotEmpty()) {
                usersCollection.document(firebaseUser.uid).update(userData).await()
            }

            Resource.Success(firebaseUser.toUser())
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Échec de mise à jour du profil", e)
        }
    }

    // Extension pour convertir FirebaseUser en modèle User
    private fun FirebaseUser.toUser(): User {
        return User(
            id = uid,
            email = email ?: "",
            displayName = displayName,
            phoneNumber = phoneNumber,
            profileImageUrl = photoUrl?.toString()
        )
    }

}