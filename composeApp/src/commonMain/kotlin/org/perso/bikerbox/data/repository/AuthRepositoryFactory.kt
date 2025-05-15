package org.perso.bikerbox.data.repository

expect object AuthRepositoryFactory {
    fun createRepository(): AuthRepository
}
