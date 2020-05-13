package com.ExceptionHandled.GameServer;

import com.ExceptionHandled.GameMessages.Wrappers.Packet;
import com.ExceptionHandled.GameServer.InternalMessage.ServerPacket;

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
    private UUID connectionID;

    private BlockingQueue<ServerPacket> messageQueue;
    private List<ClientConnection> clientConnectionList;
    private Thread thread;

    public ClientConnection(Socket socket, int clientNo, BlockingQueue<ServerPacket> messageQueue, List<ClientConnection> clientConnectionList) {
        this.socket = socket;
        this.clientNo = clientNo;
        this.messageQueue = messageQueue;
        this.clientConnectionList = clientConnectionList;

        connectionID = UUID.randomUUID();
        clientConnectionList.add(this);

        thread = new Thread(this);
        thread.start();
    }

    public String getConnectionID() {
        return connectionID.toString();
    }

    public synchronized ObjectOutputStream getObjectOutputStream() {
        return objectOutputStream;
    }

    @Override
    public void run() {
        try {
            System.out.println("client connection thread started with ID: " + connectionID);
            ObjectInputStream inputFromClient = new ObjectInputStream(socket.getInputStream());
            this.objectOutputStream = new ObjectOutputStream(socket.getOutputStream());

            while(true){
                Packet packet = (Packet) inputFromClient.readObject();
                ServerPacket serverPacket = new ServerPacket(this, packet);

                System.out.println(serverPacket);
                messageQueue.put(serverPacket);
            }
        } catch (IOException e) {
            System.out.println("Connection from client interrupted");
        } catch( ClassNotFoundException e){
            System.out.println("ClassNotFound");
        } catch( InterruptedException e){
            System.out.println("InterruptedException");
        }
        finally{
            clientConnectionList.remove(this);
        }

    }
}
