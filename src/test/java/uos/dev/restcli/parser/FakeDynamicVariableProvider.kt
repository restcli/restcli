package uos.dev.restcli.parser

class FakeDynamicVariableProvider : DynamicVariableProvider {
    override fun invoke(name: String): String? = when (name) {
        "\$uuid" -> FAKE_UUID
        "\$timestamp" -> FAKE_TIMESTAMP
        "\$randomInt" -> FAKE_RANDOM_INT
        else -> null
    }

    companion object {
        const val FAKE_UUID: String = "e9e87c05-82eb-4522-bc47-f0fcfdde4cab"
        const val FAKE_TIMESTAMP: String = "1563362218"
        const val FAKE_RANDOM_INT: String = "123"
    }
}
