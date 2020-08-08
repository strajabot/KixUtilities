package me.kixstar.kixutilities;

import me.kixstar.kixutilities.rabbitmq.teleport.LocationTeleportPacket;
import org.apache.commons.lang.Validate;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.jetbrains.annotations.NotNull;

public class Location {

    @BsonProperty(value = "server_name")
    private String serverName;
    @BsonProperty(value = "world_name")
    private String worldName;

    @BsonProperty(value = "x")
    private double x;
    @BsonProperty(value = "y")
    private double y;
    @BsonProperty(value = "z")
    private double z;

    @BsonProperty(value = "yaw")
    private float yaw;
    @BsonProperty(value = "pitch")
    private float pitch;

    public Location(LocationTeleportPacket packet) {
        this(
                packet.getServerName(),
                packet.getWorldName(),
                packet.getX(),
                packet.getY(),
                packet.getZ(),
                packet.getYaw(),
                packet.getPitch()
        );
    }

    @NotNull
    public static Location convertLocation(
            @NotNull org.bukkit.Location location,
            @NotNull String serverName
    ) {
        Validate.notNull(location, "Argument \"location\" can't be null");
        Validate.notNull(serverName, "Argument \"serverName\" can't be null");
        return new Location(
                serverName,
                location.getWorld().getName(),
                location.getX(),
                location.getY(),
                location.getZ(),
                location.getYaw(),
                location.getPitch()
        );
    }

    public Location(
            @NotNull String serverName,
            @NotNull String worldName,
            double x,
            double y,
            double z,
            float yaw,
            float pitch
    ) {
        Validate.notNull(serverName, "Argument \"serverName\" can't be null");
        Validate.notNull(worldName, "Argument \"worldName\" can't be null");
        this.serverName = serverName;
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    @NotNull
    public String getServerName() {
        return serverName;
    }

    @NotNull
    public String getWorldName() {
        return worldName;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

}
