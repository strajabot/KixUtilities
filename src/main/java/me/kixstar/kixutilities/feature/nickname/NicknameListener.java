package me.kixstar.kixutilities.feature.nickname;

import me.kixstar.kixutilities.KixstarDB;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;

public class NicknameListener implements Listener {

    private static NicknameListener instance = new NicknameListener();

    public static NicknameListener getInstance() {
        return instance;
    }

    private NicknameListener() {}

    private HashMap<Player, NicknameSynchronizer> managedPlayers = new HashMap();

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        this.addSynchronizer(player);
    }

    @EventHandler
    public void  onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        this.managedPlayers.remove(player).unbind();
    }

    private void addSynchronizer(Player player) {
        player.setDisplayName(KixstarDB.getNickname(player));
        NicknameSynchronizer syncr = new NicknameSynchronizer(player);
        syncr.bind();
        this.managedPlayers.put(player, syncr);
    }

    public void register(Plugin plugin) {
        //makes sure that players get tracked even after reload
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            this.addSynchronizer(player);
        }

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void unregister() {
        //destroy RabbitMQ consumers
        for (NicknameSynchronizer syncr : this.managedPlayers.values()) {
            syncr.unbind();
        }

        this.managedPlayers.clear();
        HandlerList.unregisterAll(this);
    }
}
