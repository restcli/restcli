package uos.dev.restcli.parser

import uos.dev.restcli.CustomEnvironment
import uos.dev.restcli.configs.EnvironmentConfigs
import java.util.*

class RequestEnvironmentInjector(
    private val environmentVariableInjector: EnvironmentVariableInjector =
        EnvironmentVariableInjectorImpl()
) {
    fun inject(
        request: Request,
        customEnvironment: CustomEnvironment,
        environment: EnvironmentConfigs,
        jsGlobalEnv: EnvironmentConfigs,
        decoratePrivate: Boolean = false
    ): Request {


        fun envInject(source: String): String = environmentVariableInjector.inject(
            source,
            decoratePrivate,
            customEnvironment.privateEnv,
            customEnvironment.publicEnv,
            jsGlobalEnv,
            environment
        )

        fun headerInject(headers: Map<String, String>): Map<String, String> =
            headers.entries.associate { envInject(it.key) to envInject(it.value) }
                .entries.associate(::authorizationTransform)

        fun requestPartInject(part: Request.Part): Request.Part = part.copy(
            headers = headerInject(part.headers),
            body = part.body?.let(::envInject)
        )

        return request.copy(
            requestTarget = envInject(request.requestTarget),
            headers = headerInject(request.headers),
            body = request.body?.let(::envInject),
            parts = request.parts.map(::requestPartInject)
        )
    }

    private fun authorizationTransform(entry: Map.Entry<String, String>): Pair<String, String> {
        val value = generateUsernamePasswordInBase64(entry.key, entry.value) ?: entry.value
        return entry.key to value
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
