package uos.dev.restcli.parser

import java.io.Reader

class HttpRequestParser {
    private val lexer: Yylex = Yylex(null)

    private fun nextToken(): Yytoken? = lexer.yylex()

    private fun reset(input: Reader) {
        lexer.yyreset(input)
    }

    fun parse(input: Reader): List<Request> {
        reset(input)
        val result = mutableListOf<Request>()
        var builder = Request.Builder()
        var headerName: String? = null

        fun buildRequestAndMakeBuilderNew() {
            builder.build()?.let { result.add(it) }
            builder = Request.Builder()
        }

        var isFirstTimeInRequestLine = true
        while (true) {
            val token = nextToken() ?: break
            when (lexer.yystate()) {
                Yylex.YYINITIAL -> buildRequestAndMakeBuilderNew()
                Yylex.S_REQUEST_SEPARATOR -> {
                    isFirstTimeInRequestLine = true
                    buildRequestAndMakeBuilderNew()
                }
                Yylex.S_REQUEST_LINE -> {
                    if (isFirstTimeInRequestLine) {
                        buildRequestAndMakeBuilderNew()
                        isFirstTimeInRequestLine = false
                    }
                }
                Yylex.S_HEADER -> Unit
                Yylex.S_BODY -> Unit
                Yylex.S_SCRIPT_HANDLER -> Unit
                Yylex.S_SCRIPT_REFERENCE -> Unit
                Yylex.S_MULTIPLE_PART_HEADER -> Unit
                Yylex.S_MULTIPLE_PART_BODY -> Unit
            }

            when (token.type) {
                Yytoken.TYPE_VALUE_FILE_REF -> Unit
                Yytoken.TYPE_REQUEST_METHOD -> builder.method = RequestMethod.from(token.value)
                Yytoken.TYPE_REQUEST_TARGET -> builder.requestTarget = token.value
                Yytoken.TYPE_REQUEST_HTTP_VERSION -> builder.httpVersion = token.value
                Yytoken.TYPE_FIELD_NAME -> {
                    if (headerName != null) {
                        throw IllegalStateException("Header name already exist($headerName). Expect header value")
                    }
                    headerName = token.value
                }
                Yytoken.TYPE_FIELD_VALUE -> {
                    val nonNullHeaderName = headerName
                        ?: throw IllegalStateException("Header name is null, but got header value ${token.value}")
                    builder.headers[nonNullHeaderName] = token.value
                    headerName = null
                }
                Yytoken.TYPE_BODY_MESSAGE -> builder.rawBody.add(token.value)
                Yytoken.TYPE_SEPARATOR -> Unit
                Yytoken.TYPE_BLANK -> Unit
                Yytoken.TYPE_PART -> Unit
                Yytoken.TYPE_OPEN_SCRIPT_HANDLER -> Unit
                Yytoken.TYPE_CLOSE_SCRIPT_HANDLER -> Unit
                Yytoken.TYPE_RESPONSE_REFERENCE -> Unit
                Yytoken.TYPE_COMMENT -> Unit
            }
        }
        return result
    }
}
