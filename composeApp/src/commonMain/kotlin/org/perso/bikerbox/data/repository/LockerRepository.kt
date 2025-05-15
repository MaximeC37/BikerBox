package org.perso.bikerbox.data.repository

import kotlinx.datetime.LocalDateTime
import org.perso.bikerbox.data.models.Locker
import org.perso.bikerbox.data.models.LockerSize
import org.perso.bikerbox.data.models.Reservation

interface LockersRepository {
    suspend fun getAvailableLockers(): List<Locker>
    suspend fun getLockerById(id: String): Locker?
    suspend fun getAvailability(lockerId: String, startDate: LocalDateTime, endDate: LocalDateTime): Map<LockerSize, Int>
    suspend fun makeReservation(lockerId: String, size: LockerSize, startDate: LocalDateTime, endDate: LocalDateTime): Reservation
    suspend fun checkAvailability(lockerId: String, size: LockerSize, startDate: LocalDateTime, endDate: LocalDateTime): Boolean
    suspend fun getUserReservations(): List<Reservation>
    suspend fun cancelReservation(reservationId: String)
}

interface LockersRepositoryFactory {
    fun createRepository(): LockersRepository
}


