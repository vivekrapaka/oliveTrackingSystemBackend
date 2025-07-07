package com.olive.model.enums;

public enum TaskType {
    BRD("Business Requirement Document"),
    EPIC("Epic"),
    STORY("Story"),
    TASK("Task"),
    BUG("Bug"),
    SUB_TASK("Sub-Task");

    private final String displayName;

    TaskType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
