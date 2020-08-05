package me.kixstar.kixutilities.rabbitmq.teleport;

import me.kixstar.kixutilities.rabbitmq.CustomProtocol;

public class TeleportProtocol extends CustomProtocol {

    public TeleportProtocol() {
        this.addPacket("ScheduleLocationTeleportPacket", LocationTeleportPacket.class);
        this.addPacket("SchedulePlayerTeleportPacket", PlayerTeleportPacket.class);
        this.addPacket("CancelTeleportPacket", CancelTeleportPacket.class);
        this.addPacket("ExecuteTeleportPacket", ExecuteTeleportPacket.class);
        this.addPacket("ConfirmReadyTeleportPacket", ConfirmReadyTeleportPacket.class);
    }

}
