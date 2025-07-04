package org.perso.bikerbox.data.repository


import kotlinx.coroutines.flow.firstOrNull
import kotlinx.datetime.LocalDateTime
import org.perso.bikerbox.data.models.Locker
import org.perso.bikerbox.data.models.LockerSize
import org.perso.bikerbox.data.models.Reservation
import org.perso.bikerbox.data.services.PricingService.Companion.calculatePrice
class MockLockersRepository : LockersRepository {
    private var authRepository: AuthRepository? = null

    fun setAuthRepository(repo: AuthRepository) {
        authRepository = repo
    }
    private val lockers = mutableListOf(
        Locker(
            id = "locker1",
            name = "Centre Commercial",
            location = "Niveau 2, près de l'entrée principale",
            availableSizes = listOf(LockerSize.SMALL, LockerSize.MEDIUM, LockerSize.LARGE),
            availableCount = mutableMapOf(
                LockerSize.SMALL to 5,
                LockerSize.MEDIUM to 3,
                LockerSize.LARGE to 1
            )
        ),
        Locker(
            id = "locker2",
            name = "Gare Centrale",
            location = "Hall principal, à côté des toilettes",
            availableSizes = listOf(LockerSize.SMALL),
            availableCount = mutableMapOf(
                LockerSize.SMALL to 8
            )
        ),
        Locker(
            id = "locker3",
            name = "Bibliothèque Municipale",
            location = "Rez-de-chaussée, près de l'accueil",
            availableSizes = listOf(LockerSize.SMALL, LockerSize.MEDIUM),
            availableCount = mutableMapOf(
                LockerSize.SMALL to 2,
                LockerSize.MEDIUM to 4,
                LockerSize.LARGE to 1
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

        val availableCount = locker.availableCount[size] ?: 0
        if (availableCount <= 0) return false

        val overlappingReservations = reservations.count { reservation ->
            reservation.lockerId == lockerId &&
                    reservation.size == size &&
                    !((reservation.endDate < startDate) || (reservation.startDate > endDate))
        }

        return overlappingReservations < availableCount
    }

    override suspend fun makeReservation(lockerId: String, size: LockerSize, startDate: LocalDateTime, endDate: LocalDateTime): Reservation {

        val userId = getCurrentUserId() ?: "guest-user"
        val locker = getLockerById(lockerId)
        val lockerName = locker?.name ?: "Casier inconnu"

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
    private suspend fun getCurrentUserId(): String? {
        return authRepository?.currentUser?.firstOrNull()?.id
    }


    private fun generateRandomCode(): String {
        return (1000..9999).random().toString()
    }
}
