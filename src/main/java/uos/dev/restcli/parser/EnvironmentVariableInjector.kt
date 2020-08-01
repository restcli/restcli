package uos.dev.restcli.parser

interface EnvironmentVariableInjector {
    fun inject(source: String, environment: Map<String, String>): String
}

class EnvironmentVariableInjectorImpl(
    private val dynamicVariableProvider: DynamicVariableProvider = DynamicVariableProviderImpl()
) : EnvironmentVariableInjector {
    override fun inject(source: String, environment: Map<String, String>): String {
        var result = source
        val matches = VARIABLE_REGEX.findAll(source).toList()
        // MUST replace variable string reversed for keeping match index.
        matches.asReversed().forEach {
            val variableName = it.groupValues[VARIABLE_GROUP_INDEX]
            val variableValue = obtainVariableValue(variableName, environment)
            if (variableValue is VariableValue.Value) {
                result = result.replaceRange(it.range, variableValue.value)
            }
        }
        return result
    }

    private fun obtainVariableValue(
        variableName: String,
        environment: Map<String, String>
    ): VariableValue {
        val isDynamicVariable = variableName.startsWith("$")
        if (isDynamicVariable) {
            val dynamicVariableValue = dynamicVariableProvider(variableName)
            return if (dynamicVariableValue == null) {
                println("WARNING: dynamic variable $variableName is not supported, fallback null")
                VariableValue.Unknown
            } else {
                VariableValue.Value(dynamicVariableValue)
            }
        }
        if (!environment.containsKey(variableName)) {
            println("WARNING: Define $variableName but there is no define in environment")
            return VariableValue.Unknown
        }
        val variableValue = environment[variableName].toString()
        return VariableValue.Value(variableValue)
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
