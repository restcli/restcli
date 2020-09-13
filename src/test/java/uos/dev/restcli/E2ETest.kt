package uos.dev.restcli

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import picocli.CommandLine

class E2ETest {
    @Test
    fun testHttpRequests() {
        val fileNames = listOf(
            "get-requests.http",
            "post-requests.http",
            "requests-with-authorization.http",
            "requests-with-name.http",
            "requests-with-tests.http"
        )
        fileNames.forEach { fileName -> testExecuteHttpRequestFile(fileName) }
    }

    /**
     * Tests execute the http request `fileName` in the test resource folder.
     * If you running the test on the IDE, you have to set the working directory is the test
     * resource folder.
     */
    private fun testExecuteHttpRequestFile(fileName: String) {
        println("Test file: $fileName")
        val args = arrayOf("-e", "test", "-l", "BASIC", fileName)
        val exitCode = CommandLine(RestCli())
            .apply { isCaseInsensitiveEnumValuesAllowed = true }
            .execute(*args)
        assertThat(exitCode).isEqualTo(0)
    }
}
