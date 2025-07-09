package org.perso.bikerbox

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import org.perso.bikerbox.data.location.LocationProvider

class BikerBoxApplication : Application() {
    val locationProvider: LocationProvider by lazy {
        LocationProvider(this)
    }

    override fun onCreate() {
        super.onCreate()
        try {
            FirebaseApp.initializeApp(this)
            Log.d("BikerBoxApplication", "Firebase initialisé")
        } catch (e: Exception) {
            Log.e("BikerBoxApplication", "Erreur lors de l'initialisation de Firebase: ${e.message}")
        }
    }
}