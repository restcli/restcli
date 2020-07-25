package uos.dev.restcli.parser

import uos.dev.restcli.Outcome
import uos.dev.restcli.parser.grammar.Comment
import uos.dev.restcli.parser.grammar.HeaderField
import uos.dev.restcli.parser.grammar.OpenResponseHandler
import uos.dev.restcli.parser.grammar.RequestLine
import uos.dev.restcli.parser.grammar.RequestPart
import uos.dev.restcli.parser.grammar.RequestSeparator
import uos.dev.restcli.parser.grammar.ResponseReference
import java.io.InputStream

class Parser {
    //region Http Request Grammars.
    private val requestSeparator = RequestSeparator()
    private val comment = Comment()
    private val requestLine = RequestLine()
    private val headerField = HeaderField()
    private val requestPart = RequestPart()
    private val openResponseHandler = OpenResponseHandler()
    private val responseReference = ResponseReference()
    //endregion

    fun parse(rawHttpRequestString: String): List<Request> =
        parse(Reader.StringReader(rawHttpRequestString))

    fun parse(input: InputStream): List<Request> = parse(Reader.InputStreamReader(input))

    fun parse(reader: Reader): List<Request> {
        var state = ParseState.URL
        val result = mutableListOf<Request>()
        var count = 0
        var builder = Request.Builder()
        reader.forEachLine { line ->
            count++
            val separatorResult = requestSeparator.parse(line)
            val commentResult = comment.parse(line)
            val openResponseHandlerResult = openResponseHandler.parse(line)
            val responseReferenceResult = responseReference.parse(line)

            when {
                line.isEmpty() -> {
                    if (state == ParseState.HEADER) {
                        state = ParseState.BODY
                    }
                    return@forEachLine
                }
                separatorResult is Outcome.Success -> {
                    builder.build()?.let(result::add)
                    builder = Request.Builder()
                    val comment = separatorResult.data.comment
                    state = ParseState.URL
                    return@forEachLine
                }
                commentResult is Outcome.Success -> {
                    return@forEachLine
                }
                openResponseHandlerResult is Outcome.Success -> {
                    state = ParseState.RESPONSE_HANDLER
                }

                responseReferenceResult is Outcome.Success -> {
                    state = ParseState.RESPONSE_REFERENCE
                }
            }

            when (state) {
                ParseState.URL -> {
                    val requestLineResult = requestLine.parse(line)
                            as? Outcome.Success ?: throw createErrorException(count, line)
                    val data = requestLineResult.data
                    builder.method = data.method
                    builder.requestTarget = data.target
                    data.httpVersion?.let { builder.httpVersion = it }
                    state = ParseState.HEADER
                }
                ParseState.HEADER -> {
                    val headerFieldResult = headerField.parse(line)
                            as? Outcome.Success ?: throw createErrorException(count, line)
                    val data = headerFieldResult.data
                    builder.headers[data.name] = data.value
                }
                ParseState.BODY -> builder.rawBody.add(line)
                ParseState.RESPONSE_HANDLER -> builder.rawResponseHandler.add(line)
                ParseState.RESPONSE_REFERENCE -> builder.rawResponseReference = line
            }
        }
        builder.build()?.let(result::add)
        return result
    }

    private fun createErrorException(lineNumber: Int, content: String): IllegalStateException =
        IllegalStateException("Error parsing http request at line $lineNumber\n>$content")
}

interface Reader {
    fun forEachLine(action: (line: String) -> Unit)

    companion object {
        fun StringReader(text: String): Reader = object : Reader {
            override fun forEachLine(action: (line: String) -> Unit) {
                text.lines().forEach(action)
            }
        }

        fun InputStreamReader(inputStream: InputStream) = object : Reader {
            override fun forEachLine(action: (line: String) -> Unit) {
                inputStream.bufferedReader().forEachLine(action)
            }
        }
    }
}

enum class ParseState {
    URL,
    HEADER,
    BODY,
    RESPONSE_HANDLER,
    RESPONSE_REFERENCE
}
