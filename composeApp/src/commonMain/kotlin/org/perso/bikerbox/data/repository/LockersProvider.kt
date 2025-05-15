package org.perso.bikerbox.data.repository

object LockersProvider {
    // Cette propriété sera initialisée par la plateforme spécifique
    private var _repository: LockersRepository? = null

    fun initialize(repository: LockersRepository) {
        _repository = repository
    }

    val repository: LockersRepository
        get() = _repository ?: throw IllegalStateException("LockersRepository non initialisé")
}