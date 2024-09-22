package com.example.bookbyte.common.domain.Exceptions

// All domain related exceptions inherit from this class.
abstract class DomainException(throwable: Throwable): Exception(throwable) {
    // Takes a message in the constructor and uses it to instantiate a Exception object using : this()
    constructor(message: String) : this(Exception(message))
    constructor(message: String, throwable: Throwable) : this(Exception(message, throwable))
}