package org.perso.bikerbox.ui.viewmodel

import kotlinx.datetime.LocalDateTime
import org.perso.bikerbox.data.models.Locker
import org.perso.bikerbox.data.models.LockerSize
import org.perso.bikerbox.data.models.Reservation

sealed class ReservationState {
    object Loading : ReservationState()

    data class Error(val message: String) : ReservationState()

    data class LockerSelection(val lockers: List<Locker>) : ReservationState()

    data class SizeSelection(val locker: Locker) : ReservationState()

    data class DateSelection(
        val lockerId: String,
        val lockerName: String,
        val size: LockerSize
    ) : ReservationState()

    data class ConfirmationNeeded(
        val lockerId: String,
        val lockerName: String,
        val size: LockerSize,
        val startDate: LocalDateTime,
        val endDate: LocalDateTime,
        val price: Double
    ) : ReservationState()

    data class Success(val reservation: Reservation) : ReservationState()
}

