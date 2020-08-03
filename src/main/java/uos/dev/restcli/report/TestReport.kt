package uos.dev.restcli.report

data class TestReport(
    val name: String,
    val isPassed: Boolean,
    val details: String
)
