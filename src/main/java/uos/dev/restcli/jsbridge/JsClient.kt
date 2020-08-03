package uos.dev.restcli.jsbridge

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Response
import okhttp3.ResponseBody
import org.apache.commons.text.StringEscapeUtils
import org.intellij.lang.annotations.Language
import javax.script.ScriptEngine
import javax.script.ScriptEngineManager

class JsClient {
    private val engine: ScriptEngine = ScriptEngineManager().getEngineByName("nashorn")

    init {
        val reader = javaClass.classLoader.getResourceAsStream("client.js")?.reader()
        engine.eval(reader)
    }

    fun execute(testScript: String) {
        engine.eval(testScript)
    }

    // TODO: Make abstract from okhttp response.
    fun updateResponse(response: Response) {
        val url = StringEscapeUtils.escapeEcmaScript(response.request.url.toString())
        val updateRequestUrlScript = """response.url = "$url";"""
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
            $updateRequestUrlScript;
            $updateBodyScript;
            $updateHeaderScript;
            $updateContentTypeScript;
            response.status = ${response.code};
        """.trimIndent()
        log("===== UPDATE RESPONSE SCRIPT ====")
        log(script)
        log("=================================")
        engine.eval(script)
        engine.eval("response.contentType")
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
            println(message)
        }
    }

    companion object {
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
        private const val DEBUG = false
    }
}
