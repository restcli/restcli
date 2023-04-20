package uos.dev.restcli.parser

import com.google.common.truth.Truth.assertThat
import okio.ByteString.Companion.encodeUtf8
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import uos.dev.restcli.configs.DefaultMessageObfuscator
import uos.dev.restcli.configs.EnvironmentConfigs

class EnvironmentVariableInjectorByteStringTest {
    private val environmentVariableInjector: EnvironmentVariableInjector =
        EnvironmentVariableInjectorImpl(FakeDynamicVariableProvider())

    @Test
    fun inject_normal_variable() {
        val input = "Authorization: Basic {{username}} {{password}}".encodeUtf8()
        val result = environmentVariableInjector.inject(input, NORMAL_ENVIRONMENT)
        assertThat(result).isEqualTo("Authorization: Basic admin 123456".encodeUtf8())
    }

    @Test
    fun inject_specific_variable() {
        val input = "Authorization: Basic {{username}} {{password}}".encodeUtf8()
        val result = environmentVariableInjector.inject(input, SPECIFIC_ENVIRONMENT)
        assertThat(result).isEqualTo("Authorization: Basic {{password}} 123456".encodeUtf8())
    }

    @Test
    fun inject_with_no_environment_define() {
        val input = "Authorization: Basic {{username}} {{password}}".encodeUtf8()
        val result = environmentVariableInjector.inject(input, EnvironmentConfigs())
        assertThat(result).isEqualTo("Authorization: Basic {{username}} {{password}}".encodeUtf8())
    }

    @Test
    fun inject_dynamic_variables() {
        val input = "http://httpbin.org/anything?id={{\$uuid}}&ts={{\$timestamp}}".encodeUtf8()
        val result = environmentVariableInjector.inject(input, EnvironmentConfigs())
        val expect =
            "http://httpbin.org/anything?id=${FakeDynamicVariableProvider.FAKE_UUID}&ts=${FakeDynamicVariableProvider.FAKE_TIMESTAMP}"
        assertThat(result).isEqualTo(expect.encodeUtf8())
    }

    @Test
    @DisplayName("Obfuscate private variable")
    fun obfuscatePrivateVariable() {
        val input = "Authorization: Basic {{username}} {{password}}".encodeUtf8()
        val result = environmentVariableInjector.inject(input, NORMAL_ENVIRONMENT)
        val obfuscator = DefaultMessageObfuscator(NORMAL_ENVIRONMENT)
        val actual = obfuscator.obfuscate(result.utf8())
        assertThat(actual).isEqualTo("Authorization: Basic *** ***")
    }

    companion object {
        private val NORMAL_ENVIRONMENT: EnvironmentConfigs = EnvironmentConfigs.from(
            mapOf(
                "auth_token" to "AUTH_TOKEN",
                "username" to "admin",
                "password" to "123456"
            ), true
        )
        private val SPECIFIC_ENVIRONMENT: EnvironmentConfigs = EnvironmentConfigs.from(
            mapOf(
                "auth_token" to "AUTH_TOKEN",
                "username" to "{{password}}",
                "password" to "123456"
            ), true
        )
    }
}

