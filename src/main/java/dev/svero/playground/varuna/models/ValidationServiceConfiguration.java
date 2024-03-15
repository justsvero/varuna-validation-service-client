package dev.svero.playground.helloworld.models;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ValidationServiceConfiguration {
    private final List<ReportConfiguration> reportConfigurations = new ArrayList<>();
    private final ValidationOptions validationOptions = new ValidationOptions();

    public List<ReportConfiguration> getReportConfigurations() {
        return reportConfigurations;
    }

    public ValidationOptions getValidationOptions() {
        return validationOptions;
    }

    public void addReportConfiguration(ReportConfiguration reportConfiguration) {
        if (reportConfiguration == null) {
            throw new IllegalArgumentException("reportConfiguation may not be null");
        }

        this.reportConfigurations.add(reportConfiguration);
    }

    public void addReportConfiguration(final String reportType) {
        if (StringUtils.isBlank(reportType)) {
            throw new IllegalArgumentException("reportType may not be blank");
        }

        this.reportConfigurations.add(new ReportConfiguration(reportType));
    }

    public void addReportConfiguration(final String reportType, final String reportLanguage) {
        if (StringUtils.isBlank(reportType)) {
            throw new IllegalArgumentException("reportType may not be blank");
        }

        if (StringUtils.isBlank(reportLanguage)) {
            throw new IllegalArgumentException("reportLanguage may not be blank");
        }

        this.reportConfigurations.add(new ReportConfiguration(reportType, reportLanguage));
    }

    public void setProfile(final String profile) {
        if (StringUtils.isBlank(profile)) {
            throw new IllegalArgumentException("profile may not be empty");
        }

        validationOptions.setProfile(profile);
    }

    public void setValidationDateTime(final Date validationDateTime) {
        validationOptions.setValidationDateTime(validationDateTime);
    }

    public void setMaxRecursionDepth(int maxRecursionDepth) {
        if (maxRecursionDepth < 1) {
            throw new IllegalArgumentException("maxRecursionDepth may not be lighter then 1");
        }

        validationOptions.setMaxRecursionDepth(maxRecursionDepth);
    }
}
