package me.kixstar.kixutilities.custompackets.teleport;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.kixstar.kixutilities.custompackets.Packet;

public class TeleportCancelPacket implements Packet {

    String playerUUID;

    public TeleportCancelPacket() {
    }

    public TeleportCancelPacket(String playerUUID) {
        this.playerUUID = playerUUID;
    }

    @Override
    public byte[] serialize() {
        ByteArrayDataOutput frame = ByteStreams.newDataOutput();

        frame.writeUTF(this.playerUUID);

        return frame.toByteArray();
    }

    @Override
    public void deserialize(byte[] raw) {
        ByteArrayDataInput frame = ByteStreams.newDataInput(raw);

        this.playerUUID = frame.readUTF();
    }

    public String getPlayerUUID() {
        return playerUUID;
    }

}
