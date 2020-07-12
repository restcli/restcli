package uos.dev.restcli.parser

data class Yytoken @JvmOverloads constructor(
    val type: Int,
    val value: Any? = null
) {
    companion object {
        const val TYPE_VALUE: Int = 0
        const val TYPE_VALUE_FILE_REF: Int = 1
        const val TYPE_REQUEST_METHOD = 10
        const val TYPE_REQUEST_HTTP_VERSION = 20
        const val TYPE_SEPARATOR = 30
        const val TYPE_BLANK: Int = 40
        const val TYPE_PART: Int = 50
        const val TYPE_OPEN_SCRIPT_HANDLER: Int = 60
        const val TYPE_CLOSE_SCRIPT_HANDLER: Int = 70
        const val TYPE_RESPONSE_REFERENCE: Int = 80
        const val TYPE_COMMENT: Int = 90
        const val TYPE_EOF: Int = -1
    }
}
