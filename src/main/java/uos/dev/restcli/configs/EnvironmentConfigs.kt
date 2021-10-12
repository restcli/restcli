package uos.dev.restcli.configs

class EnvironmentConfigs(
    private val configs: Map<String, EnvironmentConfig>,
    val configDecorator: PrivateConfigDecorator
) {

    constructor() : this(mapOf<String, EnvironmentConfig>(), defaultDecorator)

    constructor(configs: Map<String, EnvironmentConfig>) : this(configs, defaultDecorator)

    fun with(key: String, environmentConfig: EnvironmentConfig): EnvironmentConfigs {
        return EnvironmentConfigs(configs + Pair(key, environmentConfig), configDecorator)
    }

    fun overwriteMergeWith(other: EnvironmentConfigs): EnvironmentConfigs {
        return other.configs.keys.fold(this) { acc, key -> acc.with(key, other.configs[key]!!) }
    }

    fun containsKey(key: String): Boolean = configs.containsKey(key)

    fun getValue(key: String, decorated: Boolean = false): String? =
        if (configs.containsKey(key)) {
            if (!decorated || !configs[key]!!.isPrivate) {
                configs[key]!!.value
            } else {
                configDecorator.decorate(configs[key]!!.value)
            }
        } else {
            null
        }

    fun obfuscate(message: String): String {
        return configs.entries.fold(message) { acc, entry ->
            acc.replace(this.getValue(entry.key)!!, this.getValue(entry.key, true)!!)
        }
    }

    companion object {
        var defaultDecorator: PrivateConfigDecorator = ThreeStarConfigDecorator

        fun changeDefaultDecorator(decorator: PrivateConfigDecorator) {
            defaultDecorator = decorator
        }

        fun from(configs: Map<String, String>, isPrivate: Boolean) =
            EnvironmentConfigs(configs.mapValues { EnvironmentConfig(it.value, isPrivate) }, defaultDecorator)

        fun from(configs: Map<String, String>, isPrivate: Boolean, decorator: PrivateConfigDecorator) =
            EnvironmentConfigs(configs.mapValues { EnvironmentConfig(it.value, isPrivate) }, decorator)
    }
}
