package me.kixstar.kixutilities.rabbitmq.servercommand;

import me.kixstar.kixutilities.rabbitmq.CustomProtocol;

public class ServerCommandProtocol extends CustomProtocol {

    public ServerCommandProtocol() {
        this.addPacket("SubscribeTeleportPacket", SubscribeTeleportPacket.class);
        this.addPacket("CommandStatusPacket", CommandStatusPacket.class);
    }

}


