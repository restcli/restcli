package uos.dev.restcli.configs

abstract class PrivateConfigDecorator {
    abstract fun decorate(value: String): String
}
