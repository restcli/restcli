package uos.dev.restcli.parser

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import uos.dev.restcli.TestResourceLoader

class HttpRequestParserTest {
    private val parser = HttpRequestParser()

    @Test
    fun parse_get() {
        val reader = TestResourceLoader.testResourceReader(TEST_GET_REQUESTS_RESOURCE)
        val result = parser.parse(reader)
        assertThat(result.first()).isEqualTo(
            Request(
                method = RequestMethod.GET,
                requestTarget = "https://httpbin.org/ip",
                headers = mapOf("Accept" to "application/json"),
                httpVersion = Request.DEFAULT_HTTP_VERSION
            )
        )

        assertThat(result.last()).isEqualTo(
            Request(
                method = RequestMethod.GET,
                requestTarget = "http://httpbin.org/anything?id={{\$uuid}}&ts={{\$timestamp}}",
                httpVersion = Request.DEFAULT_HTTP_VERSION,
                headers = emptyMap()
            )
        )
    }

    @Test
    fun parse_post() {
        val reader = TestResourceLoader.testResourceReader(TEST_POST_REQUESTS_RESOURCE)
        val result = parser.parse(reader)
        assertThat(result.first()).isEqualTo(
            Request(
                method = RequestMethod.POST,
                requestTarget = "https://httpbin.org/post",
                headers = mapOf("Content-Type" to "application/json"),
                httpVersion = Request.DEFAULT_HTTP_VERSION,
                body = "{\n" +
                        "  \"id\": 999,\n" +
                        "  \"value\": \"content\"\n" +
                        "}"
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
    }
}
