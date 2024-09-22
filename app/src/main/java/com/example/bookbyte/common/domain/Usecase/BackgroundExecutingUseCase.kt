package com.example.bookbyte.common.domain.Usecase

import com.example.bookbyte.common.domain.coroutine.CoroutineContextProvider
import kotlinx.coroutines.withContext

abstract class BackgroundExecutingUseCase<INPUT, RESULT>(private val coroutineContextProvider: CoroutineContextProvider):
    UseCase<INPUT, RESULT> {

    // Called when a UseCase inherits BackgroundExecutingUseCase and gets instantiated.
    final override suspend fun execute(input: INPUT, onResult: (RESULT) -> Unit) {

        val result = withContext(coroutineContextProvider.io) {
                executeInBackground(input)
        }

        onResult(result)
    }

    // This function is overridden in the concrete UseCase with whatever Input and Result is required.
    abstract fun executeInBackground(input: INPUT): RESULT
}