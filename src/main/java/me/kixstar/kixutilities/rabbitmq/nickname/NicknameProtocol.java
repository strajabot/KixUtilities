package me.kixstar.kixutilities.rabbitmq.nickname;

import me.kixstar.kixutilities.rabbitmq.CustomProtocol;

public class NicknameProtocol extends CustomProtocol {

    public NicknameProtocol() {
        super.addPacket("NicknameChangePacket", NicknameChangePacket.class);
        super.addPacket("NicknameClearPacket", NicknameClearPacket.class);
    }

}
