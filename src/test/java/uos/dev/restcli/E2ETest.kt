package uos.dev.restcli

import com.google.common.truth.Truth.assertThat
import org.junit.Ignore
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import uos.dev.restcli.Resource.getResourcePath
import uos.dev.restcli.report.TestReportStore

class E2ETest {
    /**
     * Tests execute the http request `fileName` in the test resource folder.
     * If you are running the test on the IDE, you have to set the working directory point to the test
     * resource folder.
     */
    @ParameterizedTest
    @CsvSource(
        value = ["get-requests.http", "post-requests.http", "requests-with-authorization.http", "requests-with-name.http", "requests-with-tests.http"]
    )
    fun `should not fail requests`(fileName: String) {
        // Given
        println("Test file: $fileName")
        val restCli = RestCli().apply {
            environmentName = "test"
            logLevel = HttpLoggingLevel.HEADERS
            httpFilePaths = arrayOf(getResourcePath("/requests/${fileName}"))
            environmentFilesDirectory = getResourcePath("/requests/")
            decorator = ConfigDecorator.THREE_STAR
        }

        // When
        val exitCode = restCli.call()
        // Then
        assertThat(exitCode).isEqualTo(0)
    }

    @ParameterizedTest
    @CsvSource(
        value = ["requests-bad-ssl.http"]
    )
    fun `request with bad ssl`(fileName: String) {
        // Given
        println("Test file: $fileName")
        val restCli = RestCli().apply {
            environmentName = "test"
            logLevel = HttpLoggingLevel.BASIC
            httpFilePaths = arrayOf(getResourcePath("/requests/${fileName}"))
            environmentFilesDirectory = getResourcePath("/requests/")
            insecure = true
        }

        // When
        val exitCode = restCli.call()
        // Then
        assertThat(exitCode).isEqualTo(0)
    }

    @ParameterizedTest
    @CsvSource(
        value = ["requests-simple.http"]
    )
    fun `accept env define in private env only`(fileName: String) {
        // Given
        println("Test file: $fileName")
        val restCli = RestCli().apply {
            environmentName = "env_on_private_only"
            logLevel = HttpLoggingLevel.BASIC
            httpFilePaths = arrayOf(getResourcePath("/requests/${fileName}"))
            environmentFilesDirectory = getResourcePath("/requests/")
            insecure = true
        }

        // When
        val exitCode = restCli.call()
        // Then
        assertThat(exitCode).isEqualTo(0)
    }

    @ParameterizedTest
    @CsvSource(
        value = ["requests-simple.http"]
    )
    fun `accept env define in public env only`(fileName: String) {
        // Given
        println("Test file: $fileName")
        val restCli = RestCli().apply {
            environmentName = "env_on_public_only"
            logLevel = HttpLoggingLevel.BASIC
            httpFilePaths = arrayOf(getResourcePath("/requests/${fileName}"))
            environmentFilesDirectory = getResourcePath("/requests/")
            insecure = true
        }

        // When
        val exitCode = restCli.call()
        // Then
        assertThat(exitCode).isEqualTo(0)
    }

    @ParameterizedTest
    @CsvSource(
        value = ["requests-simple.http"]
    )
    fun `throw error if env is not define on both private or public`(fileName: String) {
        println("Test file: $fileName")
        val restCli = RestCli().apply {
            environmentName = "no_exist_env"
            logLevel = HttpLoggingLevel.BASIC
            httpFilePaths = arrayOf(getResourcePath("/requests/${fileName}"))
            environmentFilesDirectory = getResourcePath("/requests/")
            insecure = true
        }

        val exception = assertThrows<RuntimeException> { restCli.call() }

        assertThat(exception.message?.contains("is not found")).isTrue()
    }

    @ParameterizedTest
    @CsvSource(
        value = ["requests-with-failing-tests.http"]
    )
    fun `should fail request`(fileName: String) {
        // Given
        println("Test file: $fileName")
        val restCli = RestCli().apply {
            environmentName = "test"
            logLevel = HttpLoggingLevel.BASIC
            httpFilePaths = arrayOf(getResourcePath("/requests/${fileName}"))
            environmentFilesDirectory = getResourcePath("/requests/")
        }

        // When
        val exitCode = restCli.call()

        // Then
        assertThat(exitCode).isEqualTo(1)

        assertThat(TestReportStore.testGroupReports.all { it.testReports.size > 0 }).isTrue()
    }

    @Test
    fun `should fail if any of the requests fails`() {
        val paths = arrayOf(
            "requests-with-failing-test.http",
            "requests-with-passing-test.http",
        )

        // Given
        val httpFilePaths = paths.map { getResourcePath("/requests/$it") }.toTypedArray()
        val restCli = RestCli().apply {
            environmentName = "test"
            logLevel = HttpLoggingLevel.BASIC
            this.httpFilePaths = httpFilePaths
            environmentFilesDirectory = getResourcePath("/requests/")
        }

        // When
        val exitCode = restCli.call()

        // Then
        assertThat(exitCode).isEqualTo(1)

        assertThat(TestReportStore.testGroupReports.all { it.testReports.isNotEmpty() }).isTrue()
    }

    @Ignore
    fun `should share variables between two request files`() {
        val paths = arrayOf(
            "requests-share-var-between-files1.http",
            "requests-share-var-between-files2.http",
        )

        // Given
        val httpFilePaths = paths.map { getResourcePath("/requests/$it") }.toTypedArray()
        val restCli = RestCli().apply {
            environmentName = "test"
            logLevel = HttpLoggingLevel.BASIC
            this.httpFilePaths = httpFilePaths
            environmentFilesDirectory = getResourcePath("/requests/")
        }

        // When
        val exitCode = restCli.call()

        // Then
        assertThat(exitCode).isEqualTo(0)

        assertThat(TestReportStore.testGroupReports.all { it.testReports.isNotEmpty() }).isTrue()
    }
}
