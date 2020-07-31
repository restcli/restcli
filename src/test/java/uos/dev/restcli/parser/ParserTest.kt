package uos.dev.restcli.parser

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import uos.dev.restcli.TestResourceLoader
import java.util.stream.Stream

class ParserTest {
    private val parser = Parser()

    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("parserTestCases")
    fun parse(
        name: String,
        input: String,
        environment: Map<String, String> = emptyMap(),
        expected: Request
    ) {
        val reader = input.reader()
        val result = parser.parse(reader, environment)
        assertThat(result.first()).isEqualTo(expected)
    }

    @Test
    fun parse_post() {
        val reader = TestResourceLoader.testResourceReader(TEST_POST_REQUESTS_RESOURCE)
        val result = parser.parse(reader)
        assertThat(result.size).isEqualTo(4)
        assertThat(result.first()).isEqualTo(
            Request(
                method = RequestMethod.POST,
                requestTarget = "https://httpbin.org/post",
                headers = mapOf("Content-Type" to "application/json"),
                body = "{\n" +
                        "  \"id\": 999,\n" +
                        "  \"value\": \"content\"\n" +
                        "}"
            )
        )

        assertThat(result[2]).isEqualTo(
            Request(
                method = RequestMethod.POST,
                requestTarget = "https://httpbin.org/post",
                headers = mapOf("Content-Type" to "multipart/form-data; boundary=WebAppBoundary"),
                body = null,
                parts = listOf(
                    Request.Part(
                        name = "element-name",
                        headers = mapOf(
                            "Content-Disposition" to "form-data; name=\"element-name\"",
                            "Content-Type" to "text/plain"
                        ),
                        body = "Name"
                    ),
                    Request.Part(
                        name = "data",
                        headers = mapOf(
                            "Content-Disposition" to "form-data; name=\"data\"; filename=\"data.json\"",
                            "Content-Type" to "application/json"
                        ),
                        body = "< ./request-form-data.json"
                    )
                )
            )
        )

        assertThat(result.last()).isEqualTo(
            Request(
                method = RequestMethod.POST,
                requestTarget = "https://httpbin.org/post",
                headers = mapOf("Content-Type" to "application/json"),
                body = "{\n" +
                        "  \"id\": {{\$uuid}},\n" +
                        "  \"price\": {{\$randomInt}},\n" +
                        "  \"ts\": {{\$timestamp}},\n" +
                        "  \"value\": \"content\"\n" +
                        "}",
                responseReference = "<> post-requests.http"
            )
        )
    }

    @Test
    fun parse_request_with_tests() {
        val reader = TestResourceLoader.testResourceReader(TEST_REQUESTS_WITH_TESTS_RESOURCE)
        val result = parser.parse(reader)
        assertThat(result.size).isEqualTo(4)

        assertThat(result.first()).isEqualTo(
            Request(
                method = RequestMethod.GET,
                requestTarget = "https://httpbin.org/status/200",
                body = null,
                scriptHandler = "> {%\n" +
                        "client.test(\"Request executed successfully\", function() {\n" +
                        "  client.assert(response.status === 200, \"Response status is not 200\");\n" +
                        "});\n" +
                        "%}"
            )
        )

        assertThat(result[2]).isEqualTo(
            Request(
                method = RequestMethod.GET,
                requestTarget = "https://httpbin.org/get",
                body = null,
                scriptHandler = "> {%\n" +
                        "client.test(\"Request executed successfully\", function() {\n" +
                        "  client.assert(response.status === 200, \"Response status is not 200\");\n" +
                        "});\n" +
                        "\n" +
                        "client.test(\"Response content-type is json\", function() {\n" +
                        "  var type = response.contentType.mimeType;\n" +
                        "  client.assert(type === \"application/json\", \"Expected 'application/json' but received '\" + type + \"'\");\n" +
                        "});\n" +
                        "%}"
            )
        )
    }

    @Test
    fun debug() {
        val path = TestResourceLoader.testResourcePath(TEST_POST_REQUESTS_RESOURCE)
        Yylex.main(arrayOf(path))
    }

    companion object {
        private const val TEST_GET_REQUESTS_RESOURCE = "requests/get-requests.http"
        private const val TEST_POST_REQUESTS_RESOURCE = "requests/post-requests.http"
        private const val TEST_REQUESTS_WITH_TESTS_RESOURCE = "requests/requests-with-tests.http"

        @JvmStatic
        private fun parserTestCases(): Stream<Arguments> = Stream.of(
            createParserTestCase(
                name = "GET request with a header",
                input = "### GET request with a header\n" +
                        "GET https://httpbin.org/ip\n" +
                        "Accept: application/json\n",
                environment = emptyMap(),
                expected = Request(
                    method = RequestMethod.GET,
                    requestTarget = "https://httpbin.org/ip",
                    headers = mapOf("Accept" to "application/json")
                )
            ),
            createParserTestCase(
                name = "GET request with parameter",
                input = "### GET request with parameter\n" +
                        "GET https://httpbin.org/get?show_env=1\n" +
                        "Accept: application/json\n",
                environment = emptyMap(),
                expected = Request(
                    method = RequestMethod.GET,
                    requestTarget = "https://httpbin.org/get?show_env=1",
                    headers = mapOf("Accept" to "application/json")
                )
            ),
            createParserTestCase(
                name = "GET request with environment variables",
                input = "### GET request with environment variables\n" +
                        "GET {{host}}/get?show_env={{show_env}}\n" +
                        "Accept: application/json\n" +
                        "\n",
                environment = mapOf(
                    "host" to "https://httpbin.org",
                    "show_env" to "1"
                ),
                expected = Request(
                    method = RequestMethod.GET,
                    requestTarget = "https://httpbin.org/get?show_env=1",
                    headers = mapOf("Accept" to "application/json")
                )
            ),
            // TODO: Support GET request with disabled redirects
            createParserTestCase(
                name = "GET request with disabled redirects",
                input = "### GET request with disabled redirects\n" +
                        "# @no-redirect\n" +
                        "GET http://httpbin.org/status/301\n",
                environment = emptyMap(),
                expected = Request(
                    method = RequestMethod.GET,
                    requestTarget = "http://httpbin.org/status/301"
                )
            ),
            // TODO: Support dynamic variable
            createParserTestCase(
                name = "GET request with dynamic variables",
                input = "### GET request with dynamic variables\n" +
                        "GET http://httpbin.org/anything?id={{\$uuid}}&ts={{\$timestamp}}\n",
                environment = emptyMap(),
                expected = Request(
                    method = RequestMethod.GET,
                    requestTarget = "http://httpbin.org/anything?id={{\$uuid}}&ts={{\$timestamp}}"
                )
            )
//            ,
//            createParserTestCase(
//                name = "",
//                input = "",
//                environment = emptyMap(),
//                expected = Request()
//            ),
//            createParserTestCase(
//                name = "",
//                input = "",
//                environment = emptyMap(),
//                expected = Request()
//            )
        )

        @Suppress("SameParameterValue")
        private fun createParserTestCase(
            name: String,
            input: String,
            environment: Map<String, String> = emptyMap(),
            expected: Request
        ): Arguments = Arguments.of(name, input, environment, expected)
    }
}
