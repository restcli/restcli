package uos.dev.restcli.parser

data class Yytoken @JvmOverloads constructor(
    val type: Int,
    val value: String = ""
) {
    override fun toString(): String {
        val typeString = when (type) {
            TYPE_VALUE_FILE_REF -> "TYPE_VALUE_FILE_REF"
            TYPE_REQUEST_METHOD -> "TYPE_REQUEST_METHOD"
            TYPE_REQUEST_TARGET -> "TYPE_REQUEST_TARGET"
            TYPE_REQUEST_HTTP_VERSION -> "TYPE_REQUEST_HTTP_VERSION"
            TYPE_FIELD_NAME -> "TYPE_FIELD_NAME"
            TYPE_FIELD_VALUE -> "TYPE_FIELD_VALUE"
            TYPE_BODY_MESSAGE -> "TYPE_BODY_MESSAGE"
            TYPE_SEPARATOR -> "TYPE_SEPARATOR"
            TYPE_BLANK -> "TYPE_BLANK"
            TYPE_OPEN_SCRIPT_HANDLER -> "TYPE_OPEN_SCRIPT_HANDLER"
            TYPE_CLOSE_SCRIPT_HANDLER -> "TYPE_CLOSE_SCRIPT_HANDLER"
            TYPE_HANDLER_SCRIPT -> "TYPE_HANDLER_SCRIPT"
            TYPE_RESPONSE_REFERENCE -> "TYPE_RESPONSE_REFERENCE"
            TYPE_COMMENT -> "TYPE_COMMENT"
            else -> "Unknown"
        }
        return "$typeString($value)"
    }

    companion object {
        const val TYPE_VALUE_FILE_REF: Int = 1
        const val TYPE_REQUEST_METHOD = 10
        const val TYPE_REQUEST_TARGET = 11
        const val TYPE_REQUEST_HTTP_VERSION = 20
        const val TYPE_FIELD_NAME = 21
        const val TYPE_FIELD_VALUE = 22
        const val TYPE_BODY_MESSAGE = 23
        const val TYPE_SEPARATOR = 30
        const val TYPE_BLANK: Int = 40
        const val TYPE_OPEN_SCRIPT_HANDLER: Int = 60
        const val TYPE_CLOSE_SCRIPT_HANDLER: Int = 70
        const val TYPE_HANDLER_SCRIPT: Int = 71
        const val TYPE_RESPONSE_REFERENCE: Int = 80
        const val TYPE_COMMENT: Int = 90
    }
}
