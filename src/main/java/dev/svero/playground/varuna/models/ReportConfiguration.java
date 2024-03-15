package dev.svero.playground.helloworld.models;

public class ReportConfiguration {
    private String reportLanguage;
    private String reportType;

    public ReportConfiguration() {}

    public ReportConfiguration(String reportType) {
        this.reportType = reportType;
    }

    public ReportConfiguration(String reportType, String reportLanguage) {
        this.reportLanguage = reportLanguage;
        this.reportType = reportType;
    }

    public String getReportLanguage() {
        return reportLanguage;
    }

    public void setReportLanguage(String reportLanguage) {
        this.reportLanguage = reportLanguage;
    }

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }
}
