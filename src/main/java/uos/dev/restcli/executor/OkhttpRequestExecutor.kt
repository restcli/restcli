package uos.dev.restcli.executor

import com.github.ajalt.mordant.TermColors
import mu.KotlinLogging
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.internal.tls.OkHostnameVerifier
import okhttp3.logging.HttpLoggingInterceptor
import org.apache.commons.validator.routines.RegexValidator
import org.apache.commons.validator.routines.UrlValidator
import org.intellij.lang.annotations.Language
import uos.dev.restcli.configs.MessageObfuscator
import uos.dev.restcli.parser.Request
import java.security.cert.CertificateException
import java.util.concurrent.TimeUnit
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import okhttp3.Request as OkhttpRequest

class OkhttpRequestExecutor(
    private val logLevel: HttpLoggingInterceptor.Level = HttpLoggingInterceptor.Level.BODY,
    obfuscator: MessageObfuscator,
    private val insecure: Boolean,
    private val requestTimeout: Long,
) : RequestExecutor {
    @Suppress("RegExpRedundantEscape")
    @Language("RegExp")
    private val urlValidator: UrlValidator = UrlValidator(
        RegexValidator("^[a-zA-Z0-9]([a-zA-Z0-9\\-\\.]*[a-zA-Z0-9])?(:\\d+)?"), UrlValidator.ALLOW_LOCAL_URLS
    )
    private val loggingInterceptor: Interceptor =
        HttpLoggingInterceptor(CustomLogger(obfuscator)).apply { setLevel(logLevel) }

    private val hostnameVerifier = if (insecure) HostnameVerifier { _, _ -> true } else OkHostnameVerifier

    private fun OkHttpClient.Builder.addSSLFactory(): OkHttpClient.Builder {
        if (insecure) {
            // init untrustmanager https://gist.github.com/maiconhellmann/c61a533eca6d41880fd2b3f8459c07f7
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                @kotlin.jvm.Throws(CertificateException::class)
                override fun checkClientTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {
                }

                @kotlin.jvm.Throws(CertificateException::class)
                override fun checkServerTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {
                }

                override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> {
                    return arrayOf()
                }
            })

            // Install the all-trusting trust manager
            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, java.security.SecureRandom())
            // Create an ssl socket factory with our all-trusting manager
            val sslSocketFactory = sslContext.socketFactory
            this.sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
        }
        return this
    }

    private val okHttpClient: OkHttpClient =
        OkHttpClient.Builder().addInterceptor(loggingInterceptor).hostnameVerifier(hostnameVerifier).addSSLFactory()
            .build()

    override fun execute(request: Request): Response {
        val client = okHttpClient.newBuilder().followRedirects(request.isFollowRedirects)
            .callTimeout(requestTimeout, TimeUnit.MILLISECONDS).build()
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
        val builder = MultipartBody.Builder().setType(MultipartBody.FORM)
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
        val path = request.requestTarget.replace(" ", "+")
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
        val isStartWithHttp = host.startsWith("http://") || host.startsWith("https://")
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

    private fun Request.Part.createRequestBody(): RequestBody = body.orEmpty().toRequestBody(contentType)

    private val Request.Part.contentType: MediaType?
        get() {
            val headerContentType = headers.entries.firstOrNull {
                it.key.equals("Content-Type", ignoreCase = true)
            } ?: return null
            return headerContentType.value.toMediaTypeOrNull()
        }

    private class CustomLogger(private val obfuscator: MessageObfuscator) : HttpLoggingInterceptor.Logger {
        private val logger = KotlinLogging.logger {}
        private val t: TermColors = TermColors()
        override fun log(message: String) {
            val log = obfuscator.obfuscate(message)
            logger.info(t.gray(log))
        }
    }
}

