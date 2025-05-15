package org.perso.bikerbox.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.perso.bikerbox.data.models.Resource
import org.perso.bikerbox.data.models.User
import org.perso.bikerbox.data.repository.AuthRepository

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    // État d'authentification
    private val _authState = MutableStateFlow<Resource<User?>>(Resource.Loading)
    val authState: StateFlow<Resource<User?>> = _authState.asStateFlow()

    // État des opérations d'authentification
    private val _authOperation = MutableStateFlow<Resource<Unit>?>(null)
    val authOperation: StateFlow<Resource<Unit>?> = _authOperation.asStateFlow()

    init {
        observeAuthState()
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            authRepository.currentUser.collectLatest { user ->
                _authState.value = Resource.Success(user)
            }
        }
    }

    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            _authOperation.value = Resource.Loading
            when (val result = authRepository.signUp(email, password)) {
                is Resource.Success -> _authOperation.value = Resource.Success(Unit)
                is Resource.Error -> _authOperation.value = Resource.Error(result.message, result.exception)
                Resource.Loading -> { /* Déjà géré au début */ }
            }
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _authOperation.value = Resource.Loading
            when (val result = authRepository.signIn(email, password)) {
                is Resource.Success -> _authOperation.value = Resource.Success(Unit)
                is Resource.Error -> _authOperation.value = Resource.Error(result.message, result.exception)
                Resource.Loading -> { /* Déjà géré au début */ }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            _authOperation.value = Resource.Loading
            _authOperation.value = authRepository.signOut()
        }
    }

    fun sendPasswordReset(email: String) {
        viewModelScope.launch {
            _authOperation.value = Resource.Loading
            _authOperation.value = authRepository.sendPasswordReset(email)
        }
    }

    fun resetOperationState() {
        _authOperation.value = null
    }
    fun updateUserProfile(displayName: String? = null, phoneNumber: String? = null) {
        viewModelScope.launch {
            _authOperation.value = Resource.Loading
            _authOperation.value = Resource.Success(Unit)
        }
    }
}
