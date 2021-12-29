package uos.dev.restcli.configs

class EnvironmentConfigs(
    val configs: Map<String, EnvironmentConfig>,
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

    fun getValue(key: String): String? = configs[key]?.value

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
