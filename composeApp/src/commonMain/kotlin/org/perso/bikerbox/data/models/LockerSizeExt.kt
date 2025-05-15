package org.perso.bikerbox.data.models

val LockerSize.displayName: String
    get() = when(this) {
        LockerSize.SINGLE -> "Simple"
        LockerSize.DOUBLE -> "Double"
    }

val LockerSize.description: String
    get() = when(this) {
        LockerSize.SINGLE -> "Pour un vélo standard"
        LockerSize.DOUBLE -> "Pour deux vélos ou un vélo cargo"
    }

val LockerSize.basePricePerDay: Double
    get() = when(this) {
        LockerSize.SINGLE -> 6.0
        LockerSize.DOUBLE -> 10.0
    }
val LockerSize.hourlyRate: Double
    get() = when(this) {
        LockerSize.SINGLE -> 6.0 / 24

        LockerSize.DOUBLE -> 10.0 / 24
    }
