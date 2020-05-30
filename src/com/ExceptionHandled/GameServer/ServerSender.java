package com.ExceptionHandled.GameServer;

import com.ExceptionHandled.GameMessages.Wrappers.Packet;
import com.ExceptionHandled.GameServer.InternalMessage.ServerPacket;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;

public class ServerSender implements Runnable {
    private BlockingQueue<ServerPacket> outgoingQueue;

    public ServerSender(BlockingQueue<ServerPacket> outgoingQueue) {
        this.outgoingQueue = outgoingQueue;
        System.out.println("Server sender started");
        Thread thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        while(true){
            try {
                ServerPacket serverPacket = outgoingQueue.take();
                Packet packet = serverPacket.getPacket();
                ClientConnection cc = serverPacket.getClientConnection();

                cc.getObjectOutputStream().writeObject(packet);
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }

        }

    }
}