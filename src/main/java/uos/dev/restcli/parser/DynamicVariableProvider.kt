package uos.dev.restcli.parser

import java.util.UUID
import kotlin.random.Random

interface DynamicVariableProvider {
    /**
     * Gets the dynamic variable with [name].
     * The dynamic variable **MUST** start with `$` character.
     * The `null` value will be returned if the dynamic variable is not supported or the variable
     * not start with `$`.
     */
    operator fun invoke(name: String): String?
}

class DynamicVariableProviderImpl : DynamicVariableProvider {
    override operator fun invoke(name: String): String? {
        val isDynamicVariableName = name.startsWith("$")
        if (!isDynamicVariableName) {
            return null
        }
        val nameWithoutDollarPrefix = name.removePrefix("$")
        val factory = DYNAMIC_VARIABLE_FACTORIES[nameWithoutDollarPrefix] ?: return null
        return factory()
    }

    companion object {
        private val DYNAMIC_VARIABLE_FACTORIES: Map<String, () -> String> = mapOf(
            // "uuid": {
            //   "description": "This dynamic variable generates a new UUID-v4",
            //   "sample-value": "e9e87c05-82eb-4522-bc47-f0fcfdde4cab"
            // }
            "uuid" to { UUID.randomUUID().toString() },

            // "timestamp": {
            //   "description": "This dynamic variable generates the current Unix timestamp",
            //   "sample-value": "1563362218"
            // }
            "timestamp" to { System.currentTimeMillis().toString() },

            // "randomInt": {
            //   "description": "This dynamic variable generates a random integer between 0 and 1000",
            //   "sample-value": "123"
            // }
            "randomInt" to { Random.nextInt(0, 1000).toString() }
        )
    }
}
