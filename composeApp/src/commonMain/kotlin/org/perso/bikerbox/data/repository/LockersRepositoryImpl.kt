package org.perso.bikerbox.data.repository

import android.util.Log
import kotlinx.datetime.LocalDateTime
import org.perso.bikerbox.data.models.*
import org.perso.bikerbox.data.services.PricingService
import kotlin.random.Random

class LockersRepositoryImpl : LockersRepository {
    private val fakeLockers = listOf(
        Locker(
            id = "locker_1",
            name = "Central Station Locker",
            location = "Station Square",
            availableSizes = listOf(LockerSize.SMALL, LockerSize.MEDIUM),
            availableCount = mutableMapOf(
                LockerSize.SMALL to 10,
                LockerSize.MEDIUM to 7,
                LockerSize.LARGE to 1
            )
        ),
        Locker(
            id = "locker_2",
            name = "Market Square Locker",
            location = "Market Square",
            availableSizes = listOf(LockerSize.SMALL, LockerSize.MEDIUM),
            availableCount = mutableMapOf(
                LockerSize.SMALL to 8,
                LockerSize.MEDIUM to 4,
                LockerSize.LARGE to 1
            )
        )
    )

    private val fakeReservations = mutableListOf<Reservation>()

    override suspend fun getAvailableLockers(): List<Locker> {
        return fakeLockers
    }

    override suspend fun getLockerById(id: String): Locker? {
        return fakeLockers.find { it.id == id }
    }

    override suspend fun getAvailability(
        lockerId: String,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Map<LockerSize, Int> {

        val locker = getLockerById(lockerId)
        return if (locker != null) {
            locker.availableSizes.associateWith { size ->
                when (size) {
                    LockerSize.SMALL -> 5
                    LockerSize.MEDIUM -> 3
                    LockerSize.LARGE -> 1
                }
            }
        } else {
            emptyMap()
        }
    }

    override suspend fun makeReservation(
        lockerId: String,
        size: LockerSize,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Reservation {

        val locker = getLockerById(lockerId)
            ?: throw Exception("Locker not found")

        val price = PricingService.calculatePrice(size, startDate, endDate)

        val reservation = Reservation(
            id = "res_${System.currentTimeMillis()}_${Random.nextInt(1000)}",
            lockerId = lockerId,
            lockerName = locker.name,
            size = size,
            startDate = startDate,
            endDate = endDate,
            status = "CONFIRMED",
            code = generateCode(),
            price = price
        )

        fakeReservations.add(reservation)

        return reservation
    }

    override suspend fun checkAvailability(
        lockerId: String,
        size: LockerSize,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Boolean {
        val locker = getLockerById(lockerId)
        return locker?.availableSizes?.contains(size) ?: false
    }

    override suspend fun getUserReservations(): List<Reservation> {
        return fakeReservations.toList()
    }

    override suspend fun cancelReservation(reservationId: String) {

        val sizeBefore = fakeReservations.size
        val removed = fakeReservations.removeAll { it.id == reservationId }
        val sizeAfter = fakeReservations.size

        if (removed) {
            Log.d("LockersRepositoryImpl", "✅ Reservation deleted! Before: $sizeBefore, After: $sizeAfter")
        } else {
            Log.e("LockersRepositoryImpl", "❌ Reservation not found: $reservationId")
            throw Exception("Reservation not found")
        }
    }

    private fun generateCode(): String {
        return (1000..9999).random().toString()
    }
}