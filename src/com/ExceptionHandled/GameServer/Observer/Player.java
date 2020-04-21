package com.ExceptionHandled.GameServer.Observer;

import java.util.UUID;

public class Player {
    UUID id;
    private String name;

    public Player(UUID id, String name) {
        this.id = id;
        this.name = name;
    }

    public UUID getId() {
        return id;
    }

    public Player(String name) {
        this.name = name;
    }
}
