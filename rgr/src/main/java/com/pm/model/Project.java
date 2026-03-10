package com.pm.model;

import java.sql.Timestamp;

public class Project {
    private int id;
    private int ownerId;
    private String name;
    private String description;
    private String status;
    private Timestamp createdAt;

    public Project() {}
    public int getId()              { return id; }
    public void setId(int v)        { this.id = v; }
    public int getOwnerId()         { return ownerId; }
    public void setOwnerId(int v)   { this.ownerId = v; }
    public String getName()         { return name; }
    public void setName(String v)   { this.name = v; }
    public String getDescription()  { return description; }
    public void setDescription(String v) { this.description = v; }
    public String getStatus()       { return status; }
    public void setStatus(String v) { this.status = v; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp v) { this.createdAt = v; }
}
