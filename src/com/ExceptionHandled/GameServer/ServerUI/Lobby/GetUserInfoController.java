package com.ExceptionHandled.GameServer.ServerUI.Lobby;


import com.ExceptionHandled.GameMessages.Login.*;
import com.ExceptionHandled.GameMessages.MainMenu.ListActiveGamesRequest;
import com.ExceptionHandled.GameMessages.Stats.PlayerStatsRequest;
import com.ExceptionHandled.GameMessages.UserUpdate.*;

import com.ExceptionHandled.GameMessages.Wrappers.Packet;
import com.ExceptionHandled.GameServer.Database.SQLiteQuery;
import com.ExceptionHandled.GameServer.ServerUI.AlertFactory;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.Serializable;
import java.util.Observable;

public class GetUserInfoController extends Observable {
    @FXML
    Button action;
    @FXML
    TextField username;
    @FXML
    PasswordField password;
    @FXML
    TextField firstName;
    @FXML
    TextField lastName;
    @FXML
    Label firstNameLabel;

    @FXML
    Label lastNameLabel;
    @FXML
    AnchorPane getInfoAnchor;

    boolean isLogin;
    boolean isChange;

    private String userID;


    public void initialize(){
        action.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                String newUsername = username.getText();
                String newPassword = password.getText();
                String fName = firstName.getText();
                String lName = lastName.getText();

                UserUpdateRequest request = new UserUpdateRequest(newUsername, newPassword, fName, lName);
                updateUser(request);
            }
        });
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    private void updateUser(UserUpdateRequest request){
        Packet response = SQLiteQuery.getInstance().updateUserInfo(new Packet("stats", userID, request));
        if(response.getMessage() instanceof UserUpdateSuccess){
            alertResponse("Update success");
        }
        else{
            alertResponse("Update failed");
        }
    }

    private void alertResponse(String message){
        AlertFactory.getInstance().displayAlert(message);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                close();
            }
        });
    }

    public void messageProcessor(Serializable message){

        if (message instanceof UserUpdateSuccess){
            updateUserSuccess((UserUpdateSuccess) message);
        }
        else if(message instanceof UserUpdateFail){

        }

    }

    private void close(){
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                ((Stage) action.getScene().getWindow()).close();
            }
        });
    }


    public void setType(String type){
        if (type.equals("Register"))
            setToRegister();
        else if (type.equals("Login"))
            setToLogin();
        else {
            setToChange();
        }
    }

    private void setToRegister(){
        action.setText("Register");
        isLogin = false;
        isChange = false;
    }

    private void setToLogin(){
        action.setText("Login");
        firstNameLabel.setVisible(false);
        lastNameLabel.setVisible(false);
        firstName.setVisible(false);
        lastName.setVisible(false);
        isLogin = true;
        isChange = false;
    }

    private void setToChange(){
        action.setText("Change Profile Information");
        firstNameLabel.setVisible(false);
        lastNameLabel.setVisible(false);
        firstName.setVisible(false);
        lastName.setVisible(false);
        isLogin = false;
        isChange = true;
    }

    private void updateUserSuccess(UserUpdateSuccess success){
        AlertFactory.getInstance().displayAlert(success.toString());
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                close();
            }
        });
    }



}
