package com.ExceptionHandled.GameServer.Observer;

public interface GameLogicSubject {
    public void addGameLogicObserver(GameLogicObserver obs);
    public void removeGameLogicObserver(GameLogicObserver obs);
    public void notifyGameLogicObserver();
}
