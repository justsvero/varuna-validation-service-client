package dev.svero.playground.varuna.models;

import java.util.Date;

public class ValidationOptions {
    private String profile = "AUTOMATIC";
    private Date validationDateTime = new Date();
    private int maxRecursionDepth = 1;
    private String etsiSignatureValidationPolicy;
    private String govCustomSignatureValidationPolicy;

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public Date getValidationDateTime() {
        return validationDateTime;
    }

    public void setValidationDateTime(Date validationDateTime) {
        this.validationDateTime = validationDateTime;
    }

    public int getMaxRecursionDepth() {
        return maxRecursionDepth;
    }

    public void setMaxRecursionDepth(int maxRecursionDepth) {
        this.maxRecursionDepth = maxRecursionDepth;
    }

    public String getEtsiSignatureValidationPolicy() {
        return etsiSignatureValidationPolicy;
    }

    public void setEtsiSignatureValidationPolicy(String etsiSignatureValidationPolicy) {
        this.etsiSignatureValidationPolicy = etsiSignatureValidationPolicy;
    }

    public String getGovCustomSignatureValidationPolicy() {
        return govCustomSignatureValidationPolicy;
    }

    public void setGovCustomSignatureValidationPolicy(String govCustomSignatureValidationPolicy) {
        this.govCustomSignatureValidationPolicy = govCustomSignatureValidationPolicy;
    }
}
