package com.pm.model;

import java.sql.Date;
import java.sql.Timestamp;

public class Task {

    private int id;
    private int projectId;
    private String projectName;
    private Integer assigneeId;
    private String assigneeName;
    private String title;
    private String description;
    private String status;
    private String priority;
    private Date dueDate;
    private Timestamp createdAt;
    private int totalMinutes;

    public Task() {}

    public int getId()                          { return id; }
    public void setId(int v)                    { this.id = v; }

    public int getProjectId()                   { return projectId; }
    public void setProjectId(int v)             { this.projectId = v; }

    public String getProjectName()              { return projectName; }
    public void setProjectName(String v)        { this.projectName = v; }

    public Integer getAssigneeId()              { return assigneeId; }
    public void setAssigneeId(Integer v)        { this.assigneeId = v; }

    public String getAssigneeName()             { return assigneeName; }
    public void setAssigneeName(String v)       { this.assigneeName = v; }

    public String getTitle()                    { return title; }
    public void setTitle(String v)              { this.title = v; }

    public String getDescription()              { return description; }
    public void setDescription(String v)        { this.description = v; }

    public String getStatus()                   { return status; }
    public void setStatus(String v)             { this.status = v; }

    public String getPriority()                 { return priority; }
    public void setPriority(String v)           { this.priority = v; }

    public Date getDueDate()                    { return dueDate; }
    public void setDueDate(Date v)              { this.dueDate = v; }

    public Timestamp getCreatedAt()             { return createdAt; }
    public void setCreatedAt(Timestamp v)       { this.createdAt = v; }

    public int getTotalMinutes()                { return totalMinutes; }
    public void setTotalMinutes(int v)          { this.totalMinutes = v; }
}
