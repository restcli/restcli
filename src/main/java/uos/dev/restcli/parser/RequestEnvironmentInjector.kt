package uos.dev.restcli.parser

import uos.dev.restcli.CustomEnvironment
import java.util.*

class RequestEnvironmentInjector(
    private val environmentVariableInjector: EnvironmentVariableInjector =
        EnvironmentVariableInjectorImpl()
) {
    fun inject(
        request: Request,
        customEnvironment: CustomEnvironment,
        environment: Map<String, String>,
        jsGlobalEnv: Map<String, String>
    ): Request {


        fun inject(source: String): String = environmentVariableInjector.inject(
            source,
            customEnvironment.privateEnv,
            customEnvironment.publicEnv,
            jsGlobalEnv,
            environment
        )

        fun inject(headers: Map<String, String>): Map<String, String> {
            val result = mutableMapOf<String, String>()
            headers.forEach { (key, value) -> result[inject(key)] = inject(value) }
            return result
        }

        fun inject(part: Request.Part): Request.Part = part.copy(
            headers = inject(part.headers),
            body = part.body?.let(::inject)
        )

        fun authorization(headers: Map<String, String>): Map<String, String> {
            val result = mutableMapOf<String, String>()
            headers.forEach { (key: String, value: String) ->
                if (key.equals("authorization",ignoreCase = true)) {
                    // format Basic Auth
                   val methodUsernamePassword = value.split("\\s+".toRegex(),limit=3)
                    if (methodUsernamePassword[0].equals("basic",ignoreCase = true) && methodUsernamePassword.size == 3 ) {
                       val userPassword: String = (methodUsernamePassword[1]+":"+methodUsernamePassword[2])
                        val basicAuth = Base64.getEncoder().encodeToString(userPassword.toByteArray())
                        result[key] = "Basic $basicAuth"
                    }else{
                        result[key] = value
                    }
                }else {
                    result[key] = value
                }
            }
            return result
        }

        return request.copy(
            requestTarget = inject(request.requestTarget),
            headers = authorization(inject(request.headers)),
            body = request.body?.let(::inject),
            parts = request.parts.map(::inject)
        )
    }
}
