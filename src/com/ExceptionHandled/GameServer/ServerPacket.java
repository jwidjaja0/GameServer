package com.ExceptionHandled.GameServer;


import com.ExceptionHandled.GameMessages.Wrappers.Packet;

public class ServerPacket {
    private ClientConnection clientConnection;
    private Packet packet;

    public ServerPacket(ClientConnection clientConnection, Packet packet) {
        this.clientConnection = clientConnection;
        this.packet = packet;
    }

    public ClientConnection getClientConnection() {
        return clientConnection;
    }

    public Packet getPacket() {
        return packet;
    }
}
