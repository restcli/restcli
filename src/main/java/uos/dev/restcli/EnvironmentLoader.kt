package uos.dev.restcli

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import uos.dev.restcli.configs.EnvironmentConfig
import uos.dev.restcli.configs.EnvironmentConfigs
import java.io.File

/**
 * A loader class for support loading environment variables for http client.
 */
class EnvironmentLoader {

    fun load(environmentFilesDirectory: String, environmentName: String): EnvironmentConfigs {
        val privateEnvConfig = loadConfig(environmentFilesDirectory, PRIVATE_ENV_FILE, environmentName)
        val publicEnvConfig = loadConfig(environmentFilesDirectory, PUBLIC_ENV_FILE, environmentName)
        val publicConfigs = getEnvironmentConfigs(publicEnvConfig, false)
        val privateConfigs = getEnvironmentConfigs(privateEnvConfig, true)

        return publicConfigs.overwriteMergeWith(privateConfigs)
    }

    private fun getEnvironmentConfigs(publicEnvConfig: JsonObject?, isPrivate: Boolean) =
        publicEnvConfig?.keySet()?.fold(EnvironmentConfigs()) { acc, key ->
            getEnvironmentConfigs(publicEnvConfig, key, isPrivate)?.let { config -> acc.with(key, config) } ?: acc
        } ?: EnvironmentConfigs()

    private fun getEnvironmentConfigs(jsonConfig: JsonObject, key: String, isPrivate: Boolean): EnvironmentConfig? =
        jsonConfig.get(key).asStringOrNull?.let { value -> EnvironmentConfig(value, isPrivate) }

    private fun loadConfig(
        environmentFilesDirectory: String,
        httpClientEnvConfigFilePath: String,
        environmentName: String
    ): JsonObject? {
        val file = File(File(environmentFilesDirectory).absolutePath, httpClientEnvConfigFilePath)
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
