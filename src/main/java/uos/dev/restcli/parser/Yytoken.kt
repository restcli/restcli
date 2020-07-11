package uos.dev.restcli.parser

data class Yytoken @JvmOverloads constructor(
    val type: Int,
    val value: Any? = null
) {
    companion object {
        const val TYPE_VALUE: Int = 0
        const val TYPE_SEPARATOR = 1
        const val TYPE_BLANK: Int = 2
        const val TYPE_PART: Int = 3
        const val TYPE_OPEN_SCRIPT_HANDLER: Int = 4
        const val TYPE_CLOSE_SCRIPT_HANDLER: Int = 5
        const val TYPE_RESPONSE_REFERENCE: Int = 6
        const val TYPE_COMMENT: Int = 7
        const val TYPE_EOF: Int = -1
    }
}
