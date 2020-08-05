package uos.dev.restcli.report

data class TestReport(
    val name: String,
    val isPassed: Boolean,
    val exception: String? = null,
    val detail: String = ""
)
