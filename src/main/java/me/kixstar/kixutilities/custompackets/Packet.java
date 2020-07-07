package me.kixstar.kixutilities.custompackets;

public interface Packet {

    byte[] serialize();

    void deserialize(byte[] raw);
}
