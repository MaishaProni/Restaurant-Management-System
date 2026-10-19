package com.restaurant.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDto {
    private String fullName;
    private String username;

    public String getFullName() { return fullName; }
    public String getUsername() { return username; }

    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setUsername(String username)  { this.username = username; }
}
