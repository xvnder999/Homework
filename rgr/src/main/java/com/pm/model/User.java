package com.pm.model;

public class User {
    private int id;
    private String username;
    private String email;
    private String role;
    private boolean confirmed;

    public User() {}
    public int getId()               { return id; }
    public void setId(int id)        { this.id = id; }
    public String getUsername()      { return username; }
    public void setUsername(String v){ this.username = v; }
    public String getEmail()         { return email; }
    public void setEmail(String v)   { this.email = v; }
    public String getRole()          { return role; }
    public void setRole(String v)    { this.role = v; }
    public boolean isConfirmed()     { return confirmed; }
    public void setConfirmed(boolean v) { this.confirmed = v; }
    public boolean isAdmin()         { return "admin".equals(role); }
}
