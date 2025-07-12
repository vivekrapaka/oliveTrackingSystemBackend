package com.olive.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public class TaskActivityResponse {
    private Long id;
    private String eventType;
    private String content;
    private String fieldName;
    private String oldValue;
    private String newValue;
    private String userName;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    public TaskActivityResponse(Long id, String eventType, String content, String fieldName, String oldValue, String newValue, String userName, LocalDateTime timestamp) {
        this.id = id;
        this.eventType = eventType;
        this.content = content;
        this.fieldName = fieldName;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.userName = userName;
        this.timestamp = timestamp;
    }
    // Getters...

    public Long getId() {
        return id;
    }

    public String getEventType() {
        return eventType;
    }

    public String getContent() {
        return content;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getOldValue() {
        return oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public String getUserName() {
        return userName;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}