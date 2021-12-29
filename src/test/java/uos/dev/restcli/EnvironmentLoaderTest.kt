package uos.dev.restcli

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import uos.dev.restcli.configs.EnvironmentConfigs
import uos.dev.restcli.configs.NoopConfigDecorator
import uos.dev.restcli.configs.ThreeStarConfigDecorator

/**
 * IMPORTANT: When run this test, the working directory must be set to:
 *  $ProjectFileDir$/src/test/resources/requests
 * That is needed for resolve referenced http env files.
 */
class EnvironmentLoaderTest {
    @AfterEach
    fun tearDown() {
        EnvironmentConfigs.changeDefaultDecorator(ThreeStarConfigDecorator)
    }

    @Test
    fun load_http_client_env_config() {
        val environmentFilesDirectory = javaClass.getResource("/requests").path
        EnvironmentConfigs.changeDefaultDecorator(NoopConfigDecorator)
        val env = EnvironmentLoader().load(environmentFilesDirectory, "test")
        mapOf(
            "host" to "https://httpbin.org",
            "show_env" to "1",
            "username" to "user",
            "password" to "passwd"
        ).forEach { entry ->
            assertThat(env.containsKey(entry.key)).isTrue()
            assertThat(env.getValue(entry.key)).isEqualTo(entry.value)
        }
    }
}
