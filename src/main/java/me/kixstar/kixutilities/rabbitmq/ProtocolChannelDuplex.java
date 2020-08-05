package me.kixstar.kixutilities.rabbitmq;

//todo: implement
public abstract class ProtocolChannelDuplex extends ProtocolChannelInput {



    private String channel;

    private ProtocolChannelDuplex(CustomProtocol protocol) {
        super(protocol);
        this.channel = channel;

    }

    public void send(Packet packet, String route) {

        //server.sendData(this.getChannel(), frame.toByteArray());

    }

    public String getChannel() {
        return channel;
    }

}
