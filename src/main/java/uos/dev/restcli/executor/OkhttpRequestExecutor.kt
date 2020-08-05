package uos.dev.restcli.executor

import com.github.ajalt.mordant.TermColors
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import org.apache.commons.validator.routines.RegexValidator
import org.apache.commons.validator.routines.UrlValidator
import org.intellij.lang.annotations.Language
import uos.dev.restcli.parser.Request
import okhttp3.Request as OkhttpRequest

class OkhttpRequestExecutor(
    private val logLevel: HttpLoggingInterceptor.Level = HttpLoggingInterceptor.Level.BODY
) : RequestExecutor {
    @Suppress("RegExpRedundantEscape")
    @Language("RegExp")
    private val urlValidator: UrlValidator = UrlValidator(
        RegexValidator("^[a-zA-Z0-9]([a-zA-Z0-9\\-\\.]*[a-zA-Z0-9])?(:\\d+)?"),
        UrlValidator.ALLOW_LOCAL_URLS
    )
    private val loggingInterceptor: Interceptor = HttpLoggingInterceptor(CustomLogger())
        .apply { setLevel(logLevel) }

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    override fun execute(request: Request): Response {
        val client = okHttpClient.newBuilder()
            .followRedirects(request.isFollowRedirects)
            .build()
        val builder = OkhttpRequest.Builder()
        val requestTarget = obtainRequestTarget(request)
            ?: throw UnsupportedOperationException("Can't execute request target: ${request.requestTarget}")
        builder.url(requestTarget)
        val body = request.body?.toRequestBody() ?: request.createMultipartRequestBody()
        builder.method(request.method.name, body)
        request.headers.forEach { (name, value) ->
            builder.addHeader(name, value)
        }
        return client.newCall(builder.build()).execute()
    }

    private fun Request.createMultipartRequestBody(): RequestBody? {
        if (parts.isEmpty()) {
            return null
        }
        val builder = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
        parts.forEach { part ->
            if (part.fileName != null) {
                builder.addFormDataPart(part.name, part.fileName, part.createRequestBody())
            } else {
                builder.addFormDataPart(part.name, part.body.orEmpty())
            }
        }
        return builder.build()
    }

    private fun obtainRequestTarget(request: Request): String? {
        val path = request.requestTarget
        if (urlValidator.isValid(path)) {
            // 1. If the path is url valid -> used it.
            return path
        }

        // 2. Find the `Host` value from header.
        val hostKey = request.headers.keys.firstOrNull { it.equals("host", ignoreCase = true) }
        if (hostKey == null) {
            // 2.1. Host doesn't exist -> try to add `http://` prefix to the path then checking the
            // url valid status. If not valid, returns null.
            val pathWithHttpPrefix = "http://$path"
            return if (urlValidator.isValid(pathWithHttpPrefix)) pathWithHttpPrefix else null
        }
        val host = request.headers[hostKey] ?: return null

        // 3. Now, the host exist. We will ensure that the host starts with http/https prefix.
        val isStartWithHttp = host.startsWith("http://") ||
                host.startsWith("https://")
        val hostWithHttpPrefix = if (isStartWithHttp) host else "http://$host"

        return when {
            // Host not valid -> return null.
            !urlValidator.isValid(hostWithHttpPrefix) -> null

            // the path is asterisk -> return host.
            path == "*" -> hostWithHttpPrefix

            // If the path valid by checking start with `/` then combine host with path to get
            // request target.
            path.startsWith("/") -> "$hostWithHttpPrefix$path"

            else -> null
        }
    }

    private fun Request.Part.createRequestBody(): RequestBody =
        body.orEmpty().toRequestBody(contentType)

    private val Request.Part.contentType: MediaType?
        get() {
            val headerContentType = headers.entries.firstOrNull {
                it.key.equals("Content-Type", ignoreCase = true)
            } ?: return null
            return headerContentType.value.toMediaTypeOrNull()
        }

    private class CustomLogger : HttpLoggingInterceptor.Logger {
        private val t: TermColors = TermColors()
        override fun log(message: String) {
            println(t.gray(message))
        }
    }
}
