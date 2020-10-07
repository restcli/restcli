package uos.dev.restcli

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

/**
 * IMPORTANT: When run this test, the working directory must be set to:
 *  $ProjectFileDir$/src/test/resources/requests
 * That is needed for resolve referenced http env files.
 */
class EnvironmentLoaderTest {
    @Test
    fun load_http_client_env_config() {
        val environmentFilesDirectory = javaClass.getResource("/requests").path
        val env = EnvironmentLoader().load(environmentFilesDirectory, "test")
        assertThat(env).isEqualTo(
            mapOf(
                "host" to "https://httpbin.org",
                "show_env" to "1",
                "username" to "user",
                "password" to "passwd"
            )
        )
    }
}
