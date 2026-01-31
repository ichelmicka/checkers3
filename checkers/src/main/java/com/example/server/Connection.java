package com.example.server;
import com.example.model.Player;

public interface Connection {
    void send(String msg);
    void setPlayer(Player p);
    Player getPlayer();
}
