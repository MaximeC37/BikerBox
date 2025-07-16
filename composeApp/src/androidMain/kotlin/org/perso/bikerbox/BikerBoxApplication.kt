package org.perso.bikerbox

import android.app.Application
import com.google.firebase.FirebaseApp
import org.maplibre.android.MapLibre
import org.perso.bikerbox.data.location.LocationProvider
import org.perso.bikerbox.data.repository.AuthRepository
import org.perso.bikerbox.data.repository.LockersProvider
import org.perso.bikerbox.data.repository.firebase.FirebaseAuthRepository
import org.perso.bikerbox.data.repository.firebase.FirebaseLockersRepository

class BikerBoxApplication : Application() {
    val locationProvider: LocationProvider by lazy {
        LocationProvider(this)
    }

    lateinit var authRepository: AuthRepository
        private set

    override fun onCreate() {
        super.onCreate()
        // 1. Initialiser Firebase
        FirebaseApp.initializeApp(this)

        // 2. Initialiser nos Repositories qui dépendent de Firebase
        authRepository = FirebaseAuthRepository()
        LockersProvider.initialize(FirebaseLockersRepository())

        // 3. Initialiser MapLibre
        MapLibre.getInstance(this)
    }
}
