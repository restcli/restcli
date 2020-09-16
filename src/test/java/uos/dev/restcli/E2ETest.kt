package uos.dev.restcli

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import picocli.CommandLine

class E2ETest {
    /**
     * Tests execute the http request `fileName` in the test resource folder.
     * If you running the test on the IDE, you have to set the working directory is the test
     * resource folder.
     */
    @ParameterizedTest
    @CsvSource(value = [
        "get-requests.http",
        "post-requests.http",
        "requests-with-authorization.http",
        "requests-with-name.http",
        "requests-with-tests.http"
    ])
    fun `should not fail requests`(fileName: String) {
        //given
        println("Test file: $fileName")
        val args = arrayOf("-e", "test", "-l", "BASIC", fileName)
        //when
        val exitCode = CommandLine(RestCli())
            .apply { isCaseInsensitiveEnumValuesAllowed = true }
            .execute(*args)
        //then
        assertThat(exitCode).isEqualTo(0)
    }

    @ParameterizedTest
    @CsvSource(
        value = [
            "requests-with-failing-tests.http"
        ]
    )
    fun `should fail request`(fileName: String) {
        //given
        println("Test file: $fileName")
        val args = arrayOf("-e", "test", "-l", "BASIC", fileName)
        //when
        val exitCode = CommandLine(RestCli())
            .apply { isCaseInsensitiveEnumValuesAllowed = true }
            .execute(*args)
        //then
        assertThat(exitCode).isEqualTo(1)
    }
}
