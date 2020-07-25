package uos.dev.restcli.parser

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class ParserTest {
    private val parser: Parser = Parser()

    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("provideTestCases")
    fun parse_http_request(
        name: String,
        rawHttpRequest: String,
        expected: Request
    ) {
        val result = parser.parse(rawHttpRequest)
        val request = result.first()
        assertEquals(request, expected)
    }

    private fun assertEquals(r1: Request, r2: Request) {
        assertThat(r1.method).isEqualTo(r2.method)
        assertThat(r1.requestTarget).isEqualTo(r2.requestTarget)
        assertThat(r1.httpVersion).isEqualTo(r2.httpVersion)
        assertThat(r1.headers).isEqualTo(r2.headers)
        assertThat(r1.body).isEqualTo(r2.body)
        assertThat(r1.fileLoad).isEqualTo(r2.fileLoad)

        assertThat(r1.parts.size).isEqualTo(r2.parts.size)
        r1.parts.forEachIndexed { index, part1 ->
            val part2 = r2.parts[index]
            assertThat(part1.name).isEqualTo(part2.name)
            assertThat(part1.headers).isEqualTo(part2.headers)
            assertThat(part1.body).isEqualTo(part2.body)
            assertThat(part1.fileLoad).isEqualTo(part2.fileLoad)
        }
    }

    companion object {
        @JvmStatic
        private fun provideTestCases(): Stream<Arguments> = Stream.of(
            Arguments.of(
                "Send POST request with json body",
                POST_REQUEST_WITH_JSON_BODY,
                Request(
                    method = RequestMethod.POST,
                    requestTarget = "https://httpbin.org/post",
                    headers = mapOf(
                        "Content-Type" to "application/json"
                    ),
                    body = "{\n" +
                            "  \"id\": 999,\n" +
                            "  \"value\": \"content\"\n" +
                            "}",
                    httpVersion = Request.DEFAULT_HTTP_VERSION
                )
            ),
            Arguments.of(
                "Send POST request with body as parameters",
                SEND_POST_REQUEST_WITH_BODY_AS_PARAMETERS,
                Request(
                    method = RequestMethod.POST,
                    requestTarget = "https://httpbin.org/post",
                    headers = mapOf(
                        "Content-Type" to "application/x-www-form-urlencoded"
                    ),
                    body = "id=999&value=content",
                    httpVersion = Request.DEFAULT_HTTP_VERSION
                )
            ),
            Arguments.of(
                "Send a form with the text and file fields",
                SEND_A_FORM_WITH_THE_TEXT_AND_FILE_FIELDS,
                Request(
                    method = RequestMethod.POST,
                    requestTarget = "https://httpbin.org/post",
                    headers = mapOf(
                        "Content-Type" to "multipart/form-data; boundary=WebAppBoundary"
                    ),
                    body = null,
                    parts = listOf(
                        Request.Part(
                            name = "element-name",
                            headers = mapOf(
                                "Content-Disposition" to "form-data; name=\"element-name\"",
                                "Content-Type" to "text/plain"
                            )
                        ),
                        Request.Part(
                            name = "data",
                            headers = mapOf(
                                "Content-Disposition" to "form-data; name=\"data\"; filename=\"data.json\"",
                                "Content-Type" to "application/json"
                            ),
                            // TODO: Reader content from file
                            body = "< ./request-form-data.json"
                        )
                    ),
                    httpVersion = Request.DEFAULT_HTTP_VERSION
                )
            ),
            Arguments.of(
                "Send request with dynamic variables in request's body",
                SEND_REQUEST_WITH_DYNAMIC_VARIABLES_IN_REQUEST_BODY,
                Request(
                    method = RequestMethod.POST,
                    requestTarget = "https://httpbin.org/post",
                    headers = mapOf(
                        "Content-Type" to "application/json"
                    ),
                    body = "{\n" +
                            "  \"id\": {{\$uuid}},\n" +
                            "  \"price\": {{\$randomInt}},\n" +
                            "  \"ts\": {{\$timestamp}},\n" +
                            "  \"value\": \"content\"\n" +
                            "}",
                    httpVersion = Request.DEFAULT_HTTP_VERSION
                )
            )
        )

        private const val POST_REQUEST_WITH_JSON_BODY = "### Send POST request with json body\n" +
                "POST https://httpbin.org/post\n" +
                "Content-Type: application/json\n" +
                "\n" +
                "{\n" +
                "  \"id\": 999,\n" +
                "  \"value\": \"content\"\n" +
                "}\n" +
                "\n"
        private const val SEND_POST_REQUEST_WITH_BODY_AS_PARAMETERS =
            "### Send POST request with body as parameters\n" +
                    "POST https://httpbin.org/post\n" +
                    "Content-Type: application/x-www-form-urlencoded\n" +
                    "\n" +
                    "id=999&value=content\n" +
                    "\n"
        private const val SEND_A_FORM_WITH_THE_TEXT_AND_FILE_FIELDS =
            "### Send a form with the text and file fields\n" +
                    "POST https://httpbin.org/post\n" +
                    "Content-Type: multipart/form-data; boundary=WebAppBoundary\n" +
                    "\n" +
                    "--WebAppBoundary\n" +
                    "Content-Disposition: form-data; name=\"element-name\"\n" +
                    "Content-Type: text/plain\n" +
                    "\n" +
                    "Name\n" +
                    "--WebAppBoundary\n" +
                    "Content-Disposition: form-data; name=\"data\"; filename=\"data.json\"\n" +
                    "Content-Type: application/json\n" +
                    "\n" +
                    "< ./request-form-data.json\n" +
                    "--WebAppBoundary--\n" +
                    "\n"
        private const val SEND_REQUEST_WITH_DYNAMIC_VARIABLES_IN_REQUEST_BODY =
            "### Send request with dynamic variables in request's body\n" +
                    "POST https://httpbin.org/post\n" +
                    "Content-Type: application/json\n" +
                    "\n" +
                    "{\n" +
                    "  \"id\": {{\$uuid}},\n" +
                    "  \"price\": {{\$randomInt}},\n" +
                    "  \"ts\": {{\$timestamp}},\n" +
                    "  \"value\": \"content\"\n" +
                    "}\n" +
                    "\n"
    }
}
