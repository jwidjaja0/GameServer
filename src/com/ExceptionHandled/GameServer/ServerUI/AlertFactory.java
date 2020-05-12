package com.ExceptionHandled.GameServer.ServerUI;


import javax.swing.*;

public class AlertFactory {
    private static AlertFactory instance = new AlertFactory();

    private AlertFactory(){
    }

    public static AlertFactory getInstance(){
        return instance;
    }

    public void displayAlert(String reason){
        JOptionPane.showMessageDialog(null, reason);
    }
}
