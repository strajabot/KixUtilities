package me.kixstar.kixutilities.feature.teleport;

import io.papermc.lib.PaperLib;
import me.kixstar.kixutilities.KixUtilities;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.ConcurrentHashMap;

public class TeleportService implements Listener {

    private static TeleportService instance = new TeleportService();
    /**
     * A player is put into this collection when the ExecuteTeleportPacket is received
     * if the teleport target is located on this server and if the player isn't already
     * logged onto this server.
     */
    private ConcurrentHashMap<String, TeleportTransaction> teleportTo = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Player, TeleportTransaction> watchMove = new ConcurrentHashMap<>();

    public static TeleportService get() {
        return instance;
    }

    public static void register() {
        Plugin plugin = KixUtilities.getInstance();
        plugin.getServer().getPluginManager().registerEvents(get(), plugin);
    }

    public static void unregister() {
        HandlerList.unregisterAll(get());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        TeleportTransaction transaction = teleportTo.remove(player.getUniqueId().toString());
        if (transaction == null) return;

        PaperLib.teleportAsync(player, transaction.getLocation());
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        TeleportTransaction transaction = watchMove.get(event.getPlayer());
        if (transaction == null) return;
        //only check for position and not rotation so that players can look around while waiting for their teleport
        if (event.getFrom().toVector().equals(event.getTo().toVector())) return;
        transaction.cancel("Teleport cancelled because player moved");
    }

    public void addWatchMove(Player player, TeleportTransaction transaction) {
        this.watchMove.put(player, transaction);
    }

    public void removeWatchMove(Player player) {
        this.watchMove.remove(player);
    }

    public void scheduleTeleport(String playerUUID, TeleportTransaction transaction) {
        this.teleportTo.put(playerUUID, transaction);

    }
}
