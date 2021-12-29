package uos.dev.restcli.configs

object ThreeStarConfigDecorator : PrivateConfigDecorator() {
    override fun decorate(value: String): String = "***"
}
