package org.perso.bikerbox.data.models

val LockerSize.displayName: String
    get() = when(this) {
        LockerSize.SMALL -> "Simple"
        LockerSize.MEDIUM -> "Double"
        LockerSize.LARGE -> "Très grand"
    }

val LockerSize.basePricePerDay: Double
    get() = when(this) {
        LockerSize.SMALL -> 6.0
        LockerSize.MEDIUM -> 10.0
        LockerSize.LARGE -> 14.0
    }
val LockerSize.hourlyRate: Double
    get() = when(this) {
        LockerSize.SMALL -> 6.0 / 24
        LockerSize.MEDIUM -> 10.0 / 24
        LockerSize.LARGE -> 14.0 / 24
    }
