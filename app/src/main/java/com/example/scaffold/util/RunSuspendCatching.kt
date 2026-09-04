package com.example.scaffold.util

import kotlin.coroutines.cancellation.CancellationException

/**
 * [runCatching] for code that suspends: identical, except a [CancellationException] is rethrown
 * rather than folded into a `Result.failure`.
 *
 * Catching cancellation breaks structured concurrency — a coroutine cancelled by its parent (a
 * superseded refresh job, a ViewModel being cleared) would go on to report the cancellation as
 * if it were a failure of its own work, e.g. by showing the user an error for a request nobody
 * is waiting for any more.
 */
@Suppress("TooGenericExceptionCaught")
inline fun <T> runSuspendCatching(block: () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (throwable: Throwable) {
        Result.failure(throwable)
    }
