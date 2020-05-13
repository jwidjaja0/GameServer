package com.ExceptionHandled.GameServer.ServerUI.Lobby;

import com.ExceptionHandled.GameServer.InternalMessage.UserInvolvement;
import com.ExceptionHandled.GameServer.Player;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.text.Text;

import java.util.List;

public class PlayerDetailController {
    @FXML
    Text idText;
    @FXML
    Text usernameText;
    @FXML
    Text firstnameText;
    @FXML
    Text lastnameText;
    @FXML
    ListView gamesPlayingList;
    @FXML
    ListView gamesViewingList;

    UserInvolvement userInvolvement;

    public PlayerDetailController() {
    }

    public void initialize(){

    }

    public UserInvolvement getUserInvolvement() {
        return userInvolvement;
    }

    public void setUserInvolvement(UserInvolvement userInvolvement) {
        this.userInvolvement = userInvolvement;
    }

    public void setFields(Player player){
        idText.setText(player.getId());
        usernameText.setText(player.getUsername());
        firstnameText.setText(player.getFirstName());
        lastnameText.setText(player.getLastName());

        gamesPlayingList.getItems().addAll(userInvolvement.getGamesPlaying());
        gamesViewingList.getItems().addAll(userInvolvement.getGamesViewing());

    }


}
