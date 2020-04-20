package com.ExceptionHandled.GameServer.Game;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Class that handles
 */
public class HumanPlayer extends Player {

    private String username;
    private String password;
    private BufferedImage token;

    public HumanPlayer(String username, String password){
        this.username = username;
        this.password = password;
    }

    public HumanPlayer() throws IOException {
        this("Guest", "");
        setToken(ImageIO.read(getClass().getResourceAsStream("/resources/images/XBlack.png")));
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
