package uos.dev.restcli.jsbridge

import com.google.common.truth.Truth.assertThat
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledForJreRange
import org.junit.jupiter.api.condition.JRE
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import uos.dev.restcli.jsbridge.JsClient.JavaVersion

class JsClientTest {

    @Test
    @EnabledForJreRange(max = JRE.JAVA_14)
    fun nashorn() {
        testGlobalEnvironment(JsClient())
    }

    @Test
    @EnabledForJreRange(min = JRE.JAVA_15)
    fun graaljs() {
        testGlobalEnvironment(JsClient())
    }

    @ParameterizedTest
    @CsvSource(
        "1.8.0_265,nashorn",
        "14,nashorn",
        "15,graal.js"
    )
    fun testJavaVersion(version: String, engineName: String) {
        assertThat(JavaVersion(version).jsEngineName).isEqualTo(engineName)
    }

    private fun testGlobalEnvironment(jsClient: JsClient) {
        @Language("JavaScript")
        val envSet = """client.global.set("env_key", "env_value")"""
        jsClient.execute(envSet)

        assertThat(jsClient.globalEnvironment()["env_key"]).isEqualTo("env_value")
    }
}
