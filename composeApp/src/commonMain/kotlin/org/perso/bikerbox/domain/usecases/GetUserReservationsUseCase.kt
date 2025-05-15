package org.perso.bikerbox.domain.usecases

import org.perso.bikerbox.data.models.Reservation
import org.perso.bikerbox.data.repository.LockersRepository

class GetUserReservationsUseCase(private val repository: LockersRepository) {
    suspend operator fun invoke(): List<Reservation> {
        return repository.getUserReservations()
    }
}

class CancelReservationUseCase(private val repository: LockersRepository) {
    suspend operator fun invoke(reservationId: String) {
        return repository.cancelReservation(reservationId)
    }
}
