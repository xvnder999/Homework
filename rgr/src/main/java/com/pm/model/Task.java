package com.pm.model;

import java.sql.Timestamp;

public class Task {
    private int id;
    private int projectId;
    private String title;
    private String description;
    private String status;
    private String priority;
    private Timestamp createdAt;

    public Task() {}
    public int getId()              { return id; }
    public void setId(int v)        { this.id = v; }
    public int getProjectId()       { return projectId; }
    public void setProjectId(int v) { this.projectId = v; }
    public String getTitle()        { return title; }
    public void setTitle(String v)  { this.title = v; }
    public String getDescription()  { return description; }
    public void setDescription(String v) { this.description = v; }
    public String getStatus()       { return status; }
    public void setStatus(String v) { this.status = v; }
    public String getPriority()     { return priority; }
    public void setPriority(String v){ this.priority = v; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp v) { this.createdAt = v; }
}
