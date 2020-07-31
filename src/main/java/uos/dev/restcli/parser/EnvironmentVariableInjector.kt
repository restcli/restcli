package uos.dev.restcli.parser

object EnvironmentVariableInjector {
    private val VARIABLE_REGEX: Regex = "\\{\\{(.*?)}}".toRegex()
    private const val VARIABLE_GROUP_INDEX: Int = 1

    fun inject(source: String, environment: Map<String, String>): String {
        var result = source
        val matches = VARIABLE_REGEX.findAll(source).toList()
        // MUST replace variable string reversed for keeping match index.
        matches.asReversed().forEach {
            val variableName = it.groupValues[VARIABLE_GROUP_INDEX]
            if (!environment.containsKey(variableName)) {
                println("WARNING: Define $variableName but there is no define in environment")
                return@forEach
            }
            val variableValue = environment[variableName].orEmpty()
            result = result.replaceRange(it.range, variableValue)
        }
        return result
    }
}
