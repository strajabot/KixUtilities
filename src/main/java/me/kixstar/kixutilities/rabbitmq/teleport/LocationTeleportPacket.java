package me.kixstar.kixutilities.rabbitmq.teleport;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.kixstar.kixutilities.rabbitmq.Packet;

public class LocationTeleportPacket extends Packet {

    private String playerUUID;
    private String serverName;
    private String worldName;

    private double x;
    private double y;
    private double z;

    private float yaw;
    private float pitch;

    public LocationTeleportPacket() {

    }

    public LocationTeleportPacket(
            String playerUUID,
            String serverName,
            String worldName,
            double x,
            double y,
            double z,
            float yaw,
            float pitch
    ) {
        this.playerUUID = playerUUID;
        this.serverName = serverName;
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public byte[] serialize() {
        ByteArrayDataOutput frame = ByteStreams.newDataOutput();

        frame.writeUTF(this.playerUUID);
        frame.writeUTF(this.serverName);
        frame.writeUTF(this.worldName);
        frame.writeDouble(this.x);
        frame.writeDouble(this.y);
        frame.writeDouble(this.z);
        frame.writeFloat(this.yaw);
        frame.writeFloat(this.pitch);

        return frame.toByteArray();

    }

    public void deserialize(byte[] raw) {
        ByteArrayDataInput frame = ByteStreams.newDataInput(raw);

        this.playerUUID = frame.readUTF();
        this.serverName = frame.readUTF();
        this.worldName = frame.readUTF();
        this.x = frame.readDouble();
        this.y = frame.readDouble();
        this.z = frame.readDouble();
        this.yaw = frame.readFloat();
        this.pitch = frame.readFloat();

    }

    public String getPlayerUUID() {
        return playerUUID;
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
