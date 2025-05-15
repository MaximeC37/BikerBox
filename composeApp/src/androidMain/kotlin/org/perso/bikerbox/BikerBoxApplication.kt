package org.perso.bikerbox

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import org.perso.bikerbox.data.repository.AuthRepositoryFactory
import org.perso.bikerbox.data.repository.LockersProvider
import org.perso.bikerbox.data.repository.LockersRepositoryFactory
import org.perso.bikerbox.data.repository.LockersRepositoryFactoryImpl
import org.perso.bikerbox.data.repository.firebase.FirebaseLockersRepository

class BikerBoxApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        try {
            // Initialiser Firebase
            FirebaseApp.initializeApp(this)
            Log.d("BikerBoxApplication", "Firebase initialisé")

            // Initialiser le repository
            Log.d("BikerBoxApplication", "Initialisation du repository Firebase")
            val repository = LockersRepositoryFactoryImpl.createRepository()
            LockersProvider.initialize(repository)
            Log.d("BikerBoxApplication", "Repository initialisé avec succès")
        } catch (e: Exception) {
            Log.e("BikerBoxApplication", "Erreur lors de l'initialisation: ${e.message}")
            e.printStackTrace()
        }
    }
}
