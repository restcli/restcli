package uos.dev.restcli.report

object TestReportStore {
    private val _testReports: MutableList<TestReport> = mutableListOf()
    val testReports: List<TestReport> get() = _testReports

    @JvmOverloads
    @JvmStatic
    fun add(name: String, isPassed: Boolean, details: String? = null) {
        val report = TestReport(name = name, isPassed = isPassed, details = details.orEmpty())
        _testReports.add(report)
    }

    fun clear() {
        _testReports.clear()
    }
}
