package com.example.bookbyte.common.utils

sealed class Resource<T>(val data: T? = null, val message: String? = null) {
    class Loading<T>(data: T? = null) : Resource<T>(data)  // Represents the loading state
    class Success<T>(data: T) : Resource<T>(data)  // Success state with required data
    class Error<T>(data: T? = null, message: String) : Resource<T>(data, message)  // Error with required message and optional data
}