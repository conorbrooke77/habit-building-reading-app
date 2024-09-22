package com.example.bookbyte.common.domain.Usecase

import com.example.bookbyte.common.domain.Exceptions.DomainException
import com.example.bookbyte.common.domain.Exceptions.UnknownDomainException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class UseCaseExecutor(private val coroutineScope: CoroutineScope) {
    fun <OUTPUT> execute(useCase: UseCase<Unit, OUTPUT>, onSuccess: (OUTPUT) -> Unit = {}, onException: (DomainException) -> Unit = {}) {
        execute(useCase, Unit, onSuccess, onException)
    }

    fun <INPUT, OUTPUT> execute(useCase: UseCase<INPUT, OUTPUT>, value: INPUT, onSuccess: (OUTPUT) -> Unit = {}, onException: (DomainException) -> Unit = {}) {
        coroutineScope.launch {
            try {
                useCase.execute(value, onSuccess)
            } //catch (ignore: CancellationException) {  } Todo: Add a cancellationException
            catch (throwable: Throwable) {
                // The as? operator performs a safe cast.
                // If throwable is of type DomainException, it successfully casts it and returns the result.
                // If it's not of type DomainException, the cast fails, and null is returned
                onException((throwable as? DomainException) ?: UnknownDomainException(throwable))
            }
        }
    }
}