package org.perso.bikerbox

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform