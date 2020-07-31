package uos.dev.restcli.parser

import java.io.Reader

class Parser {
    private val lexer: Yylex = Yylex(null)

    private fun nextToken(): Yytoken? = lexer.yylex()

    private fun reset(input: Reader) {
        lexer.yyreset(input)
    }

    // TODO: parsing with load content from file referenced.
    fun parse(
        input: Reader,
        environment: Map<String, String> = emptyMap()
    ): List<Request> {
        fun injectEnv(input: String): String =
            EnvironmentVariableInjector.inject(input, environment)

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
                Yylex.S_RESPONSE_REFERENCE -> Unit
                Yylex.S_MULTIPLE_PART_HEADER -> Unit
                Yylex.S_MULTIPLE_PART_BODY -> Unit
            }

            when (token.type) {
                TokenType.TYPE_VALUE_FILE_REF -> Unit
                TokenType.TYPE_REQUEST_METHOD -> builder.method = RequestMethod.from(token.value)
                TokenType.TYPE_REQUEST_TARGET -> builder.requestTarget = injectEnv(token.value)
                TokenType.TYPE_REQUEST_HTTP_VERSION -> builder.httpVersion = token.value
                TokenType.TYPE_FIELD_NAME -> {
                    if (headerName != null) {
                        throw IllegalStateException("Header name exist($headerName). Expect header value")
                    }
                    headerName = injectEnv(token.value)
                }
                TokenType.TYPE_FIELD_VALUE -> {
                    val nonNullHeaderName = headerName
                        ?: throw IllegalStateException("Header name is null, but got header value ${token.value}")
                    val headerValue = injectEnv(token.value)
                    createNewPartIfNeeded(builder)
                    if (lexer.isMultiplePart && lexer.yystate() != Yylex.S_HEADER) {
                        builder.parts.last().headers[nonNullHeaderName] = headerValue
                        if (nonNullHeaderName.contentEquals("Content-Disposition")) {
                            builder.parts.last().name = extractPartName(headerValue)
                        }
                    } else {
                        builder.headers[nonNullHeaderName] = headerValue
                    }
                    headerName = null
                }
                TokenType.TYPE_BODY_MESSAGE -> {
                    val bodyMessage = injectEnv(token.value);
                    if (lexer.isMultiplePart) {
                        builder.parts.last().rawBody.add(bodyMessage)
                    } else {
                        builder.rawBody.add(bodyMessage)
                    }
                }
                TokenType.TYPE_SEPARATOR -> Unit
                TokenType.TYPE_BLANK -> Unit
                TokenType.TYPE_RESPONSE_REFERENCE -> builder.rawResponseReference = token.value
                TokenType.TYPE_COMMENT -> Unit
                TokenType.TYPE_HANDLER_FILE_SCRIPT -> builder.rawScriptHandler.add(token.value)
                TokenType.TYPE_HANDLER_EMBEDDED_SCRIPT -> builder.rawScriptHandler.add(token.value)
            }
        }
        // Build the latest request if exist.
        buildRequestAndMakeBuilderNew()
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
