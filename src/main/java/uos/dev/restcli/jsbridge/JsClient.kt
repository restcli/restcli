@file:Suppress("DEPRECATION")

package uos.dev.restcli.jsbridge

import mu.KotlinLogging
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Response
import okhttp3.ResponseBody
import org.apache.commons.text.StringEscapeUtils
import org.graalvm.polyglot.Context
import org.graalvm.polyglot.HostAccess
import org.intellij.lang.annotations.Language

class JsClient(private val context: Context) {
    constructor() : this(Context.newBuilder().allowAllAccess(true).allowHostAccess(HostAccess.ALL).build())

    private val logger = KotlinLogging.logger {}

    init {
        val reader = javaClass.classLoader.getResourceAsStream("client.js")?.reader()
        context.eval("js", reader?.readText())
    }

    fun execute(testScript: String) {
        context.eval("js", testScript)
    }

    // TODO: Make abstract from okhttp response.
    fun updateResponse(response: Response) {
        val updateHeaderScriptBuilder = StringBuilder()
        updateHeaderScriptBuilder.append("response.headers = new ResponseHeaders();")
        response.headers.forEach {
            val headerName = StringEscapeUtils.escapeEcmaScript(it.first)
            val headerValue = StringEscapeUtils.escapeEcmaScript(it.second)

            @Language("JavaScript")
            val script = """response.headers.add("$headerName", "$headerValue");"""
            updateHeaderScriptBuilder.append(script)
        }
        val updateHeaderScript = updateHeaderScriptBuilder.toString()
        val isJsonContent = response.body?.isJsonContent ?: false

        val body = response.body
        val contentType = body?.contentType()
        val mimeType = contentType?.toString().orEmpty()
        val charset = contentType?.charset()?.toString().orEmpty()
        val mimeTypeEscape = StringEscapeUtils.escapeEcmaScript(mimeType)
        val charsetEscape = StringEscapeUtils.escapeEcmaScript(charset)

        @Language("JavaScript")
        val updateContentTypeScript = """
            response.contentType = new ContentType();
            response.contentType.mimeType = "$mimeTypeEscape";
            response.contentType.charset = "$charsetEscape";
        """.trimIndent()

        val rawBody = body?.string().orEmpty()
        val rawBodyEscape = StringEscapeUtils.escapeEcmaScript(rawBody)

        @Language("JavaScript")
        val updateBodyScript = if (isJsonContent && rawBody.isNotEmpty()) {
            """response.body = JSON.parse("$rawBodyEscape");"""
        } else {
            """response.body = "$rawBodyEscape";"""
        }


        @Language("JavaScript")
        val script = """
            $updateBodyScript;
            $updateHeaderScript;
            $updateContentTypeScript;
            response.status = ${response.code};
        """.trimIndent()
        log("===== UPDATE RESPONSE SCRIPT ====")
        log(script)
        log("=================================")
        context.eval("js", script)
        context.eval("js", "response.contentType")
    }

    private val ResponseBody.isJsonContent: Boolean
        get() {
            val contentType = this.contentType() ?: return false
            return contentType.type == JSON_MEDIA_TYPE.type
                    && contentType.subtype == JSON_MEDIA_TYPE.subtype
        }

    private fun log(message: String) {
        @Suppress("ConstantConditionIf")
        if (DEBUG) {
            logger.info(message)
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun globalEnvironment(): Map<String, String> {
        @Language("JavaScript")
        val result = context.eval("js", "client.global.store") as? Map<String, String>
        globalVariables + (result ?: emptyMap())
        return globalVariables
    }

    fun close() {
        context.close();
    }

    companion object {
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
        private const val DEBUG = false
        private val globalVariables = emptyMap<String, String>()
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
