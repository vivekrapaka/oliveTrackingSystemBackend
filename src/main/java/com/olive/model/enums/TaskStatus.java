package com.olive.model.enums;

public enum TaskStatus {
    BACKLOG("Backlog"),
    ANALYSIS("Analysis"),
    DEVELOPMENT("Development"),
    CODE_REVIEW("Code Review"), // NEW: Added Code Review stage
    SIT_TESTING("SIT Testing"),
    SIT_FAILED("SIT Failed"),
    UAT_TESTING("UAT Testing"),
    UAT_FAILED("UAT Failed"),
    PREPROD("Pre-Production"),
    PROD("Production"),
    COMPLETED("Completed"),
    CLOSED("Closed"),
    REOPENED("Reopened"),
    BLOCKED("Blocked");

    private final String displayName;

    TaskStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}