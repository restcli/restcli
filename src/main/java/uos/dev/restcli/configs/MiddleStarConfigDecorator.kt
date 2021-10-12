package uos.dev.restcli.configs

object MiddleStarConfigDecorator : PrivateConfigDecorator() {
    private var clearSize: Int = 1

    fun changeClearSize(newSize: Int) {
        clearSize = newSize
    }

    override fun decorate(value: String): String = value.substring(0, clearSize) +
            Regex(".").replace(value.substring(clearSize, value.length - clearSize), "*") +
            value.reversed().substring(0, clearSize).reversed()
}
