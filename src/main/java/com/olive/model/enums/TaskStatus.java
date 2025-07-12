package com.olive.model.enums;

public enum TaskStatus {
    BACKLOG("Backlog"),
    ANALYSIS("Analysis"),
    DEVELOPMENT("Development"),
    CODE_REVIEW("Code Review"),
    UAT_TESTING("UAT Testing"),
    UAT_FAILED("UAT Failed"),
    READY_FOR_PREPROD("Ready for Pre-Prod"),
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
