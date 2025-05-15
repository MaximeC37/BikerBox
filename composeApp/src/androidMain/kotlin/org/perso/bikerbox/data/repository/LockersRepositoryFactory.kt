package org.perso.bikerbox.data.repository

import org.perso.bikerbox.data.repository.firebase.FirebaseLockersRepository

object LockersRepositoryFactoryImpl : LockersRepositoryFactory {
    override fun createRepository(): LockersRepository {
        return FirebaseLockersRepository()
    }
}
