@file:Suppress("DEPRECATION")

package uos.dev.restcli.jsbridge

import mu.KotlinLogging
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Response
import okhttp3.ResponseBody
import org.apache.commons.text.StringEscapeUtils
import org.graalvm.polyglot.Context
import org.graalvm.polyglot.Engine
import org.graalvm.polyglot.HostAccess
import org.graalvm.polyglot.Value
import org.intellij.lang.annotations.Language

class JsClient(private val context: Context) {
    constructor() : this(
        Context.newBuilder().allowAllAccess(true)
            .engine(
                Engine.newBuilder()
                    .option("engine.WarnInterpreterOnly", "false")
                    .build()
            )
            .allowHostAccess(HostAccess.ALL).build()
    )

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
    fun globalEnvironment(): Map<String, Any?> {

        @Language("JavaScript")
        val value = context.eval("js", "client.global.store")
        value.memberKeys.forEach { globalVariables[it] = convertValue(value.getMember(it)) }
        context.getBindings("js")
            .getMember("client")
            .getMember("global")
            .putMember("store", globalVariables)
        return globalVariables
    }

    private fun convertValue(value: Value?): Any? =
        value?.let {
            if (value.hasMembers()) {
                return value.memberKeys.map { it to convertValue(value.getMember(it)) }.toMap()
            } else if (value.hasArrayElements()) {
                return (0 until value.arraySize).map { value.getArrayElement(it) }.toList()
            } else if (value.isBoolean) {
                return value.asBoolean()
            } else if (value.isDate) {
                return value.asDate()
            } else if (value.isDuration) {
                return value.asDuration()
            } else if (value.isString) {
                return value.asString()
            } else if (value.isInstant) {
                return value.asInstant()
            } else if (value.isIterator) {
                return value.hasIterator()
            } else if (value.isNumber) {
                return value.asDouble()
            } else if (value.isTime) {
                return value.asTime()
            }
        }

    fun close() {
        context.close();
    }

    companion object {
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
        private const val DEBUG = false
        private val globalVariables = mutableMapOf<String, Any?>()
    }
}
