package uos.dev.restcli.report

import mu.KotlinLogging
import java.io.File
import java.io.FileWriter

class TestReportPrinter(private val httpFilePath: String) {
    private val logger = KotlinLogging.logger {}
    fun print(testGroupReports: List<TestGroupReport>) {
        val testReportName = File(httpFilePath).nameWithoutExtension
        logger.info("[START] Creating test report: $testReportName")
        val result = runCatching {
            val reportName = testReportName.ifBlank { DEFAULT_TEST_REPORT_NAME }
            val reportFile = generateReportFile(reportName)
            FileWriter(reportFile).use { JunitTestReportGenerator().generate(testGroupReports, it) }
        }
        result.onFailure {
            it.printStackTrace()
        }
        logger.info("[FINISHED]")
    }

    private fun generateReportFile(reportName: String): File {
        val reportDirectory = File(TEST_REPORTS_FOLDER_NAME).apply { mkdirs() }
        var reportFile = File(reportDirectory, "$reportName.xml")
        var index = 1
        while (reportFile.exists()) {
            reportFile = File(reportDirectory, "${reportName}_$index.xml")
            index++
        }
        return reportFile
    }

    companion object {
        private const val TEST_REPORTS_FOLDER_NAME = "test-reports"
        private const val DEFAULT_TEST_REPORT_NAME = "rest-cli-test-report"
    }
}
