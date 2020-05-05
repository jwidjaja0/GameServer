package com.ExceptionHandled.GameServer;

import com.ExceptionHandled.GameMessages.Connection.*;
import com.ExceptionHandled.GameMessages.Game.*;
import com.ExceptionHandled.GameMessages.Interfaces.*;
import com.ExceptionHandled.GameMessages.Login.*;
import com.ExceptionHandled.GameMessages.MainMenu.*;
import com.ExceptionHandled.GameMessages.Stats.*;
import com.ExceptionHandled.GameMessages.UserUpdate.*;
import com.ExceptionHandled.GameMessages.Wrappers.Packet;
import com.ExceptionHandled.GameServer.Database.SQLiteQuery;
import com.ExceptionHandled.GameServer.InternalMessage.ActivePlayerList;
import com.ExceptionHandled.GameServer.InternalMessage.ServerPacket;
import com.ExceptionHandled.GameServer.Observer.GameLogicObserver;
import com.ExceptionHandled.GameServer.Observer.GameLogicSubject;


import java.io.IOException;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Server implements Runnable, GameLogicSubject {
    private BlockingQueue<ServerPacket> messageQueue;

    private List<ClientConnection> clientConnectionList;
    private List<GameRoom> gameRoomList;
    private Map<String, ClientConnection> activePlayerMapCC;
    private ListenNewClient listenNewClient;

    private Thread thread;

    private List<GameLogicObserver> observerList;

    public Server() {
        messageQueue = new ArrayBlockingQueue<>(500);
        clientConnectionList = new ArrayList<>(100);
        gameRoomList = new ArrayList<>(100);
        listenNewClient = new ListenNewClient(clientConnectionList, messageQueue);
        activePlayerMapCC = new HashMap<>();

        //gameRoomList.add(new GameRoom("sampleID", "", "sample", "x"));
        observerList = new ArrayList<>();

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

    private void handleMainMenuMessage(ServerPacket serverPacket) throws IOException, InterruptedException {
        Packet packet = serverPacket.getPacket();
        Packet response = null;
        String playerID = packet.getPlayerID();

        if(packet.getMessage() instanceof NewGameRequest){
            NewGameRequest newGameRequest = (NewGameRequest)packet.getMessage();
            response = SQLiteQuery.getInstance().insertNewGame(packet);

            if(response.getMessage() instanceof NewGameSuccess){
                NewGameSuccess ngs = (NewGameSuccess)response.getMessage();
                String gameID = ngs.getGameId();
                String pw = newGameRequest.getGamePassword();
                String gameName = newGameRequest.getGameName();

                GameRoom gm = new GameRoom(gameID, pw, gameName, playerID);
                System.out.println("New Game added");
                gameRoomList.add(gm);

                notifyGameLogicObserver(getListActiveGames());

                if (newGameRequest.getOpponent().equalsIgnoreCase("AI")) {
                    String aiID = "a1234bcd";
                    JoinGameRequest aiJoin = new JoinGameRequest(gameID, pw);
                    Packet sPacket = new Packet("MainMenu", aiID, aiJoin);
                    messageQueue.put(new ServerPacket(serverPacket.getClientConnection(), sPacket));
                }
            }
        }

        else if(packet.getMessage() instanceof JoinGameRequest){
            JoinGameRequest request = (JoinGameRequest)packet.getMessage();
            String idRequest = request.getGameId();
            String pw = request.getGamePassword();
            System.out.println("received joinGameRequest");

            for(GameRoom g : gameRoomList){
                //TODO: removed password check, reimplement to next line when fixed
                if(g.getGameID().equals(idRequest)){
                    if (g.getPlayer2() == null) {
                        ArrayList<Packet> packets = g.setPlayer2(playerID);
                        for (Packet notice : packets) {
                            System.out.println("sending to player");
                            if (!notice.getPlayerID().equals("a1234bcd")) //if not to Ai
                                activePlayerMapCC.get(notice.getPlayerID()).getObjectOutputStream().writeObject(notice);
                        }
                        response = SQLiteQuery.getInstance().joinGame(packet);

                    }
                    else {
                        Packet notice = new Packet ("MainMenu", playerID, new JoinGameFail(idRequest));
                        serverPacket.getClientConnection().getObjectOutputStream().writeObject(notice);
                    }
                }
            }
            return;
        }

        else if(packet.getMessage() instanceof ListActiveGamesRequest){

            ListActiveGames listAG = getListActiveGames();
            response = new Packet("MainMenu", packet.getPlayerID(), listAG);

        }

        else if(packet.getMessage() instanceof SpectateRequest){
            SpectateRequest sr = (SpectateRequest)packet.getMessage();
            String gameID = sr.getGameId();

            for(GameRoom gm : gameRoomList){
                if(gm.getGameID().equals(gameID)){
                    Packet notice = gm.addViewer(packet.getPlayerID());
                    if (!notice.getPlayerID().equals("a1234bcd"))
                        activePlayerMapCC.get(notice.getPlayerID()).getObjectOutputStream().writeObject(notice);
                    response = SQLiteQuery.getInstance().insertViewerToGame(packet);
                }
            }
        }

        else if(packet.getMessage() instanceof SpectatorLeave){
            SpectatorLeave sl = (SpectatorLeave)packet.getMessage();
            String gameID = sl.getGameID();

            for(GameRoom gm : gameRoomList){
                if(gm.getGameID().equals(gameID)){
                    gm.removeViewer(packet.getPlayerID());
                }
            }
        }

        serverPacket.getClientConnection().getObjectOutputStream().writeObject(response);
    }

    private void handleGameMessage(ServerPacket serverPacket) throws IOException {
        Packet packet = serverPacket.getPacket();
        Game gameMessage = (Game)packet.getMessage();
        String gameID = gameMessage.getGameID();
        GameRoom gr = null;
        boolean removeGame = false;

            //find the correct gameID
            for (GameRoom gm : gameRoomList) {

                if (gm.getGameID().equals(gameID)) {
                    ArrayList<Packet> packets = new ArrayList<Packet>(); //to send back

                    if (gameMessage instanceof MoveMade) {
                        System.out.println("MoveMade, setting packet back");
                        packets.addAll(gm.makeMove((MoveMade) gameMessage));

                        //to remove game later from gameList if game over
                        for(Packet p: packets){
                            if(p.getMessage() instanceof GameOverOutcome){
                                removeGame = true;
                                gr = gm;
                            }
                        }


                    }

                    else if (gameMessage instanceof RematchRequest) {
                        RematchRequest message = (RematchRequest) gameMessage;
                        String pID = packet.getPlayerID();
                        if (pID.equals(gm.getPlayer1())) {
                            pID = gm.getPlayer2();
                        }
                        packets.add(new Packet("Game", message.getGameID(), pID));
                    }

                    else if (gameMessage instanceof RematchRespond) {
                        RematchRespond message = (RematchRespond) gameMessage;
                        String pID = packet.getPlayerID();
                        if (pID.equals(gm.getPlayer1())) {
                            pID = gm.getPlayer2();
                        }
                        packets.add(new Packet("Game", message.getGameID(), pID));
                    }
                    //TODO: currently can only forfeit on your turn, fix to forfeit whenever
                    else if (gameMessage instanceof ForfeitGame) {
                        packets.addAll(gm.gameForfeit());
                        //to remove game later from gameList if game over
                        removeGame = true;
                        gr = gm;
                    }

                    //send all packets
                    for (Packet notice : packets) {
                        if (!notice.getPlayerID().equals("a1234bcd"))
                            activePlayerMapCC.get(notice.getPlayerID()).getObjectOutputStream().writeObject(notice);
                    }
                }
            }

        //removes game later from gameList if game over
        if (removeGame) {
            gameRoomList.remove(gr);
            notifyGameLogicObserver(getListActiveGames());
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

                notifyGameLogicObserver(getActivePlayers());
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
                notifyGameLogicObserver(getActivePlayers());
            }
        }

        //TODO: delete later, only for debugging
        if(response.getMessage() instanceof LoginSuccess){

            System.out.println(response.getPlayerID());
            System.out.println(response.getPlayerID());
        }
        serverPacket.getClientConnection().getObjectOutputStream().writeObject(response);
    }

    private ListActiveGames getListActiveGames(){
        List<ActiveGameHeader> gameList = new ArrayList<>();
        for(int i = 0; i < gameRoomList.size(); i++){
            gameList.add(gameRoomList.get(i).getActiveGameHeader());
        }
        System.out.println("Sending list active games, size: " + gameList.size());
        ListActiveGames listAG = new ListActiveGames(gameList);
        return listAG;
    }

    private ActivePlayerList getActivePlayers(){
        List<String> idList = new ArrayList<>();
        for(String key : activePlayerMapCC.keySet()){
            idList.add(key);
        }
        return new ActivePlayerList(idList);
    }

    @Override
    public void addGameLogicObserver(GameLogicObserver obs) {
        observerList.add(obs);
    }

    @Override
    public void removeGameLogicObserver(GameLogicObserver obs) {
        observerList.remove(obs);
    }

    @Override
    public void notifyGameLogicObserver(Object arg) {
        for(GameLogicObserver g: observerList){
            g.update(this, arg);
        }
    }
}


