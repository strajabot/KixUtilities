package me.kixstar.kixutilities.feature.servercommand;

import com.rabbitmq.client.AMQP;
import me.kixstar.kixutilities.KixUtilities;
import me.kixstar.kixutilities.feature.teleport.TeleportTransaction;
import me.kixstar.kixutilities.rabbitmq.Packet;
import me.kixstar.kixutilities.rabbitmq.ProtocolChannelInput;
import me.kixstar.kixutilities.rabbitmq.ProtocolChannelOutput;
import me.kixstar.kixutilities.rabbitmq.RabbitMQ;
import me.kixstar.kixutilities.rabbitmq.servercommand.CommandStatusPacket;
import me.kixstar.kixutilities.rabbitmq.servercommand.ServerCommandProtocol;
import me.kixstar.kixutilities.rabbitmq.servercommand.SubscribeTeleportPacket;
import org.bukkit.plugin.Plugin;

import java.util.function.Consumer;

public class ServerCommandListener {

    private static ProtocolChannelOutput PCO = new ProtocolChannelOutput(new ServerCommandProtocol());

    private static ProtocolChannelInput PCI = new ProtocolChannelInput(new ServerCommandProtocol()) {
        @Override
        public void onPacket(Packet in) {
            if (in instanceof SubscribeTeleportPacket) {
                SubscribeTeleportPacket packet = (SubscribeTeleportPacket) in;
                Packet out = new CommandStatusPacket(true);
                //preserve correlation ID
                AMQP.BasicProperties inProps = in.getProperties();
                AMQP.BasicProperties outProps =
                        inProps.builder().replyTo(null).build();

                out.setProperties(outProps);

                new TeleportTransaction(packet.getTransactionID()).bind();

                PCO.sendPacket(out, in.getProperties().getReplyTo());
            }
        }
    };

    private static void runSync(Consumer callback) {
        Plugin plugin = KixUtilities.getInstance();
        plugin.getServer().getScheduler().runTask(plugin, callback);
    }

    public static void register() {
        PCI.bind(RabbitMQ.getChannel(), "server-command", "direct", RabbitMQ.getOrigin());
        PCO.bind(RabbitMQ.getChannel(), "", "direct");
    }

    public static void unregister() {
        PCI.unbind();
        PCO.unbind();
    }

}
