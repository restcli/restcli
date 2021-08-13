package uos.dev.restcli.configs

object StarConfigDecorator: PrivateConfigDecorator() {
    override fun decorate(value: String): String = Regex(".").replace(value, "*")
}
