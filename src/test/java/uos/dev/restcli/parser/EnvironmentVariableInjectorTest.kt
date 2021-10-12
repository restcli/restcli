package uos.dev.restcli.parser

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import uos.dev.restcli.configs.EnvironmentConfigs

class EnvironmentVariableInjectorTest {
    private val environmentVariableInjector: EnvironmentVariableInjector =
        EnvironmentVariableInjectorImpl(FakeDynamicVariableProvider())

    @Test
    fun inject_normal_variable() {
        val input = "Authorization: Basic {{username}} {{password}}"
        val result = environmentVariableInjector.inject(input, NORMAL_ENVIRONMENT)
        assertThat(result).isEqualTo("Authorization: Basic admin 123456")
    }

    @Test
    fun inject_specific_variable() {
        val input = "Authorization: Basic {{username}} {{password}}"
        val result = environmentVariableInjector.inject(input, SPECIFIC_ENVIRONMENT)
        assertThat(result).isEqualTo("Authorization: Basic {{password}} 123456")
    }

    @Test
    fun inject_with_no_environment_define() {
        val input = "Authorization: Basic {{username}} {{password}}"
        val result = environmentVariableInjector.inject(input, EnvironmentConfigs())
        assertThat(result).isEqualTo("Authorization: Basic {{username}} {{password}}")
    }

    @Test
    fun inject_dynamic_variables() {
        val input = "http://httpbin.org/anything?id={{\$uuid}}&ts={{\$timestamp}}"
        val result = environmentVariableInjector.inject(input, EnvironmentConfigs())
        val expect =
            "http://httpbin.org/anything?id=${FakeDynamicVariableProvider.FAKE_UUID}&ts=${FakeDynamicVariableProvider.FAKE_TIMESTAMP}"
        assertThat(result).isEqualTo(expect)
    }

    @Test
    @DisplayName("Obfuscate private variable")
    fun obfuscatePrivateVariable() {
        val input = "Authorization: Basic {{username}} {{password}}"
        val result = environmentVariableInjector.inject(input, true, NORMAL_ENVIRONMENT)
        assertThat(result).isEqualTo("Authorization: Basic *** ***")
    }

    companion object {
        private val NORMAL_ENVIRONMENT: EnvironmentConfigs = EnvironmentConfigs.from(mapOf(
            "auth_token" to "AUTH_TOKEN",
            "username" to "admin",
            "password" to "123456"
        ), true)
        private val SPECIFIC_ENVIRONMENT: EnvironmentConfigs = EnvironmentConfigs.from(mapOf(
            "auth_token" to "AUTH_TOKEN",
            "username" to "{{password}}",
            "password" to "123456"
        ), true)
    }
}

