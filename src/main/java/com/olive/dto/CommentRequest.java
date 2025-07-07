package com.olive.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CommentRequest {
    @NotBlank(message = "Comment content cannot be empty")
    @Size(max = 2000, message = "Comment cannot exceed 2000 characters")
    private String content;

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
