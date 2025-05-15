package org.perso.bikerbox.data.models

import kotlinx.datetime.Clock

data class User(
    val id: String,
    val email: String,
    val displayName: String? = null,
    val phoneNumber: String? = null,
    val profileImageUrl: String? = null,
    val registrationDate: Long = Clock.System.now().toEpochMilliseconds()
)