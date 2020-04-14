package com.ExceptionHandled.GameServer;

import com.ExceptionHandled.GameMessages.Connection.ConnectionRequest;
import com.ExceptionHandled.GameMessages.Game.MoveMade;
import com.ExceptionHandled.GameMessages.Interfaces.Game;
import com.ExceptionHandled.GameMessages.Interfaces.Login;
import com.ExceptionHandled.GameMessages.Interfaces.MainMenu;
import com.ExceptionHandled.GameMessages.Interfaces.UserUpdate;
import com.ExceptionHandled.GameMessages.Login.*;
import com.ExceptionHandled.GameMessages.MainMenu.*;
import com.ExceptionHandled.GameMessages.UserUpdate.UserDeleteRequest;
import com.ExceptionHandled.GameMessages.UserUpdate.UserUpdateRequest;
import com.ExceptionHandled.GameMessages.Wrappers.Packet;
import com.ExceptionHandled.GameServer.Database.SQLiteQuery;


import java.io.IOException;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Server implements Runnable {
    private BlockingQueue<ServerPacket> messageQueue;

    private List<ClientConnection> clientConnectionList;
    private List<GameRoom> gameRoomList;

    private Map<String, ClientConnection> activePlayerMapCC;

    private ListenNewClient listenNewClient;

    private Thread thread;

    public Server() {
        messageQueue = new ArrayBlockingQueue<>(500);
        clientConnectionList = new ArrayList<>(100);
        gameRoomList = new ArrayList<>(100);
        listenNewClient = new ListenNewClient(clientConnectionList, messageQueue);
        activePlayerMapCC = new HashMap<>();

        thread = new Thread(this);
        thread.start();
        System.out.println("server instantiated");
    }

    @Override
    public void run() {
        SQLiteQuery.getInstance().setConnection();
        System.out.println("Server thread started");

        while (true){
            try {
                ServerPacket serverPacket = messageQueue.take();
                Packet packet = serverPacket.getPacket();

                if(packet.getMessage() instanceof ConnectionRequest){
                    handleConnectionRequest(serverPacket);
                }
                else if(packet.getMessage() instanceof Login){
                    handleLoginMessages(serverPacket);
                }
                else if(packet.getMessage() instanceof MainMenu){
                    handleMainMenuMessage(serverPacket);
                }
                else if(packet.getMessage() instanceof Game){
                    handleGameMessage(serverPacket);
                }
                else if(packet.getMessage() instanceof UserUpdate){
                    handeUserUpdateMessage(serverPacket);
                }

            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }

    }

    private void handeUserUpdateMessage(ServerPacket serverPacket) {
        Packet packet = serverPacket.getPacket();
        Packet response = null;

        if(packet.getMessage() instanceof UserUpdateRequest){
            response = SQLiteQuery.getInstance().updateUserInfo(packet);
        }
        else if(packet.getMessage() instanceof UserDeleteRequest){
            response = SQLiteQuery.getInstance().userDelete(packet);
        }
    }

    private void handleMainMenuMessage(ServerPacket serverPacket) throws IOException {
        Packet packet = serverPacket.getPacket();
        Packet response = null;

        if(packet.getMessage() instanceof NewGameRequest){
            NewGameRequest newGameRequest = (NewGameRequest)packet.getMessage();
            response = SQLiteQuery.getInstance().insertNewGame(packet);

            if(response.getMessage() instanceof NewGameSuccess){
                NewGameSuccess ngs = (NewGameSuccess)response.getMessage();
                String gameID = ngs.getGameId();
                String pw = newGameRequest.getGamePassword();
                String gameName = newGameRequest.getGameName();

                GameRoom gm = new GameRoom(gameID, pw, gameName, packet.getPlayerID());
                System.out.println("New Game added");
                gameRoomList.add(gm);
            }
        }
        else if(packet.getMessage() instanceof JoinGameRequest){
            JoinGameRequest request = (JoinGameRequest)packet.getMessage();
            String idRequest = request.getGameId();
            String pw = request.getGamePassword();

            for(GameRoom g : gameRoomList){

                //TODO: Add check two players already set, can't set p2 if another request comes.
                if(g.getGameID().equals(idRequest) && g.getRoomPassword().equals(pw)){
                    g.setP2(request.getRequestingPlayerId());
                    response = SQLiteQuery.getInstance().joinGame(packet);
                }
            }
        }

        else if(packet.getMessage() instanceof ListActiveGamesRequest){
            List<ActiveGameHeader> gameList = new ArrayList<>(gameRoomList.size());
            for(int i = 0; i < gameList.size(); i++){
                gameList.set(i, gameRoomList.get(i).getActiveGameHeader());
            }
            response = new Packet("MainMenu", packet.getPlayerID(), new ListActiveGames(gameList));
        }

        serverPacket.getClientConnection().getObjectOutputStream().writeObject(response);
    }

    private void handleGameMessage(ServerPacket serverPacket) {
        Packet packet = serverPacket.getPacket();

        Game gameMessage = (Game)packet.getMessage();
        String gameID = gameMessage.getGameID();

        if(gameMessage instanceof MoveMade){
            //find the correct gameID
            for(GameRoom gm : gameRoomList){
                if(gm.getGameID().equals(gameID)){
                    gm.addToMessageQ(serverPacket);
                }
            }
        }
    }

    public void handleConnectionRequest(ServerPacket serverPacket){
        System.out.println("Connection request from client");
    }

    public void handleLoginMessages(ServerPacket serverPacket) throws IOException {
        Packet packet = serverPacket.getPacket();
        Packet response = null;

        if(packet.getMessage() instanceof SignUpRequest){
            response = SQLiteQuery.getInstance().insertNewUser(packet);
        }
        else if(packet.getMessage() instanceof LoginRequest){
            response = SQLiteQuery.getInstance().userLoggingIn(packet);

            if(response.getMessage() instanceof LoginSuccess){
                LoginSuccess lg = (LoginSuccess)response.getMessage();
                activePlayerMapCC.put(lg.getPlayerID(), serverPacket.getClientConnection());
            }
        }

        else if(packet.getMessage() instanceof SignOutRequest){
            String playerID = packet.getPlayerID();
            activePlayerMapCC.remove(playerID);

            response = new Packet("Login", playerID, new SignOutSuccess());
        }

        serverPacket.getClientConnection().getObjectOutputStream().writeObject(response);
    }
}
