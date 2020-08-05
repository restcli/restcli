package uos.dev.restcli.report

object TestReportStore {
    private val _testGroupReports: MutableList<TestGroupReport> = mutableListOf()
    val testGroupReports: List<TestGroupReport> get() = _testGroupReports

    fun addTestGroupReport(name: String) {
        _testGroupReports += TestGroupReport(name)
    }

    @Suppress("unused") // Used in client.js.
    @JvmStatic
    fun addTestReport(
        name: String,
        isPassed: Boolean,
        exception: String?,
        detail: String?
    ) {
        val report = TestReport(
            name = name,
            isPassed = isPassed,
            detail = detail.orEmpty(),
            exception = exception.orEmpty()
        )
        _testGroupReports.last().addTestReport(report)
    }

    fun clear() {
        _testGroupReports.clear()
    }
}
