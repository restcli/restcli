package uos.dev.restcli.report

import mu.KotlinLogging
import java.io.File
import java.io.FileWriter

class TestReportPrinter(private val testReportName: String) {
    private val logger = KotlinLogging.logger {}
    fun print(testGroupReports: List<TestGroupReport>) {
        logger.info("[START] Creating test report: $testReportName")
        val result = runCatching {
            val reportName = testReportName.ifBlank { DEFAULT_TEST_REPORT_NAME }
            val reportDirectory = File(TEST_REPORTS_FOLDER_NAME).apply { mkdirs() }
            val reportFile = File(reportDirectory, "$reportName.xml")
            FileWriter(reportFile).use { JunitTestReportGenerator().generate(testGroupReports, it) }
        }
        result.onFailure {
            it.printStackTrace()
        }
        logger.info("[FINISHED]")
    }

    companion object {
        private const val TEST_REPORTS_FOLDER_NAME = "test-reports"
        private const val DEFAULT_TEST_REPORT_NAME = "rest-cli-test-report"
    }
}
