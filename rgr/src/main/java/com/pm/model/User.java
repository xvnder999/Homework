package com.pm.model;

public class User {

    private int id;
    private String username;
    private String email;
    private String passwordHash;
    private String role;
    private boolean confirmed;

    public User() {}

    public User(int id, String username, String email, String role, boolean confirmed) {
        this.id        = id;
        this.username  = username;
        this.email     = email;
        this.role      = role;
        this.confirmed = confirmed;
    }

    public int getId()                          { return id; }
    public void setId(int id)                   { this.id = id; }

    public String getUsername()                 { return username; }
    public void setUsername(String v)           { this.username = v; }

    public String getEmail()                    { return email; }
    public void setEmail(String v)              { this.email = v; }

    public String getPasswordHash()             { return passwordHash; }
    public void setPasswordHash(String v)       { this.passwordHash = v; }

    public String getRole()                     { return role; }
    public void setRole(String v)               { this.role = v; }

    public boolean isConfirmed()                { return confirmed; }
    public void setConfirmed(boolean v)         { this.confirmed = v; }

    public boolean isAdmin()                    { return "admin".equals(role); }
}
