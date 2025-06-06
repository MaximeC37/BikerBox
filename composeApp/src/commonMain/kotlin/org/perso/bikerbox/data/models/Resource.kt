package org.perso.bikerbox.data.models

sealed class Resource<out T> {
    data class Success<out T>(val data: T) : Resource<T>()
    data class Error(val message: String, val exception: Exception? = null) : Resource<Nothing>()
    data object Loading : Resource<Nothing>()
}
