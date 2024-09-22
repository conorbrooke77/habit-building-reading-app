package com.example.bookbyte.common.domain.coroutine

import kotlin.coroutines.CoroutineContext

interface CoroutineContextProvider {
    val main: CoroutineContext
    val io: CoroutineContext
}