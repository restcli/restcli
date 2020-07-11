package uos.dev.restcli.parser

enum class RequestMethod {
    GET,
    HEAD,
    POST,
    PUT,
    DELETE,
    CONNECT,
    PATCH,
    OPTIONS,
    TRACE;

    companion object {
        fun from(value: String, ignoreCase: Boolean = true): RequestMethod? =
            values().firstOrNull { it.name.equals(value, ignoreCase) }
    }
}
