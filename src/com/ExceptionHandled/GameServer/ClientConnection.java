package com.ExceptionHandled.GameServer;

import com.ExceptionHandled.GameMessages.Game.MoveMade;
import com.ExceptionHandled.GameMessages.Packet;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;

public class ClientConnection implements Runnable {

    private Socket socket;
    private ObjectOutputStream objectOutputStream;
    private int clientNo;
    private UUID id;

    private BlockingQueue<MoveMade> moveQ;
    private Thread thread;

    public ClientConnection(Socket socket, int clientNo, BlockingQueue<MoveMade> moveQ) {
        this.socket = socket;
        this.clientNo = clientNo;
        this.moveQ = moveQ;

        id = UUID.randomUUID();

        thread = new Thread(this);
        thread.start();

    }

    public synchronized ObjectOutputStream getObjectOutputStream() {
        return objectOutputStream;
    }

    @Override
    public void run() {
        try {
            ObjectInputStream inputFromClient = new ObjectInputStream(socket.getInputStream());
            this.objectOutputStream = new ObjectOutputStream(socket.getOutputStream());

            while(true){
                Packet packet = (Packet)inputFromClient.readObject();
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

    }
}
