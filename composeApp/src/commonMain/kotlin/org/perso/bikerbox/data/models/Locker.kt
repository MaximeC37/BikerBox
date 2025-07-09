package org.perso.bikerbox.data.models

data class Locker(
    val id: String,
    val name: String,
    val location: String,
    val availableSizes: List<LockerSize>,
    val availableCount: MutableMap<LockerSize, Int>,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)

