package me.kixstar.kixutilities.rabbitmq.teleport;

import me.kixstar.kixutilities.rabbitmq.Packet;

public class ExecuteTeleportPacket extends Packet {

    public ExecuteTeleportPacket() {
    }

    @Override
    public byte[] serialize() {
        return new byte[]{(byte) 1};
    }

    @Override
    public void deserialize(byte[] raw) {
    }
}
