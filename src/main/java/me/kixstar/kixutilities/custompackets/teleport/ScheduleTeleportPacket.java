package me.kixstar.kixutilities.custompackets.teleport;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.kixstar.kixutilities.custompackets.Packet;

public class ScheduleTeleportPacket implements Packet {
    //todo: add support for rotation
    private String playerUUID;
    private String serverName;
    private String worldName;
    private double x;
    private double y;
    private double z;
    /**
     *  - 'i' for inbound teleport
     *  - 'o' for outbound teleport
     *  - 'l' for local teleport (target is on the same Spigot instance as the player)
     */
    private char direction;

    public void deserialize(byte[] raw) {
        ByteArrayDataInput frame = ByteStreams.newDataInput(raw);

        this.playerUUID = frame.readUTF();
        this.serverName = frame.readUTF();
        this.worldName = frame.readUTF();
        this.x = frame.readDouble();
        this.y = frame.readDouble();
        this.z = frame.readDouble();
        this.direction = frame.readChar();

    }

    public ScheduleTeleportPacket() {

    }

    public ScheduleTeleportPacket(
            String playerUUID,
            String serverName,
            String worldName,
            double x,
            double y,
            double z,
            char direction
    ) {
        this.playerUUID = playerUUID;
        this.serverName = serverName;
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
        this.direction = direction;
    }

    public byte[] serialize() {
        ByteArrayDataOutput frame = ByteStreams.newDataOutput();

        frame.writeUTF(this.playerUUID);
        frame.writeUTF(this.serverName);
        frame.writeUTF(this.worldName);
        frame.writeDouble(this.x);
        frame.writeDouble(this.y);
        frame.writeDouble(this.z);
        frame.writeChar(this.direction);

        return frame.toByteArray();

    }

    public String getPlayerUUID() {
        return this.playerUUID;
    }

    public String getServerName() {
        return this.serverName;
    }

    public String getWorldName() {
        return this.worldName;
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public double getZ() {
        return this.z;
    }

    public char getDirection() {return this.direction;}


}
