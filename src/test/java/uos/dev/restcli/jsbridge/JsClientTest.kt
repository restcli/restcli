package uos.dev.restcli.jsbridge

import com.google.common.truth.Truth.assertThat
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test

class JsClientTest {

    @Test
    fun graaljs() {
        testGlobalEnvironment(JsClient())
    }


    private fun testGlobalEnvironment(jsClient: JsClient) {
        @Language("JavaScript")
        val envSet = """client.global.set("env_key", "env_value")"""
        jsClient.execute(envSet)

        assertThat(jsClient.globalEnvironment()["env_key"]).isEqualTo("env_value")
    }
}
