package uos.dev.restcli.jsbridge

import mu.KotlinLogging
import org.intellij.lang.annotations.Language
import javax.script.ScriptEngine
import javax.script.ScriptEngineManager

class ScriptEngineAdapter(
    private val javaVersion: JavaVersion = JavaVersion()
) : ScriptEngine by scriptEngine(javaVersion.jsEngineName) {

    private val logger = KotlinLogging.logger {}

    init {
        logger.info("engineName: ${javaVersion.jsEngineName}")
    }

    @Suppress("UNCHECKED_CAST")
    fun store(): Map<String, String> {
        @Language("JavaScript")
        val result = eval("client.global.store") as? Map<String, String>
        return result ?: emptyMap()
    }

    companion object {
        fun ScriptEngine.store() = (this as ScriptEngineAdapter).store()
        private fun scriptEngine(engineName: String) = ScriptEngineManager().getEngineByName(engineName)
    }

    class JavaVersion(private val versionElements: String = System.getProperty("java.version")) {
        init {
            if (useGraalJs()) {
                System.setProperty("polyglot.js.nashorn-compat", "true")
            }
        }

        val jsEngineName: String
            get() = if (useGraalJs()) "graal.js" else "nashorn"

        private fun useGraalJs(): Boolean {
            val versionElements = versionElements.split(".")
            val discard = versionElements[0].toInt()
            return if (discard == 1) {
                versionElements[1].toInt()
            } else {
                discard
            } >= NASHORN_REMOVED_VERSION
        }

        companion object {
            private const val NASHORN_REMOVED_VERSION = 15
        }
    }
}

