package com.ExceptionHandled.GameServer.ServerUI.Lobby;

import com.ExceptionHandled.GameMessages.UserInfo;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.ArrayList;
import java.util.List;

public class AllPlayersController {
    @FXML private TableView<UserInfo> userTableView;
    @FXML private TableColumn<UserInfo, String> userIDCol;
    @FXML private TableColumn<UserInfo, String> usernameCol;
    @FXML private TableColumn<UserInfo, String> fNameCol;
    @FXML private TableColumn<UserInfo, String> lNameCol;
    @FXML private TableColumn<UserInfo, Boolean> statusCol;

    private List<UserInfo> playersInfo;

    public AllPlayersController() {
        playersInfo = new ArrayList<>();
    }

    public void initialize(){
        userIDCol.setCellValueFactory(new PropertyValueFactory<>("userID"));
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        fNameCol.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lNameCol.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("Active"));
    }

    public ObservableList<UserInfo> getUserInfo(){
        ObservableList<UserInfo> userInfos = FXCollections.observableArrayList();
        userInfos.addAll(playersInfo);
        return userInfos;
    }

    public void setPlayersInfo(List<UserInfo> playersInfo) {
        this.playersInfo = playersInfo;
    }

    public void populate(){
        userTableView.setItems(getUserInfo());
    }

}
