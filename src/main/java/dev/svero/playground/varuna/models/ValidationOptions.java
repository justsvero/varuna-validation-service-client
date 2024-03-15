package dev.svero.playground.helloworld.models;

import java.util.Date;

public class ValidationOptions {
    private String profile;
    private Date validationDateTime = new Date();
    private int maxRecursionDepth = 1;

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
}
