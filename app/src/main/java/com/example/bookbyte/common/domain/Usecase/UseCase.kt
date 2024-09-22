package com.example.bookbyte.common.domain.Usecase

interface UseCase<INPUT, RESULT> {
    suspend fun execute(input: INPUT, onResult: (RESULT) -> Unit)
}