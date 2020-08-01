package uos.dev.restcli.parser

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import uos.dev.restcli.TestResourceLoader
import java.util.stream.Stream

/**
 * IMPORTANT: When run this test, the working directory must be set to:
 *  $ProjectFileDir$/src/test/resources/requests
 * That is needed for resolve referenced files such as message body referenced file.
 */
class ParserTest {
    private val environmentVariableInjector =
        EnvironmentVariableInjectorImpl(FakeDynamicVariableProvider())
    private val parser = Parser(environmentVariableInjector)

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
    fun debug() {
        val path = TestResourceLoader.testResourcePath("requests/post-requests.http")
        Yylex.main(arrayOf(path))
    }

    companion object {
        @Suppress("unused")
        @JvmStatic
        private fun parserTestCases(): Stream<Arguments> = Stream.of(
            createParserTestCase(
                name = "GET request with a header",
                input = "### GET request with a header\n" +
                        "GET https://httpbin.org/ip\n" +
                        "Accept: application/json\n",
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
                expected = Request(
                    method = RequestMethod.GET,
                    requestTarget = "http://httpbin.org/status/301"
                )
            ),
            createParserTestCase(
                name = "GET request with dynamic variables",
                input = "### GET request with dynamic variables\n" +
                        "GET http://httpbin.org/anything?id={{\$uuid}}&ts={{\$timestamp}}\n",
                expected = Request(
                    method = RequestMethod.GET,
                    requestTarget = "http://httpbin.org/anything?id=" +
                            "${FakeDynamicVariableProvider.FAKE_UUID}&ts=" +
                            "${FakeDynamicVariableProvider.FAKE_TIMESTAMP}"
                )
            ),
            createParserTestCase(
                name = "Send POST request with json body",
                input = "### Send POST request with json body\n" +
                        "POST https://httpbin.org/post\n" +
                        "Content-Type: application/json\n" +
                        "\n" +
                        "{\n" +
                        "  \"id\": 999,\n" +
                        "  \"value\": \"content\"\n" +
                        "}\n",
                expected = Request(
                    method = RequestMethod.POST,
                    requestTarget = "https://httpbin.org/post",
                    headers = mapOf("Content-Type" to "application/json"),
                    body = "{\n" +
                            "  \"id\": 999,\n" +
                            "  \"value\": \"content\"\n" +
                            "}"
                )
            ),
            createParserTestCase(
                name = "Send POST request with body as parameters",
                input = "\n" +
                        "### Send POST request with body as parameters\n" +
                        "POST https://httpbin.org/post\n" +
                        "Content-Type: application/x-www-form-urlencoded\n" +
                        "\n" +
                        "id=999&value=content\n",
                expected = Request(
                    method = RequestMethod.POST,
                    requestTarget = "https://httpbin.org/post",
                    headers = mapOf(
                        "Content-Type" to "application/x-www-form-urlencoded"
                    ),
                    body = "id=999&value=content"
                )
            ),
            // TODO: Handle loading content from referenced file.
            createParserTestCase(
                name = "Send a form with the text and file fields",
                input = "### Send a form with the text and file fields\n" +
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
                        "--WebAppBoundary--\n",
                expected = Request(
                    method = RequestMethod.POST,
                    requestTarget = "https://httpbin.org/post",
                    headers = mapOf(
                        "Content-Type" to "multipart/form-data; boundary=WebAppBoundary"
                    ),
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
                            body = "{\n" +
                                    "  \"name\": \"my-name\",\n" +
                                    "  \"params\": [1, 2, 3]\n" +
                                    "}"
                        )
                    )
                )
            ),
            createParserTestCase(
                name = "Send request with dynamic variables in request's body",
                input = "### Send request with dynamic variables in request's body\n" +
                        "POST https://httpbin.org/post\n" +
                        "Content-Type: application/json\n" +
                        "\n" +
                        "{\n" +
                        "  \"id\": {{\$uuid}},\n" +
                        "  \"price\": {{\$randomInt}},\n" +
                        "  \"ts\": {{\$timestamp}},\n" +
                        "  \"value\": \"content\"\n" +
                        "}\n",
                environment = emptyMap(),
                expected = Request(
                    method = RequestMethod.POST,
                    requestTarget = "https://httpbin.org/post",
                    headers = mapOf(
                        "Content-Type" to "application/json"
                    ),
                    body = "{\n" +
                            "  \"id\": ${FakeDynamicVariableProvider.FAKE_UUID},\n" +
                            "  \"price\": ${FakeDynamicVariableProvider.FAKE_RANDOM_INT},\n" +
                            "  \"ts\": ${FakeDynamicVariableProvider.FAKE_TIMESTAMP},\n" +
                            "  \"value\": \"content\"\n" +
                            "}"
                )
            ),
            createParserTestCase(
                name = "Successful test: check response status is 200",
                input = "### Successful test: check response status is 200\n" +
                        "GET https://httpbin.org/status/200\n" +
                        "\n" +
                        "> {%\n" +
                        "client.test(\"Request executed successfully\", function() {\n" +
                        "  client.assert(response.status === 200, \"Response status is not 200\");\n" +
                        "});\n" +
                        "%}\n" +
                        "\n",
                expected = Request(
                    method = RequestMethod.GET,
                    requestTarget = "https://httpbin.org/status/200",
                    scriptHandler = "> {%\n" +
                            "client.test(\"Request executed successfully\", function() {\n" +
                            "  client.assert(response.status === 200, \"Response status is not 200\");\n" +
                            "});\n" +
                            "%}"
                )
            ),
            // TODO: Add test case for inject variable in test-script.
            createParserTestCase(
                name = "Failed test: check response status is 200",
                input = "### Failed test: check response status is 200\n" +
                        "GET https://httpbin.org/status/404\n" +
                        "\n" +
                        "> {%\n" +
                        "client.test(\"Request executed successfully\", function() {\n" +
                        "  client.assert(response.status === 200, \"Response status is not 200\");\n" +
                        "});\n" +
                        "%}\n",
                expected = Request(
                    method = RequestMethod.GET,
                    requestTarget = "https://httpbin.org/status/404",
                    scriptHandler = "> {%\n" +
                            "client.test(\"Request executed successfully\", function() {\n" +
                            "  client.assert(response.status === 200, \"Response status is not 200\");\n" +
                            "});\n" +
                            "%}"
                )
            ),
            createParserTestCase(
                name = "GET request with a response reference",
                input = "### GET request with a response reference\n" +
                        "GET https://httpbin.org/ip\n" +
                        "\n" +
                        "<> ip_response.json\n",
                expected = Request(
                    method = RequestMethod.GET,
                    requestTarget = "https://httpbin.org/ip",
                    responseReference = "<> ip_response.json"
                )
            )
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
