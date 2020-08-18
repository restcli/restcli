package uos.dev.restcli.report

object TestReportStore {
    var nextRequestName: String? = null
        private set

    private val _testGroupReports: MutableList<TestGroupReport> = mutableListOf()
    val testGroupReports: List<TestGroupReport> get() = _testGroupReports

    fun addTestGroupReport(name: String, trace: TestGroupReport.Trace) {
        _testGroupReports += TestGroupReport(name, trace)
    }

    @Suppress("unused") // Used in client.js.
    @JvmStatic
    fun setNextRequest(requestName: String?) {
        nextRequestName = requestName
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
