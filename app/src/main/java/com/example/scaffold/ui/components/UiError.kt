package com.example.scaffold.ui.components

import androidx.annotation.StringRes
import com.example.scaffold.R
import retrofit2.HttpException
import java.io.IOException

/**
 * The failure kinds a screen knows how to talk about, each carrying the string resource that
 * explains it. A UiState never carries an exception's own `message` — "Unable to resolve host
 * www.themealdb.com" is a log line, not something a user can act on, and it can't be localised.
 */
sealed interface UiError {
    @get:StringRes
    val messageRes: Int

    /** No usable connection, or the request never reached the server. */
    data object Network : UiError {
        override val messageRes: Int = R.string.error_network
    }

    /** The server answered, but with a failure status. */
    data object Server : UiError {
        override val messageRes: Int = R.string.error_server
    }

    /** Anything else — a parse failure, a bug of ours. */
    data object Unknown : UiError {
        override val messageRes: Int = R.string.error_unknown
    }
}

fun Throwable.toUiError(): UiError =
    when (this) {
        is IOException -> UiError.Network
        is HttpException -> UiError.Server
        else -> UiError.Unknown
    }
