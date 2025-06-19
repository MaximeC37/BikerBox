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
            availableSizes = listOf(LockerSize.SINGLE, LockerSize.DOUBLE),
            availableCount = mutableMapOf(
                LockerSize.SINGLE to 10,
                LockerSize.DOUBLE to 7
            )
        ),
        Locker(
            id = "locker_2",
            name = "Market Square Locker",
            location = "Market Square",
            availableSizes = listOf(LockerSize.SINGLE, LockerSize.DOUBLE),
            availableCount = mutableMapOf(
                LockerSize.SINGLE to 8,
                LockerSize.DOUBLE to 4
            )
        )
    )

    private val fakeReservations = mutableListOf<Reservation>()

    override suspend fun getAvailableLockers(): List<Locker> {
        Log.d("LockersRepositoryImpl", "🔥 getAvailableLockers called")
        return fakeLockers
    }

    override suspend fun getLockerById(id: String): Locker? {
        Log.d("LockersRepositoryImpl", "🔥 getLockerById: $id")
        return fakeLockers.find { it.id == id }
    }

    override suspend fun getAvailability(
        lockerId: String,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Map<LockerSize, Int> {
        Log.d("LockersRepositoryImpl", "🔥 getAvailability for: $lockerId")

        val locker = getLockerById(lockerId)
        return if (locker != null) {
            locker.availableSizes.associateWith { size ->
                when (size) {
                    LockerSize.SINGLE -> 5
                    LockerSize.DOUBLE -> 3
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
        Log.d("LockersRepositoryImpl", "🔥 makeReservation: lockerId=$lockerId, size=$size")

        val locker = getLockerById(lockerId)
            ?: throw Exception("Locker not found")

        // 🎯 USE YOUR EXISTING PRICING LOGIC!
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

        Log.d("LockersRepositoryImpl", "✅ Reservation created: ${reservation.id}, price: $price€")
        Log.d("LockersRepositoryImpl", "🔥 Total reservations: ${fakeReservations.size}")

        return reservation
    }

    override suspend fun checkAvailability(
        lockerId: String,
        size: LockerSize,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Boolean {
        Log.d("LockersRepositoryImpl", "🔥 checkAvailability")
        val locker = getLockerById(lockerId)
        return locker?.availableSizes?.contains(size) ?: false
    }

    override suspend fun getUserReservations(): List<Reservation> {
        Log.d("LockersRepositoryImpl", "🔥 getUserReservations: ${fakeReservations.size} reservations")
        fakeReservations.forEachIndexed { index, reservation ->
            Log.d("LockersRepositoryImpl", "🔥 Reservation $index: ${reservation.id} - ${reservation.lockerName}")
        }
        return fakeReservations.toList()
    }

    override suspend fun cancelReservation(reservationId: String) {
        Log.d("LockersRepositoryImpl", "🔥 START cancelReservation: $reservationId")

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