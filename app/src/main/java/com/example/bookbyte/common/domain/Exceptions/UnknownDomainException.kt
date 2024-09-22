package com.example.bookbyte.common.domain.Exceptions

class UnknownDomainException(throwable: Throwable): DomainException(throwable) {
    constructor(errorMessage: String) : this(Throwable(errorMessage))
}