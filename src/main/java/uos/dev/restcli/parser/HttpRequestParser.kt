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
                TokenType.TYPE_VALUE_FILE_REF -> Unit
                TokenType.TYPE_REQUEST_METHOD -> builder.method = RequestMethod.from(token.value)
                TokenType.TYPE_REQUEST_TARGET -> builder.requestTarget = token.value
                TokenType.TYPE_REQUEST_HTTP_VERSION -> builder.httpVersion = token.value
                TokenType.TYPE_FIELD_NAME -> {
                    if (headerName != null) {
                        throw IllegalStateException("Header name exist($headerName). Expect header value")
                    }
                    headerName = token.value
                }
                TokenType.TYPE_FIELD_VALUE -> {
                    val nonNullHeaderName = headerName
                        ?: throw IllegalStateException("Header name is null, but got header value ${token.value}")

                    createNewPartIfNeeded(builder)
                    if (lexer.isMultiplePart && lexer.yystate() != Yylex.S_HEADER) {
                        builder.parts.last().headers[nonNullHeaderName] = token.value
                        if (nonNullHeaderName.contentEquals("Content-Disposition")) {
                            builder.parts.last().name = extractPartName(token.value)
                        }
                    } else {
                        builder.headers[nonNullHeaderName] = token.value
                    }
                    headerName = null
                }
                TokenType.TYPE_BODY_MESSAGE -> {
                    if (lexer.isMultiplePart) {
                        builder.parts.last().rawBody.add(token.value)
                    } else {
                        builder.rawBody.add(token.value)
                    }
                }
                TokenType.TYPE_SEPARATOR -> Unit
                TokenType.TYPE_BLANK -> Unit
                TokenType.TYPE_RESPONSE_REFERENCE -> Unit
                TokenType.TYPE_COMMENT -> Unit
                TokenType.TYPE_HANDLER_FILE_SCRIPT -> builder.rawScriptHandler.add(token.value)
                TokenType.TYPE_HANDLER_EMBEDDED_SCRIPT -> builder.rawScriptHandler.add(token.value)
            }
        }
        return result
    }

    private fun createNewPartIfNeeded(builder: Request.Builder) {
        if (lexer.isNewPartRequired) {
            builder.parts.add(Request.Part.Builder())
            lexer.resetNewPartRequired()
        }
    }

    companion object {
        private val PART_NAME_REGEX = ".* name=\"([^\"]+)\".*".toRegex(RegexOption.IGNORE_CASE)

        private fun extractPartName(fieldValue: String): String {
            return PART_NAME_REGEX.matchEntire(fieldValue)?.groups?.get(1)?.value.orEmpty()
        }
    }
}
