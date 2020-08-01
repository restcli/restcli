package uos.dev.restcli

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.File

/**
 * A loader class for support loading environment variables for http client.
 */
class EnvironmentLoader {

    fun load(environmentName: String): Map<String, String> {
        val privateEnvConfig = loadConfig(PRIVATE_ENV_FILE, environmentName)
        val publicEnvConfig = loadConfig(PUBLIC_ENV_FILE, environmentName)
        val result = mutableMapOf<String, String>()
        // Transfer all key/value from public env config to the result map.
        publicEnvConfig?.keySet()?.forEach { key ->
            publicEnvConfig.get(key).asStringOrNull?.let { value ->
                result[key] = value
            }
        }

        // Then transfer all key/value from private env config to the result map.
        // So, if there is the same key in private env config, this will override the public env
        // config.
        privateEnvConfig?.keySet()?.forEach { key ->
            privateEnvConfig.get(key).asStringOrNull?.let { value ->
                result[key] = value
            }
        }
        return result
    }

    private fun loadConfig(
        httpClientEnvConfigFilePath: String,
        environmentName: String
    ): JsonObject? {
        val file = File(httpClientEnvConfigFilePath)
        if (!file.exists()) {
            return null
        }
        val config = JsonParser.parseReader(file.reader())
        return config.asJsonObject.get(environmentName).asJsonObject
    }

    private val JsonElement.asStringOrNull: String?
        get() = try {
            asString
        } catch (_: Exception) {
            null
        }

    companion object {
        private const val PRIVATE_ENV_FILE = "http-client.private.env.json"
        private const val PUBLIC_ENV_FILE = "http-client.env.json"
    }
}
