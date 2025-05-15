package org.perso.bikerbox.data.repository


import kotlinx.coroutines.flow.firstOrNull
import kotlinx.datetime.LocalDateTime
import org.perso.bikerbox.data.models.Locker
import org.perso.bikerbox.data.models.LockerSize
import org.perso.bikerbox.data.models.Reservation
import org.perso.bikerbox.data.services.PricingService.Companion.calculatePrice
class MockLockersRepository : LockersRepository {
    private var authRepository: AuthRepository? = null

    // Méthode pour définir le AuthRepository
    fun setAuthRepository(repo: AuthRepository) {
        authRepository = repo
    }
    private val lockers = mutableListOf(
        Locker(
            id = "locker1",
            name = "Centre Commercial",
            location = "Niveau 2, près de l'entrée principale",
            availableSizes = listOf(LockerSize.SINGLE, LockerSize.DOUBLE),
            availableCount = mutableMapOf(
                LockerSize.SINGLE to 5,
                LockerSize.DOUBLE to 3
            )
        ),
        Locker(
            id = "locker2",
            name = "Gare Centrale",
            location = "Hall principal, à côté des toilettes",
            availableSizes = listOf(LockerSize.SINGLE),
            availableCount = mutableMapOf(
                LockerSize.SINGLE to 8
            )
        ),
        Locker(
            id = "locker3",
            name = "Bibliothèque Municipale",
            location = "Rez-de-chaussée, près de l'accueil",
            availableSizes = listOf(LockerSize.SINGLE, LockerSize.DOUBLE),
            availableCount = mutableMapOf(
                LockerSize.SINGLE to 2,
                LockerSize.DOUBLE to 4
            )
        )
    )

    private val reservations = mutableListOf<Reservation>()

    override suspend fun getAvailableLockers(): List<Locker> {
        return lockers.filter { locker ->
            locker.availableCount.any { (_, count) -> count > 0 }
        }
    }

    override suspend fun getLockerById(id: String): Locker? {
        return lockers.find { it.id == id }
    }

    override suspend fun getAvailability(
        lockerId: String,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Map<LockerSize, Int> {
        TODO("Not yet implemented")
    }

    override suspend fun checkAvailability(lockerId: String, size: LockerSize, startDate: LocalDateTime, endDate: LocalDateTime): Boolean {
        val locker = getLockerById(lockerId) ?: return false

        // Vérifier si le casier a cette taille disponible
        val availableCount = locker.availableCount[size] ?: 0
        if (availableCount <= 0) return false

        // Vérifier si d'autres réservations n'entrent pas en conflit
        val overlappingReservations = reservations.count { reservation ->
            reservation.lockerId == lockerId &&
                    reservation.size == size &&
                    !((reservation.endDate < startDate) || (reservation.startDate > endDate))
        }

        // S'il y a moins de réservations qui se chevauchent que de casiers disponibles, alors c'est disponible
        return overlappingReservations < availableCount
    }

    override suspend fun makeReservation(lockerId: String, size: LockerSize, startDate: LocalDateTime, endDate: LocalDateTime): Reservation {
        // Vérification de la disponibilité comme avant...

        val userId = getCurrentUserId() ?: "guest-user" // Utiliser "guest-user" si non connecté
        val locker = getLockerById(lockerId)
        val lockerName = locker?.name ?: "Casier inconnu"

        // Créer et stocker la réservation
        val reservation = Reservation(
            id = "generateUUID()",
            lockerId = lockerId,
            lockerName = lockerName,
            size = size,
            startDate = startDate,
            endDate = endDate,
            status = "CONFIRMED",
            price = calculatePrice(size, startDate, endDate),
            code = generateRandomCode(),
        )

        reservations.add(reservation)
        reservationUserMap[reservation.id] = userId

        return reservation
    }

    private val reservationUserMap = mutableMapOf<String, String>()

    override suspend fun getUserReservations(): List<Reservation> {
        val currentUserId = getCurrentUserId()

        return if (currentUserId != null) {
            // Utilisez "it" comme paramètre implicite ou un nom de paramètre explicite
            reservations.filter { reservation ->
                reservationUserMap[reservation.id] == currentUserId
            }
        } else {
            reservations
        }
    }


    override suspend fun cancelReservation(reservationId: String) {
        val reservationIndex = reservations.indexOfFirst { it.id == reservationId }
        if (reservationIndex != -1) {
            reservations.removeAt(reservationIndex)
        } else {
            throw IllegalArgumentException("Réservation non trouvée")
        }
    }
    // Méthode d'assistance pour obtenir l'ID de l'utilisateur actuel
    private suspend fun getCurrentUserId(): String? {
        return authRepository?.currentUser?.firstOrNull()?.id
    }


    private fun generateRandomCode(): String {
        return (1000..9999).random().toString()
    }
}
