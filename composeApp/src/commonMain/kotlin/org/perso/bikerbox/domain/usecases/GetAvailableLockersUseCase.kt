package org.perso.bikerbox.domain.usecases

import android.util.Log
import org.perso.bikerbox.data.models.Locker
import org.perso.bikerbox.data.repository.LockersRepository

class GetAvailableLockersUseCase(
    private val lockersRepository: LockersRepository
) {
    suspend operator fun invoke(): List<Locker> {
        Log.d("GetAvailableLockersUseCase", "Appel de la méthode invoke()")
        val lockers = lockersRepository.getAvailableLockers()
        Log.d("GetAvailableLockersUseCase", "Casiers récupérés: ${lockers.size}")
        // Loguer les détails de chaque casier pour vérifier
        lockers.forEachIndexed { index, locker ->
            Log.d("GetAvailableLockersUseCase", "Casier $index: ${locker.name}, ${locker.location}, tailles: ${locker.availableSizes}")
        }
        return lockers
    }
}
