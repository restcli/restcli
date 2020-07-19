package uos.dev.restcli.parser

import org.junit.jupiter.api.Test
import uos.dev.restcli.TestResourceLoader

class HttpRequestParserTest {

    @Test
    fun parse() {
        javaClass.classLoader.getResourceAsStream("requests/get-requests.http")
        val parser = HttpRequestParser()
        val reader = TestResourceLoader.testResourceReader(TEST_GET_REQUESTS_RESOURCE)
        val result = parser.parse(reader)
        println("Hello:" + result.joinToString("\n"))
    }

    @Test
    fun debug() {
        val path = TestResourceLoader.testResourcePath(TEST_GET_REQUESTS_RESOURCE)
        Yylex.main(arrayOf(path))
    }

    companion object {
        private const val TEST_GET_REQUESTS_RESOURCE = "requests/get-requests.http"
        private const val TEST_POST_REQUESTS_RESOURCE = "requests/post-requests.http"
    }
}
