package com.keithlamond.tasktracker.model;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TaskRequest {

    private String title;
    private String description;
    private String priority;
    private Long userId;

    public TaskRequest() {}

    public TaskRequest(String title, String description, String priority, Long userId) {
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.userId = userId;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}
