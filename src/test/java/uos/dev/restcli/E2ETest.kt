package uos.dev.restcli

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import uos.dev.restcli.report.TestReportStore

class E2ETest {
    /**
     * Tests execute the http request `fileName` in the test resource folder.
     * If you running the test on the IDE, you have to set the working directory point to the test
     * resource folder.
     */
    @ParameterizedTest
    @CsvSource(
        value = [
            "get-requests.http",
            "post-requests.http",
            "requests-with-authorization.http",
            "requests-with-name.http",
            "requests-with-tests.http"
        ]
    )
    fun `should not fail requests`(fileName: String) {
        // Given
        println("Test file: $fileName")
        val restCli = RestCli().apply {
            environmentName = "test"
            logLevel = HttpLoggingLevel.HEADERS
            httpFilePaths = arrayOf(javaClass.getResource("/requests/${fileName}").path)
            environmentFilesDirectory = javaClass.getResource("/requests/").path
            decorator = ConfigDecorator.THREE_STAR
            hidePrivateInLogs = true
        }

        // When
        val exitCode = restCli.call()
        // Then
        assertThat(exitCode).isEqualTo(0)
    }


    @ParameterizedTest
    @CsvSource(
        value = [
            "requests-bad-ssl.http"
        ]
    )
    fun `request with bad ssl`(fileName: String) {
        // Given
        println("Test file: $fileName")
        val restCli = RestCli().apply {
            environmentName = "test"
            logLevel = HttpLoggingLevel.BASIC
            httpFilePaths = arrayOf(javaClass.getResource("/requests/${fileName}").path)
            environmentFilesDirectory = javaClass.getResource("/requests/").path
            insecure = true
        }

        // When
        val exitCode = restCli.call()
        // Then
        assertThat(exitCode).isEqualTo(0)
    }

    @ParameterizedTest
    @CsvSource(
        value = [
            "requests-with-failing-tests.http"
        ]
    )
    fun `should fail request`(fileName: String) {
        // Given
        println("Test file: $fileName")
        val restCli = RestCli().apply {
            environmentName = "test"
            logLevel = HttpLoggingLevel.BASIC
            httpFilePaths = arrayOf(javaClass.getResource("/requests/${fileName}").path)
            environmentFilesDirectory = javaClass.getResource("/requests/").path
        }

        // When
        val exitCode = restCli.call()

        // Then
        assertThat(exitCode).isEqualTo(1)

        assertThat(TestReportStore.testGroupReports.all { it.testReports.size > 0 }).isTrue()
    }
}
