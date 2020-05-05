package com.ExceptionHandled.GameServer.InternalMessage;


import com.ExceptionHandled.GameMessages.Wrappers.Packet;
import com.ExceptionHandled.GameServer.ClientConnection;

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
