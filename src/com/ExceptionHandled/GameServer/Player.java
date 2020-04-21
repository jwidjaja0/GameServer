package com.ExceptionHandled.GameServer;

import java.util.UUID;

public class Player {

    private UUID id;
    private String name;

    public Player(UUID id, String name) {
        this.id = id;
        this.name = name;
    }

    public Player() {
        id = UUID.randomUUID();
        name = "BOT";
    }

    public UUID getId() {
        return id;
    }

    public Player(String name) {
        this.name = name;
    }
}
