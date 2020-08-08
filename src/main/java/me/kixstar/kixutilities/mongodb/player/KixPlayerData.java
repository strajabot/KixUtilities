package me.kixstar.kixutilities.mongodb.player;

import me.kixstar.kixutilities.Location;
import org.bson.codecs.pojo.annotations.BsonProperty;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

public class KixPlayerData {

    //todo: implement document changing.
    //considering making this immutable so that it always represents the state of the document in the database

    @BsonProperty(value = "uuid")
    private String playerUUID;
    @BsonProperty(value = "nickname")
    private String nickname;
    @BsonProperty(value = "homes")
    private Map<String, Location> homes;
    @BsonProperty(value = "balance")
    private int balance;

    public String getPlayerUUID() {
        return this.playerUUID;
    }

    public void setPlayerUUID(String playerUUID) {
        this.playerUUID = playerUUID;
    }

    public String getNickname() {
        return this.nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Map<String, Location> getHomes() {
        return new HashMap<>(this.homes);
    }

    public void setHomes(Map<String, Location> homes) {
        this.homes = new HashMap<>(homes);
    }

    public int getBalance() {
        return this.balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }
}
