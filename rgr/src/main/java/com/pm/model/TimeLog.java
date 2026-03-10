package com.pm.model;

import java.sql.Timestamp;

public class TimeLog {

    private int id;
    private int taskId;
    private int userId;
    private String username;
    private int minutes;
    private String note;
    private Timestamp loggedAt;

    public TimeLog() {}

    public int getId()                      { return id; }
    public void setId(int v)               { this.id = v; }

    public int getTaskId()                  { return taskId; }
    public void setTaskId(int v)           { this.taskId = v; }

    public int getUserId()                  { return userId; }
    public void setUserId(int v)           { this.userId = v; }

    public String getUsername()             { return username; }
    public void setUsername(String v)      { this.username = v; }

    public int getMinutes()                 { return minutes; }
    public void setMinutes(int v)          { this.minutes = v; }

    public String getNote()                 { return note; }
    public void setNote(String v)          { this.note = v; }

    public Timestamp getLoggedAt()          { return loggedAt; }
    public void setLoggedAt(Timestamp v)   { this.loggedAt = v; }
}
