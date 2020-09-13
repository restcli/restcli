package uos.dev.restcli.parser

import uos.dev.restcli.CustomEnvironment

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

        return request.copy(
            requestTarget = inject(request.requestTarget),
            headers = inject(request.headers),
            body = request.body?.let(::inject),
            parts = request.parts.map(::inject)
        )
    }
}
