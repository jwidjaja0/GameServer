package com.ExceptionHandled.GameServer;

import java.util.UUID;

public class Player {
    
    private String id;
    private String username;
    private String firstName;
    private String lastName;

    public Player(String id, String username, String firstName, String lastName) {
        this.id = id;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }
}
