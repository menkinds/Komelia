package snd.komelia.ui.error

fun formatExceptionMessage(exception: Throwable, addCause: Boolean = true): String {
    return buildErrorMessage(exception, addCause)
}

private fun buildErrorMessage(exception: Throwable, addCause: Boolean): String {
    return buildString {
        exception::class.simpleName?.let { append("$it: ") }
        exception.message?.let { append("$it; ") }
        if (addCause) {
            var cause = exception.cause
            while (cause != null) {
                cause.message?.let { append("$it;") }
                cause = cause.cause
            }
        }
    }
}
