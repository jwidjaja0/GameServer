package com.ExceptionHandled.GameServer;

import com.ExceptionHandled.GameMessages.Connection.ConnectionRequest;
import com.ExceptionHandled.GameMessages.Game.MoveMade;
import com.ExceptionHandled.GameMessages.Interfaces.*;
import com.ExceptionHandled.GameMessages.Login.*;
import com.ExceptionHandled.GameMessages.MainMenu.*;
import com.ExceptionHandled.GameMessages.Stats.GameHistoryRequest;
import com.ExceptionHandled.GameMessages.Stats.PlayerStatsInfo;
import com.ExceptionHandled.GameMessages.Stats.PlayerStatsRequest;
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

        gameRoomList.add(new GameRoom("sampleID", "", "sample", "x"));

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
                    handleUserUpdateMessage(serverPacket);
                }
                else if(packet.getMessage() instanceof Stats){
                    handleStatsMessage(serverPacket);
                }

            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleStatsMessage(ServerPacket serverPacket) throws IOException {
        Packet packet = serverPacket.getPacket();
        System.out.println("handleStatsMessage, playerID: " + packet.getPlayerID());
        Packet response = null;

        if(packet.getMessage() instanceof GameHistoryRequest){
            GameHistoryRequest request = (GameHistoryRequest)packet.getMessage();
            response = SQLiteQuery.getInstance().getGameHistoryDetail(packet, request.getGameId());
        }
        else if(packet.getMessage() instanceof PlayerStatsRequest){
            PlayerStatsRequest playerStatsRequest = (PlayerStatsRequest)packet.getMessage();
            response = SQLiteQuery.getInstance().getPlayerStatsInfo(packet);
        }
        serverPacket.getClientConnection().getObjectOutputStream().writeObject(response);
    }

    private void handleUserUpdateMessage(ServerPacket serverPacket) throws IOException {
        Packet packet = serverPacket.getPacket();
        System.out.println("handleUserUpdateMessage, playerID: " + packet.getPlayerID());
        Packet response = null;

        if(packet.getMessage() instanceof UserUpdateRequest){
            response = SQLiteQuery.getInstance().updateUserInfo(packet);
        }
        else if(packet.getMessage() instanceof UserDeleteRequest){
            response = SQLiteQuery.getInstance().userDelete(packet);
        }

        serverPacket.getClientConnection().getObjectOutputStream().writeObject(response);
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

                String playerID = packet.getPlayerID();
                GameRoom gm = new GameRoom(gameID, pw, gameName, playerID, activePlayerMapCC.get(playerID));
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
                    g.setPlayer2(request.getRequestingPlayerId());
                    response = SQLiteQuery.getInstance().joinGame(packet);
                }
            }
        }
        else if(packet.getMessage() instanceof ListActiveGamesRequest){
            List<ActiveGameHeader> gameList = new ArrayList<>();
            for(int i = 0; i < gameRoomList.size(); i++){
                gameList.add(gameRoomList.get(i).getActiveGameHeader());
            }
            System.out.println("Sending list active games, size: " + gameList.size());
            response = new Packet("MainMenu", packet.getPlayerID(), new ListActiveGames(gameList));
        }
        //TODO: write functions for spectator to request joining game
        else if(packet.getMessage() instanceof SpectateRequest){
            SpectateRequest sr = (SpectateRequest)packet.getMessage();
            String gameID = sr.getGameId();

            for(GameRoom gm : gameRoomList){
                if(gm.getGameID().equals(gameID)){
                    gm.addSpectator(packet.getPlayerID());
                    response = SQLiteQuery.getInstance().insertViewerToGame(packet);
                }
            }


        }

        serverPacket.getClientConnection().getObjectOutputStream().writeObject(response);
    }

    private void handleGameMessage(ServerPacket serverPacket) {
        Packet packet = serverPacket.getPacket();
        Packet notice = null;

        Game gameMessage = (Game)packet.getMessage();
        String gameID = gameMessage.getGameID();

            //find the correct gameID
            for(GameRoom gm : gameRoomList) {
                if (gm.getGameID().equals(gameID)) {

                    if (gameMessage instanceof MoveMade) {
                        gm.makeMove((MoveMade) gameMessage);
                    }

                    else if (gameMessage instanceof RematchRequest) {
                        RematchRequest message = (RematchRequest) gameMessage;
                        notice = new Packet("RematchRequest", message.getGameID(), message.getOpponentPlayer());
                        serverPacket.getClientConnection().getObjectOutputStream().writeObject(notice);
                    }

                    else if (gameMessage instanceof RematchRespond) {
                        RematchRespond message = (RematchRespond) gameMessage;
                        notice = new Packet("RematchRespond", message.getGameID(), message.getRequesterPlayerID());
                        serverPacket.getClientConnection().getObjectOutputStream().writeObject(notice);
                    }
                    //TODO: currently can only forfeit on your turn, fix to forfeit whenever
                    else if (gameMessage instanceof ForfeitGame) {
                        gm.gameOver();
                    }
                    else {
                        gm.addToMessageQ(gameMessage);
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
            System.out.println("LoginSuccess response");

            if(response.getMessage() instanceof LoginSuccess){
                LoginSuccess lg = (LoginSuccess) response.getMessage();
                activePlayerMapCC.put(lg.getPlayerID(), serverPacket.getClientConnection());
            }
        }

        else if(packet.getMessage() instanceof LogoutRequest){
            String playerID = packet.getPlayerID();
            if(playerID.equals(null)){
                //TODO: FIX LOGOUTFAIL
                response = new Packet("Login", playerID, new LogoutFail(""));
            }
            else{
                activePlayerMapCC.remove(playerID);
                response = new Packet("Login", playerID, new LogoutSuccess());
            }
        }

        //TODO: delete later, only for debugging
        if(response.getMessage() instanceof LoginSuccess){

            System.out.println(response.getPlayerID());
            System.out.println(response.getPlayerID());
        }
        serverPacket.getClientConnection().getObjectOutputStream().writeObject(response);
    }
}


