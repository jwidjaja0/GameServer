package com.ExceptionHandled.GameServer;

import com.ExceptionHandled.GameMessages.Packet;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;

public class ClientConnection implements Runnable {

    private Socket socket;
    private ObjectOutputStream objectOutputStream;
    private int clientNo;
    private UUID id;

    private BlockingQueue<ServerPacket> messageQueue;
    private List<ClientConnection> clientConnectionList;
    private Thread thread;

    public ClientConnection(Socket socket, int clientNo, BlockingQueue<ServerPacket> messageQueue, List<ClientConnection> clientConnectionList) {
        this.socket = socket;
        this.clientNo = clientNo;
        this.messageQueue = messageQueue;
        this.clientConnectionList = clientConnectionList;

        id = UUID.randomUUID();
        clientConnectionList.add(this);

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
                ServerPacket serverPacket =new ServerPacket(this, packet);

                System.out.println(serverPacket);
                messageQueue.put(serverPacket);
            }
        } catch (IOException | ClassNotFoundException | InterruptedException e) {
            e.printStackTrace();
        }
        finally{
            clientConnectionList.remove(this);
        }

    }
}
