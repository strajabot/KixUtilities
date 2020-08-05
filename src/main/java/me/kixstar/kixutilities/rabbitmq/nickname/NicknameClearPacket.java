package me.kixstar.kixutilities.rabbitmq.nickname;

import me.kixstar.kixutilities.rabbitmq.Packet;

public class NicknameClearPacket extends Packet {

    //no args constructor is required for deserialization of packets
    public NicknameClearPacket() {}

    @Override
    public byte[] serialize() { return new byte[]{(byte) 1}; }

    @Override
    public void deserialize(byte[] raw) {}

}
