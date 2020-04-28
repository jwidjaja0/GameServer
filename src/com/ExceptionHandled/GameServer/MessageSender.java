package com.ExceptionHandled.GameServer;

public class MessageSender {

    private static MessageSender instance = new MessageSender();
    private MessageSender(){ }


    public static MessageSender getInstance() {
        return instance;
    }



}
