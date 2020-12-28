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
            headers = inject(part.headers).entries.associate(authorizationTransform()),
            body = part.body?.let(::inject)
        )

        return request.copy(
            requestTarget = inject(request.requestTarget),
            headers = inject(request.headers),
            body = request.body?.let(::inject),
            parts = request.parts.map(::inject)
        )
    }

    private fun authorizationTransform(): ((Map.Entry<String, String>) -> Pair<String, String>) {
        return { entry ->
            val value = generateUsernamePasswordInBase64(entry.key, entry.value) ?: entry.value
            entry.key to value
        }
    }

    /**
     * Generates the value base64 with format: `Basic BASE64_USERNAME_PASSWORD_FORMAT` if the key/value is authorization
     * headers. Otherwise, returns null.
     */
    private fun generateUsernamePasswordInBase64(key: String, value: String): String? {
        if (!key.equals("authorization", ignoreCase = true)) {
            return null
        }
        val methodUsernamePassword = value.split("\\s+".toRegex(), limit = 3)
        val isBasicAuth =
            methodUsernamePassword[0].equals("basic", ignoreCase = true) && methodUsernamePassword.size == 3

        if (!isBasicAuth) {
            return null
        }
        val userPassword = "${methodUsernamePassword[1]}:${methodUsernamePassword[2]}"
        val basicAuth = Base64.getEncoder().encodeToString(userPassword.toByteArray())
        return "Basic $basicAuth"
    }
}
