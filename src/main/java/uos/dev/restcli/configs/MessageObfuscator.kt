package uos.dev.restcli.configs

interface MessageObfuscator {
    fun obfuscate(message: String): String
}

class NoOpMessageObfuscator : MessageObfuscator {
    override fun obfuscate(message: String): String = message
}

class DefaultMessageObfuscator(
    private val environmentConfigs: EnvironmentConfigs,
    private val decorator: PrivateConfigDecorator = ThreeStarConfigDecorator
) : MessageObfuscator {
    override fun obfuscate(message: String): String {
        return environmentConfigs.configs.entries.fold(message) { acc, entry ->
            val isPrivate = entry.value.isPrivate
            val value = entry.value.value
            if (isPrivate) acc.replace(value, decorator.decorate(value)) else acc
        }
    }
}
