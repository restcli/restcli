package uos.dev.restcli.executor

import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import uos.dev.restcli.parser.Request
import okhttp3.Request as OkhttpRequest

class OkhttpRequestExecutor : RequestExecutor {
    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .build()

    override fun execute(request: Request) {
        println("Execute: ${request.requestTarget}")
        val builder = OkhttpRequest.Builder()
        builder.url(request.requestTarget)
        val body = request.body?.toRequestBody()
        builder.method(request.method.name, body)
        request.headers.forEach { (name, value) ->
            builder.addHeader(name, value)
        }
        val response = okHttpClient.newCall(builder.build()).execute()
        println(response.body?.string())
    }
}
