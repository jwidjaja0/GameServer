package com.ExceptionHandled.GameServer.Factory;

public class MessageFactory {

    private static MessageFactory instance = new MessageFactory();

    private MessageFactory(){

    }

    public static MessageFactory getInstance(){
        return instance;
    }


}
