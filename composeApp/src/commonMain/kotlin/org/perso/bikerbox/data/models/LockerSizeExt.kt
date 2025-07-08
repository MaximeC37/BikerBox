package org.perso.bikerbox.data.models

import androidx.compose.runtime.Composable
import bikerbox.composeapp.generated.resources.Large
import bikerbox.composeapp.generated.resources.Medium
import bikerbox.composeapp.generated.resources.Res
import bikerbox.composeapp.generated.resources.Small
import org.jetbrains.compose.resources.stringResource

val LockerSize.displayName: String
    @Composable
    get() = when(this) {
        LockerSize.SMALL -> stringResource(Res.string.Small)
        LockerSize.MEDIUM -> stringResource(Res.string.Medium)
        LockerSize.LARGE -> stringResource(Res.string.Large)
    }

val LockerSize.basePricePerDay: Double
    get() = when(this) {
        LockerSize.SMALL -> 6.0
        LockerSize.MEDIUM -> 10.0
        LockerSize.LARGE -> 14.0
    }
