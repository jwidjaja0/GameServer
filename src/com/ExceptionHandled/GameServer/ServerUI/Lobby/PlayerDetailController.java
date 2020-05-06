package com.ExceptionHandled.GameServer.ServerUI.Lobby;

import com.ExceptionHandled.GameServer.Player;
import javafx.fxml.FXML;
import javafx.scene.text.Text;

public class PlayerDetailController {
    @FXML
    Text idText;
    @FXML
    Text usernameText;
    @FXML
    Text firstnameText;
    @FXML
    Text lastnameText;

    public PlayerDetailController() {
    }

    public void initialize(){

    }

    public void setFields(Player player){
        idText.setText(player.getId());
        usernameText.setText(player.getUsername());
        firstnameText.setText(player.getFirstName());
        lastnameText.setText(player.getLastName());
    }


}
