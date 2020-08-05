package me.kixstar.kixutilities.rabbitmq.teleport;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.kixstar.kixutilities.rabbitmq.Packet;

public class CancelTeleportPacket extends Packet {

    private String reason;

    public CancelTeleportPacket() {
    }

    public CancelTeleportPacket(String reason) {
        this.reason = reason;
    }

    @Override
    public byte[] serialize() {
        ByteArrayDataOutput frame = ByteStreams.newDataOutput();

        frame.writeUTF(this.reason);

        return frame.toByteArray();
    }

    @Override
    public void deserialize(byte[] raw) {
        ByteArrayDataInput frame = ByteStreams.newDataInput(raw);

        this.reason = frame.readUTF();

    }

}
