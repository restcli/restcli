package uos.dev.restcli.configs

object NoopConfigDecorator : PrivateConfigDecorator() {
    override fun decorate(value: String): String = value
}
