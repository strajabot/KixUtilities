package me.kixstar.kixutilities.feature.teleport;

import com.google.common.collect.HashBiMap;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import io.papermc.lib.PaperLib;
import me.kixstar.kixutilities.KixUtilities;
import me.kixstar.kixutilities.custompackets.Packet;
import me.kixstar.kixutilities.custompackets.teleport.ScheduleTeleportPacket;
import me.kixstar.kixutilities.custompackets.teleport.TeleportCancelPacket;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ConcurrentHashMap;

public class BungeeTeleportListener  implements PluginMessageListener, Listener {


    public static BungeeTeleportListener instance = new BungeeTeleportListener();

    public static BungeeTeleportListener get() {
        return instance;
    }

    private BungeeTeleportListener() {
        this.channel = "kixutilities:teleport";
        this.packets = HashBiMap.create();
        this.addExpectedPackets();

        this.teleportIn = new ConcurrentHashMap<>();
        this.teleportOut = ConcurrentHashMap.newKeySet();
    }

    private HashBiMap<String, Class<? extends Packet>> packets;

    private String channel;

    //map of UUIDs of players that have scheduled inbound teleports
    private ConcurrentHashMap<String, Location> teleportIn;

    ConcurrentHashMap.KeySetView<String, Boolean> teleportOut;

    private void addExpectedPackets() {
        this.addPacket("ScheduleTeleportPacket", ScheduleTeleportPacket.class);
        this.addPacket("TeleportCancelPacket", TeleportCancelPacket.class);
    }

    private void addPacket(String identifier, Class<? extends Packet> packet) {
        try {
            packet.getConstructor();
            this.packets.put(identifier, packet);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(
                    new StringBuilder()
                            .append("Every packet needs to have a no arguments constructor and ")
                            .append(packet.getName())
                            .append(" doesn't \n")
                            .append(packet.getName())
                            .append(" will not be listened for")
                            .toString()
            );
        }
    }

    public void send(Packet packet) {
        if (!this.packets.containsValue(packet)) return;
        Plugin plugin = KixUtilities.getInstance();

        //temp workaround, exploitable if connections outside of proxy are allowed.
        Player player = (Player) plugin.getServer().getOnlinePlayers().toArray()[0];

        ByteArrayDataOutput frame = ByteStreams.newDataOutput();

        frame.writeUTF(this.packets.inverse().get(packet));

        frame.write(packet.serialize());

        player.sendPluginMessage(plugin,this.getChannel(), frame.toByteArray());

    }

    public String getChannel() {
        return channel;
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] in) {
        if(!channel.equals(this.getChannel())) return;

        ByteArrayDataInput frame = ByteStreams.newDataInput(in);

        String identifier = frame.readUTF();
        int packetLength = frame.readInt();
        byte[] raw = new byte[packetLength];
        frame.readFully(raw);

        Class<? extends Packet> pClass = this.packets.get(identifier);

        try {
            Packet packet = pClass.getConstructor().newInstance();
            packet.deserialize(raw);

            this.handlePacket(packet);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

    }

    public void handlePacket(Packet handle) {
        Plugin plugin = KixUtilities.getInstance();
        Server server = plugin.getServer();

        if(handle instanceof ScheduleTeleportPacket) {
            ScheduleTeleportPacket packet = (ScheduleTeleportPacket) handle;

            if(packet.getDirection() == 'i') {
                Location location = new Location(
                        server.getWorld(packet.getWorldName()),
                        packet.getX(),
                        packet.getY(),
                        packet.getZ()
                );
                this.teleportIn.put(packet.getPlayerUUID(), location);
                //fail-safe: invalidate teleport if it hasn't been realised in 10 seconds
                server.getScheduler().runTaskLaterAsynchronously(plugin,() -> {
                    this.teleportIn.remove(packet.getPlayerUUID());
                    this.sendCancelTeleport(packet.getPlayerUUID());
                }, 200);
            }

            if(packet.getDirection() == 'o') {
                this.teleportOut.add(packet.getPlayerUUID());

                //fail-safe: invalidate teleport if it hasn't been realised in 10 seconds
                server.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
                    this.teleportOut.remove(packet.getPlayerUUID());
                    this.sendCancelTeleport(packet.getPlayerUUID());
                }, 200);
            }

            if(packet.getDirection() == 'l') {
                //todo: implement local teleports
            }


        }

        if(handle instanceof TeleportCancelPacket) {
            TeleportCancelPacket packet = (TeleportCancelPacket) handle;

            //probably should implement a way to differentiate which teleport was cancelled (unique IDs for teleports)
            this.teleportIn.remove(packet.getPlayerUUID());
            this.teleportOut.remove(packet.getPlayerUUID());

            //local teleports should be handled locally therefor Bungee shouldn't be sending cancel packets
        }

    }

    public void sendCancelTeleport(String playerUUID) {
        TeleportCancelPacket cancelPacket = new TeleportCancelPacket(playerUUID);
        send(cancelPacket);
    }

    @EventHandler
    public void onPlayerMoveEvent(PlayerMoveEvent event)  {
        Player player = event.getPlayer();
        String playerUUID = player.getUniqueId().toString();
        if(event.getFrom().equals(event.getTo())) return;
        this.sendCancelTeleport(playerUUID);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        //Player was moved to a different server by Bungee OR quit the server (Leaving the server is handled by Bungee)
        Player player = event.getPlayer();
        String playerUUID = player.getUniqueId().toString();
        this.teleportOut.remove(playerUUID);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String playerUUID = player.getUniqueId().toString();
        Location location = this.teleportIn.get(playerUUID);
        if(location == null) return;
        PaperLib.teleportAsync(player, location);
    }

}
