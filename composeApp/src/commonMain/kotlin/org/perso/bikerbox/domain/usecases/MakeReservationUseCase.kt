package org.perso.bikerbox.domain.usecases

import kotlinx.datetime.LocalDateTime
import org.perso.bikerbox.data.models.LockerSize
import org.perso.bikerbox.data.models.Reservation
import org.perso.bikerbox.data.repository.LockersRepository

class MakeReservationUseCase(
    private val lockersRepository: LockersRepository
) {
    suspend operator fun invoke(
        lockerId: String,
        size: LockerSize,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Reservation {
        return lockersRepository.makeReservation(lockerId, size, startDate, endDate)
    }
}
