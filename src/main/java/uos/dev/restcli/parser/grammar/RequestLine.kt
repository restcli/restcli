package uos.dev.restcli.parser.grammar

import org.intellij.lang.annotations.Language
import uos.dev.restcli.Outcome
import uos.dev.restcli.parser.RequestMethod

class RequestLine : Grammar<RequestLine.Data>() {
    data class Data(
        val input: String,
        val method: RequestMethod = RequestMethod.GET,
        val target: String? = null,
        val httpVersion: String? = null
    )

    override fun parse(input: String): Outcome<Data> {
        val matchResult = PATTERN.matchEntire(input)
            ?: return Outcome.Error(createParserException(input))
        val target = matchResult.groups[TARGET_GROUP_INDEX]?.value
            ?: return Outcome.Error(createParserException(input))
        val method = matchResult.groups[METHOD_GROUP_INDEX]?.value
        val httpVersion = matchResult.groups[HTTP_VERSION_INDEX]?.value

        return Outcome.Success(
            Data(
                input = input,
                target = target,
                method = RequestMethod.from(method.orEmpty()) ?: RequestMethod.GET,
                httpVersion = httpVersion
            )
        )
    }

    companion object {
        @Language("RegExp")
        private const val HTTP_VERSION_PATTERN = "HTTP/\\d+\\.\\d+"

        @Language("RegExp")
        private const val METHOD_PATTERN =
            "(?i)(GET|HEAD|POST|PUT|DELETE|CONNECT|PATCH|OPTIONS|TRACE)(?-i)"

        @Language("RegExp")
        private const val REQUEST_TARGET_ORIGIN_FORM_PATTERN = "\\S+"

        @Language("RegExp")
        private const val REQUEST_TARGET_ABSOLUTE_FORM_PATTERN = "((?i)(http|https)(?-i)://)\\S+"

        @Language("RegExp")
        private const val REQUEST_TARGET_ASTERISK_FORM_PATTERN = "\\*"

        @Language("RegExp")
        private const val REQUEST_TARGET_PATTERN =
            "(($REQUEST_TARGET_ORIGIN_FORM_PATTERN)|($REQUEST_TARGET_ABSOLUTE_FORM_PATTERN)|($REQUEST_TARGET_ASTERISK_FORM_PATTERN))"


        /**
         * A request line consists of a request method, target and the HTTP protocol version. If the request method is omitted, ‘GET’ will be used as a default. The HTTP protocol version can be also omitted.
         *
         * request-line:
         *       [method required-whitespace] request-target [required-whitespace http-version]
         * ```
         * method:
         *       ‘GET’
         *       ‘HEAD’
         *       ‘POST’
         *       ‘PUT’
         *       ‘DELETE’
         *       ‘CONNECT’
         *       ‘PATCH’
         *       ‘OPTIONS’
         *       ‘TRACE’
         * ```
         *
         * ```
         * http-version:
         *       ‘HTTP/’ (digit)+ ‘.’ (digit)+
         * ```
         * Example:
         *
         * `http://example.com`
         * or
         *
         * `GET http://example.com HTTP/1.1`
         */
        val PATTERN: Regex =
            "((($METHOD_PATTERN)\\s+)?$REQUEST_TARGET_PATTERN(\\s+($HTTP_VERSION_PATTERN))?)".toRegex()

        private const val METHOD_GROUP_INDEX = 4
        private const val TARGET_GROUP_INDEX = 6
        private const val HTTP_VERSION_INDEX = 12
    }
}
