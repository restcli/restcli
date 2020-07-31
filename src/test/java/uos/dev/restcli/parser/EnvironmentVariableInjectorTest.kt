package uos.dev.restcli.parser

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class EnvironmentVariableInjectorTest {

    @Test
    fun inject_normal_variable() {
        val input = "Authorization: Basic {{username}} {{password}}"
        val result = EnvironmentVariableInjector.inject(input, NORMAL_ENVIRONMENT)
        assertThat(result).isEqualTo("Authorization: Basic admin 123456")
    }

    @Test
    fun inject_specific_variable() {
        val input = "Authorization: Basic {{username}} {{password}}"
        val result = EnvironmentVariableInjector.inject(input, SPECIFIC_ENVIRONMENT)
        assertThat(result).isEqualTo("Authorization: Basic {{password}} 123456")
    }

    @Test
    fun inject_with_no_environment_define() {
        val input = "Authorization: Basic {{username}} {{password}}"
        val result = EnvironmentVariableInjector.inject(input, emptyMap())
        assertThat(result).isEqualTo("Authorization: Basic {{username}} {{password}}")
    }

    companion object {
        private val NORMAL_ENVIRONMENT: Map<String, String> = mapOf(
            "auth_token" to "AUTH_TOKEN",
            "username" to "admin",
            "password" to "123456"
        )
        private val SPECIFIC_ENVIRONMENT: Map<String, String> = mapOf(
            "auth_token" to "AUTH_TOKEN",
            "username" to "{{password}}",
            "password" to "123456"
        )
    }
}

