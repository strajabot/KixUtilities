package me.kixstar.kixutilities.feature.teleport;

import com.rabbitmq.client.AMQP;
import me.kixstar.kixutilities.KixUtilities;
import me.kixstar.kixutilities.Location;
import me.kixstar.kixutilities.rabbitmq.*;
import me.kixstar.kixutilities.rabbitmq.teleport.*;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class TeleportTransaction implements Listener {

    private static ConcurrentHashMap<String, TeleportTransaction> instances = new ConcurrentHashMap<>();

    private static ProtocolChannelOutput PCO = new ProtocolChannelOutput(new TeleportProtocol());
    private final ReentrantLock dataLock = new ReentrantLock();
    private String transactionID;
    private boolean isExecuted = false;
    //false - player to location, true - player to player
    private boolean teleportType;
    //true if the teleporting player is in this server
    private boolean teleportOutbound;
    //true if the target location/player is in this server
    private boolean teleportInbound;
    private String playerUUID;
    private String targetUUID;
    private Location targetLocation;
    private ProtocolChannelInput PCI = new ProtocolChannelInput(new TeleportProtocol()) {
        @Override
        public void onPacket(Packet in) {
            Plugin plugin = KixUtilities.getInstance();
            try {
                if (in instanceof CancelTeleportPacket) {
                    CancelTeleportPacket packet = (CancelTeleportPacket) in;
                    if (RabbitMQ.isFromThisServer(packet))
                        throw new IllegalStateException("Received CancelTeleportPacket from server with the same origin as this server");
                    handleCancel();
                    System.out.println("server " + RabbitMQ.getOrigin(packet) + " cancelled transaction: " + this.getRoute());
                } else if (in instanceof ExecuteTeleportPacket) {
                    handleExecute(in);
                } else if (in instanceof PlayerTeleportPacket) {
                    handlePlayerTeleport((PlayerTeleportPacket) in);
                } else if (in instanceof LocationTeleportPacket) {
                    handleLocationTeleport((LocationTeleportPacket) in);
                }
            } catch (UnknownPacketOriginException e) {
                e.printStackTrace();
            }
        }
    };

    public TeleportTransaction(String transactionID) {
        this.transactionID = transactionID;
    }

    //NOTE: ALL OF THE HANDLE METHODS MAY BE CALLED FROM MULTIPLE THREADS

    public static void register() {
        PCO.bind(RabbitMQ.getChannel(), "teleport", "direct");
    }

    public static void unregister() {
        PCO.unbind();
        instances.values().forEach(TeleportTransaction::unbind);
    }

    private void handlePlayerTeleport(PlayerTeleportPacket packet) {
        this.dataLock.lock();
        try {
            this.teleportType = true;
            this.playerUUID = packet.getPlayerUUID();
            this.targetUUID = packet.getTargetUUID();
            this.teleportOutbound = (getPlayer(this.playerUUID) != null);
            this.teleportInbound = (getPlayer(this.targetUUID) != null);
            this.targetLocation = null;

            this.sendConfirmResponse(packet);
            if (this.teleportOutbound) TeleportService.get().addWatchMove(getPlayer(this.playerUUID), this);
        } finally {
            this.dataLock.unlock();
        }
    }

    private void handleLocationTeleport(LocationTeleportPacket packet) {
        this.dataLock.lock();
        try {
            this.teleportType = false;
            this.playerUUID = packet.getPlayerUUID();
            this.targetUUID = null;
            this.teleportOutbound = (getPlayer(playerUUID) != null);
            this.teleportInbound = RabbitMQ.getOrigin().equals(packet.getServerName());
            this.targetLocation = new Location(packet);

            this.sendConfirmResponse(packet);
            if (this.teleportOutbound) TeleportService.get().addWatchMove(getPlayer(this.playerUUID), this);
        } finally {
            this.dataLock.unlock();
        }
    }

    private void handleExecute(Packet packet) {
        this.dataLock.lock();
        Plugin plugin = KixUtilities.getInstance();
        try {
            if (this.teleportInbound) {
                Player player = getPlayer(this.playerUUID);
                if (player != null) {
                    //if player is already on the server schedule a teleport to the next tick
                    plugin.getServer().getScheduler().runTask(
                            plugin,
                            () -> player.teleportAsync(getLocation())
                    );
                } else {
                    TeleportService.get().scheduleTeleport(this.playerUUID, get());
                }
            } else {
                TeleportService.get().removeWatchMove(getPlayer(this.playerUUID));
            }
            this.sendConfirmResponse(packet);
            unbind();
        } finally {
            this.dataLock.unlock();
        }

    }

    private void handleCancel() {
        this.unbind();
    }

    private void sendConfirmResponse(Packet packet) {
        AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
                .correlationId(packet.getProperties().getCorrelationId())
                .build();

        Packet out = new ConfirmReadyTeleportPacket(this.teleportOutbound, this.teleportInbound);
        out.setProperties(properties);
        PCO.sendPacket(out, this.transactionID);
    }

    public void cancel(String reason) {
        Packet packet = new CancelTeleportPacket(reason);
        PCO.sendPacket(packet, this.transactionID);
        this.handleCancel();
    }

    public org.bukkit.Location getLocation() {
        this.dataLock.lock();
        try {
            if (this.teleportType) {
                return getPlayer(this.targetUUID).getLocation();
            } else {
                World world = KixUtilities.getInstance().getServer().getWorld(this.targetLocation.getWorldName());
                if (world == null)
                    throw new IllegalStateException("World is null when it shouldn't be");
                return new org.bukkit.Location(
                        world,
                        this.targetLocation.getX(),
                        this.targetLocation.getY(),
                        this.targetLocation.getZ(),
                        this.targetLocation.getYaw(),
                        this.targetLocation.getPitch()
                );
            }
        } finally {
            this.dataLock.unlock();
        }
    }

    /**
     * This is thread safe according to:
     * https://github.com/EngineHub/WorldEdit/blob/50a744f4347230183d18958409dcb5435ba6eced/worldedit-bukkit/src/main/java/com/sk89q/worldedit/bukkit/BukkitPlayer.java#L275
     */
    private Player getPlayer(String uuid) {
        return KixUtilities.getInstance().getServer().getPlayer(UUID.fromString(uuid));
    }

    public void bind() {
        this.PCI.bind(RabbitMQ.getChannel(), "teleport", "direct", this.transactionID);
        instances.put(transactionID, this);
    }

    //this shouldn't be called inside a consumer
    public void unbind() {
        if (teleportOutbound) TeleportService.get().removeWatchMove(getPlayer(this.playerUUID));
        this.PCI.unbind();
        instances.remove(this.transactionID);
    }

    //todo: fix this bodge
    private TeleportTransaction get() {
        return this;
    }

}
