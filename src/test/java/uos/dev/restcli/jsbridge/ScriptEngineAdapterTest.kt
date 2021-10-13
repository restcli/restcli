package uos.dev.restcli.jsbridge

import com.google.common.truth.Truth.assertThat
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledForJreRange
import org.junit.jupiter.api.condition.EnabledOnJre
import org.junit.jupiter.api.condition.JRE
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import uos.dev.restcli.jsbridge.ScriptEngineAdapter.*
import uos.dev.restcli.jsbridge.ScriptEngineAdapter.Companion.store
import javax.script.ScriptEngine

class ScriptEngineAdapterTest {
    private val clientJs = javaClass.classLoader.getResourceAsStream("client.js")?.bufferedReader()?.readText()

    @Language("JavaScript")
    val evalStatement = """client.global.set("key", "value")"""

    @Test
    @EnabledForJreRange(max = JRE.JAVA_14)
    fun nashorn() {
        assertEngineEval(ScriptEngineAdapter(JavaVersion("1.8")))
    }

    @Test
    @EnabledForJreRange(min = JRE.JAVA_15)
    fun graaljs() {
        assertEngineEval(ScriptEngineAdapter(JavaVersion("15")))
    }

    @ParameterizedTest
    @CsvSource(
        value = [
            "1.8.0_265,nashorn",
            "14,nashorn",
            "15,graal.js"
        ]
    )
    fun testJavaVersion(version: String, engineName: String) {
        assertThat(JavaVersion(version).jsEngineName).isEqualTo(engineName)
    }

    private fun assertEngineEval(scriptEngine: ScriptEngine) {
        scriptEngine.eval(
            """
            $clientJs
            $evalStatement
            """.trimIndent()
        )

        assertThat(scriptEngine.store()["key"]).isEqualTo("value")
    }
}
