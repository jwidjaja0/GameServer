package com.ExceptionHandled.GameServer;

import com.ExceptionHandled.GameMessages.Game.MoveMade;
import com.ExceptionHandled.GameMessages.Interfaces.Game;
import com.ExceptionHandled.GameMessages.MainMenu.ActiveGameHeader;
import com.ExceptionHandled.GameMessages.Wrappers.Packet;
import com.ExceptionHandled.GameServer.Game.Game;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class GameRoom implements Runnable {
    private String gameID;
    private String roomPassword;
    private String gameName;

    private String p1;
    private String p2;

    private Game game;

    private BlockingQueue<ServerPacket> serverPacketQ;
    private Thread thread;

    public GameRoom(String gameID, String roomPassword, String gameName, String p1) {
        this.gameID = gameID;
        this.roomPassword = roomPassword;
        this.gameName = gameName;
        this.p1 = p1;

        game = new Game();

        serverPacketQ = new ArrayBlockingQueue<>(20);
        thread = new Thread(this);
        thread.start();
    }

    public String getGameID() {
        return gameID;
    }

    public String getRoomPassword() {
        return roomPassword;
    }

    public void setP2(String p2) {
        this.p2 = p2;
    }

    public void addToMessageQ(ServerPacket sp){
        serverPacketQ.add(sp);
    }

    public ActiveGameHeader getActiveGameHeader(){
        return new ActiveGameHeader(gameID, gameName, p1, p2);
    }

    @Override
    public void run() {
        while(true){
            try {
                ServerPacket serverPacket = serverPacketQ.take();
                Packet packet = serverPacket.getPacket();

                //handle moves
                if(packet.getMessage() instanceof Game){

                    if(packet.getMessage() instanceof MoveMade){

                        MoveMade move = (MoveMade)packet.getMessage();

                        //if invalid
                        if (!game.validMove(move.getxCoord(), move.getyCoord())) {
                            serverPacket.getClientConnection().getObjectOutputStream().writeObject(response);
                        }

                        //actually make the move
                        else {
                            game.setMove(move.getxCoord(), move.getyCoord(), game.getTurnToken());

                            if (game.gameOver()) {
                                char win = game.whoWon();
                                if (win == 'X') {
                                    //TODO: send to server class to notify clients
                                }
                                else if (win == 'O') {
                                    //TODO: send to server class to notify clients
                                }
                                else if (win == 'D') {
                                    //TODO: send to server class to notify clients
                                }
                            }
                            else {
                                game.switchTurn();
                            }
                        }
                    }
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
