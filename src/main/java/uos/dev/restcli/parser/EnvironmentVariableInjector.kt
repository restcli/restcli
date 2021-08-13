package uos.dev.restcli.parser

import mu.KotlinLogging
import uos.dev.restcli.configs.EnvironmentConfigs

interface EnvironmentVariableInjector {
    /**
     * Injects the variables in the [source] by checking the variables in [environments].
     * The strategy is taken the first environment that has the variable define.
     */

    fun inject(source: String, vararg environments: EnvironmentConfigs): String {
        return inject(source, false, *environments)
    }

    fun inject(source: String, decoratePrivate: Boolean, vararg environments: EnvironmentConfigs): String
}

class EnvironmentVariableInjectorImpl(
    private val dynamicVariableProvider: DynamicVariableProvider = DynamicVariableProviderImpl()
) : EnvironmentVariableInjector {
    private val logger = KotlinLogging.logger {}

    override fun inject(source: String, decoratePrivate: Boolean, vararg environments: EnvironmentConfigs): String {
        if (environments.isEmpty()) {
            return source
        }
        var result = source
        val matches = VARIABLE_REGEX.findAll(source).toList()
        // MUST replace variable string reversed for keeping match index.
        matches.asReversed().forEach {
            val variableName = it.groupValues[VARIABLE_GROUP_INDEX]
            val variableValue = obtainVariableValue(variableName, decoratePrivate, *environments)
            if (variableValue is VariableValue.Value) {
                result = result.replaceRange(it.range, variableValue.value)
            }
        }
        return result
    }

    private fun obtainVariableValue(
        variableName: String,
        decoratePrivate: Boolean,
        vararg environments: EnvironmentConfigs
    ): VariableValue {
        val isDynamicVariable = variableName.startsWith("$")
        if (isDynamicVariable) {
            val dynamicVariableValue = dynamicVariableProvider(variableName)
            return if (dynamicVariableValue == null) {
                logger.info("WARNING: dynamic variable $variableName is not supported, fallback null")
                VariableValue.Unknown
            } else {
                VariableValue.Value(dynamicVariableValue)
            }
        }
        val environment = environments.firstOrNull { it.containsKey(variableName) }

        if (environment == null) {
            logger.info("WARNING: Define $variableName but there is no define in environment")
            return VariableValue.Unknown
        }
        return VariableValue.Value(environment.getValue(variableName, decoratePrivate).toString())
    }

    private sealed class VariableValue {
        data class Value(val value: String) : VariableValue()
        object Unknown : VariableValue()
    }

    companion object {
        private val VARIABLE_REGEX: Regex = "\\{\\{(.*?)}}".toRegex()
        private const val VARIABLE_GROUP_INDEX: Int = 1
    }
}
