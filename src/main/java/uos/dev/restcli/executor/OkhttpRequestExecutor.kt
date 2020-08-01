package uos.dev.restcli.executor

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import uos.dev.restcli.parser.Request
import java.util.logging.Level
import java.util.logging.Logger
import okhttp3.Request as OkhttpRequest

class OkhttpRequestExecutor(
    private val logLevel: HttpLoggingInterceptor.Level = HttpLoggingInterceptor.Level.BODY
) : RequestExecutor {
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
        builder.url(request.requestTarget)
        val body = request.body?.toRequestBody()
        builder.method(request.method.name, body)
        request.headers.forEach { (name, value) ->
            builder.addHeader(name, value)
        }
        return client.newCall(builder.build()).execute()
    }

    private class CustomLogger : HttpLoggingInterceptor.Logger {
        private val logger: Logger = Logger.getLogger("RestCli")
        override fun log(message: String) = logger.log(Level.INFO, message)
    }
}
