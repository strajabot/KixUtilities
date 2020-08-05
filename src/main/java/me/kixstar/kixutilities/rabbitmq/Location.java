package me.kixstar.kixutilities.rabbitmq;

import me.kixstar.kixutilities.rabbitmq.teleport.LocationTeleportPacket;

public class Location {

    private String serverName;
    private String worldName;

    private double x;
    private double y;
    private double z;

    private float yaw;
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

    public Location(
            String serverName,
            String worldName,
            double x,
            double y,
            double z,
            float yaw,
            float pitch
    ) {
        this.serverName = serverName;
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public String getServerName() {
        return serverName;
    }

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
