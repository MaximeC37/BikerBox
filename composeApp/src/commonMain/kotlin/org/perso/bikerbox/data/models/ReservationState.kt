package org.perso.bikerbox.data.models

import kotlinx.datetime.LocalDateTime

sealed class ReservationState {
    object Loading : ReservationState()

    data class Error(val message: String) : ReservationState()

    data class LockerSelection(val availableLockers: List<Locker>) : ReservationState()

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
}