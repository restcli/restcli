package uos.dev.restcli.report

data class TestGroupReport(
    val name: String
) {
    private val _testReports: MutableList<TestReport> =
        mutableListOf()
    val testReports: List<TestReport> get() = _testReports

    fun addTestReport(testReport: TestReport) {
        _testReports += testReport
    }
}
