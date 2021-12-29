package uos.dev.restcli

object Resource {
    fun getResourcePath(name: String): String {
        return javaClass.getResource(name)?.path.orEmpty()
    }
}
