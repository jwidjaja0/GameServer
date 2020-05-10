package com.ExceptionHandled.GameServer.ServerUI.Lobby;

import com.ExceptionHandled.GameMessages.Stats.PlayerStatsInfo;
import com.ExceptionHandled.GameMessages.Stats.PlayerStatsRequest;
import com.ExceptionHandled.GameMessages.UserInfo;
import com.ExceptionHandled.GameMessages.Wrappers.Packet;
import com.ExceptionHandled.GameServer.Database.SQLiteQuery;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class AllPlayersController implements Observer {
    @FXML private TableView<UserInfo> userTableView;
    @FXML private TableColumn<UserInfo, String> userIDCol;
    @FXML private TableColumn<UserInfo, String> usernameCol;
    @FXML private TableColumn<UserInfo, String> fNameCol;
    @FXML private TableColumn<UserInfo, String> lNameCol;
    @FXML private TableColumn<UserInfo, Boolean> statusCol;

    @FXML private Button playerButton;
    @FXML private Button modifyButton;

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

        playerButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                showStats();
            }
        });

        modifyButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                askNewInfo();
            }
        });
    }

    public ObservableList<UserInfo> getUserInfo(){
        ObservableList<UserInfo> userInfos = FXCollections.observableArrayList();
        userInfos.addAll(playersInfo);
        return userInfos;
    }

    private void askNewInfo(){
        String userID = userTableView.getSelectionModel().getSelectedItem().getUserID();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("GetUserInfo.fxml"));
        try {
            Parent root = loader.load();
            GetUserInfoController guic = loader.getController();
            guic.setUserID(userID);
            guic.addObserver(this);

            Stage stage = new Stage();
            stage.setTitle("Update user profile");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public void setPlayersInfo(List<UserInfo> playersInfo) {
        this.playersInfo = playersInfo;
    }

    public void populate(){
        userTableView.setItems(getUserInfo());
    }

    private void showStats() {
        //request player statsinfo from database
        String userID = userTableView.getSelectionModel().getSelectedItem().getUserID();
        Packet packet = new Packet("Stats", userID, new PlayerStatsRequest());
        Packet received = SQLiteQuery.getInstance().getPlayerStatsInfo(packet);
        PlayerStatsInfo playerStatsInfo = (PlayerStatsInfo)received.getMessage();

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                try {
                    FXMLLoader statsScreen = new FXMLLoader(getClass().getResource("StatsViewer.fxml"));
                    Parent statsScreenWindow = statsScreen.load();
                    StatsViewerController svw;
                    svw = statsScreen.getController();
                    svw.updateStats(playerStatsInfo);
                    Stage stage = new Stage();
                    stage.setTitle("Player Game History");
                    stage.setScene(new Scene(statsScreenWindow));
                    stage.show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void update(Observable o, Object arg) {
        //TODO:Implement this to get new value
    }
}
