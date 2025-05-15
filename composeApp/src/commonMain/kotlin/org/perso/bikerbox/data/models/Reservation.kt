package org.perso.bikerbox.data.models

import kotlinx.datetime.LocalDateTime


data class Reservation(
    val id: String,
    val lockerId: String,
    val lockerName: String,
    val size: LockerSize,
    val startDate: LocalDateTime,
    val endDate: LocalDateTime,
    val status: String,
    val code: String,
    val price: Double

)


