package com.ExceptionHandled.GameServer;

import com.ExceptionHandled.GameMessages.Game.MoveMade;

import java.util.List;
import java.util.concurrent.BlockingQueue;

public class ListenNewClient {
    private List<ClientConnection> clientConnectionList;
    private BlockingQueue<MoveMade> moveQ;
}
