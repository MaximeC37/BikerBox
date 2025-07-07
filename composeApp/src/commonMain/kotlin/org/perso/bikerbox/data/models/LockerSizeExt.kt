package org.perso.bikerbox.data.models

val LockerSize.displayName: String
    get() = when(this) {
        LockerSize.SMALL -> "Small"
        LockerSize.MEDIUM -> "Double"
        LockerSize.LARGE -> "Large"
    }

val LockerSize.basePricePerDay: Double
    get() = when(this) {
        LockerSize.SMALL -> 6.0
        LockerSize.MEDIUM -> 10.0
        LockerSize.LARGE -> 14.0
    }
