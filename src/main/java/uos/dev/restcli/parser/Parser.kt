package uos.dev.restcli.parser

import okio.ByteString
import okio.ByteString.Companion.encodeUtf8
import okio.ByteString.Companion.toByteString
import java.io.File
import java.io.FileReader
import java.io.Reader
import java.nio.file.Paths

class Parser {
    private val lexer: Yylex = Yylex(null)

    private fun nextToken(): Yytoken? = lexer.yylex()

    private fun reset(input: Reader) {
        lexer.yyreset(input)
    }

    fun parse(httpFilePath: String): List<Request> {
        val httpFile = File(httpFilePath)
        val input = FileReader(httpFile)
        val basedir = httpFile.parentFile?.absolutePath.orEmpty()
        return parse(input, basedir)
    }

    fun parse(input: Reader, basedir: String = ""): List<Request> {
        reset(input)
        val result = mutableListOf<Request>()
        var builder = Request.Builder()
        var headerName: String? = null

        fun buildRequestAndMakeBuilderNew() {
            builder.build()?.let { result.add(it) }
            builder = Request.Builder()
        }

        fun readFileContent(filePath: String): String {
            val absoluteFilePath = if (File(filePath).isAbsolute) filePath else Paths.get(basedir, filePath).toString()
            return File(absoluteFilePath).readText()
        }

        fun readFileContentByte(filePath: String): ByteString {
            val absoluteFilePath = if (File(filePath).isAbsolute) filePath else Paths.get(basedir, filePath).toString()
            return File(absoluteFilePath).readBytes().toByteString()
        }

        while (true) {
            val token = nextToken() ?: break
            if (lexer.yystate() == Yylex.YYINITIAL) {
                buildRequestAndMakeBuilderNew()
            }

            when (token.type) {
                TokenType.TYPE_SEPARATOR -> buildRequestAndMakeBuilderNew()
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
                    val headerValue = token.value
                    createNewPartIfNeeded(builder)
                    if (lexer.isMultiplePart && lexer.yystate() != Yylex.S_HEADER) {
                        builder.parts.last().headers[nonNullHeaderName] = headerValue
                        if (nonNullHeaderName.contentEquals("Content-Disposition")) {
                            with(builder.parts.last()) {
                                name = extractPartName(headerValue)
                                fileName = extractPartFileName(headerValue)
                            }
                        }
                    } else {
                        builder.headers[nonNullHeaderName] = headerValue
                    }
                    headerName = null
                }
                TokenType.TYPE_BODY_FILE_REF,
                TokenType.TYPE_BODY_MESSAGE -> {
                    val bodyMessage = if (token.type == TokenType.TYPE_BODY_FILE_REF) {
                        readFileContentByte(token.value)
                    } else {
                        token.value.encodeUtf8()
                    }
                    if (lexer.isMultiplePart) {
                        builder.parts.last().rawBody.add(bodyMessage)
                    } else {
                        builder.rawBody.add(bodyMessage)
                    }
                }
                TokenType.TYPE_BLANK -> Unit
                // TODO: Figure out what should we do with response reference.
                TokenType.TYPE_RESPONSE_REFERENCE -> builder.rawResponseReference = token.value
                TokenType.TYPE_COMMENT -> Unit
                TokenType.TYPE_HANDLER_FILE_SCRIPT,
                TokenType.TYPE_HANDLER_EMBEDDED_SCRIPT -> {
                    val script = if (token.type == TokenType.TYPE_HANDLER_FILE_SCRIPT) {
                        Request.wrapContentWithBarrier(readFileContent(token.value))
                    } else {
                        token.value
                    }
                    builder.rawScriptHandler.add(script)
                    builder.scriptHandlerStartLine = token.lineNumber + 1
                }
                TokenType.TYPE_NO_REDIRECT -> builder.isFollowRedirects = false
                TokenType.TYPE_NO_COOKIE_JAR -> builder.isNoCookieJar = true
                TokenType.TYPE_NO_LOG -> builder.isNoLog = true
                TokenType.TYPE_USE_OS_CREDENTIALS -> builder.isUseOsCredentials = true
                TokenType.TYPE_REQUEST_NAME -> builder.name = token.value
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
        private val PART_FILE_NAME_REGEX =
            ".* filename=\"([^\"]+)\".*".toRegex(RegexOption.IGNORE_CASE)

        private fun extractPartName(fieldValue: String): String =
            PART_NAME_REGEX.matchEntire(fieldValue)?.groups?.get(1)?.value.orEmpty()

        private fun extractPartFileName(fieldValue: String): String? =
            PART_FILE_NAME_REGEX.matchEntire(fieldValue)?.groups?.get(1)?.value
                ?.takeIf { it.isNotBlank() }
    }
}
