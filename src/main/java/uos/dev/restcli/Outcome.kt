package uos.dev.restcli

sealed class Outcome<out T> {
    data class Success<T>(val data: T) : Outcome<T>()
    data class Error(val exception: Throwable) : Outcome<Nothing>()
}

fun <T> runSafe(action: () -> T): Outcome<T> =
    try {
        Outcome.Success(action())
    } catch (e: Throwable) {
        Outcome.Error(e)
    }
