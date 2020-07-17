package me.kixstar.kixutilities.feature.nickname;

import me.kixstar.kixutilities.KixUtilities;
import me.kixstar.kixutilities.KixstarDB;
import me.kixstar.kixutilities.rabbitmq.Packet;
import me.kixstar.kixutilities.rabbitmq.ProtocolChannelInput;
import me.kixstar.kixutilities.rabbitmq.RabbitMQ;
import me.kixstar.kixutilities.rabbitmq.nickname.NicknameChangePacket;
import me.kixstar.kixutilities.rabbitmq.nickname.NicknameClearPacket;
import me.kixstar.kixutilities.rabbitmq.nickname.NicknameProtocol;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Logger;

//This listens for nickname
public class NicknameSynchronizer {

    private Player player;

    NicknameSynchronizer(Player player) {
        this.player = player;
    }

    private ProtocolChannelInput PCI = new ProtocolChannelInput(new NicknameProtocol()) {
        @Override
        public void onPacket(Packet in) {
            Player player = getPlayer(PCI.getRoute());
            if(player == null) return;
            if(in instanceof NicknameChangePacket) {
                NicknameChangePacket packet = (NicknameChangePacket) in;
                runSync((ignore) -> setNickname(player, packet.getNickname()));
            } else if( in instanceof NicknameClearPacket) {
                runSync((ignore) -> setNickname(player, null));
            }
        }
    };


    private void setNickname(Player player, String nickname) {
        String oldNickname = player.getDisplayName();
        player.setDisplayName(nickname);
        Logger logger = KixUtilities.getInstance().getLogger();
        logger.info(player.getName()+":Change nickname:"+oldNickname+"->"+nickname);
    }

    private void runSync(Consumer callback) {
        Plugin plugin = KixUtilities.getInstance();
        plugin.getServer().getScheduler().runTask(plugin, callback);
    }

    private Player getPlayer(String playerUUID) {
        return KixUtilities.getInstance().getServer().getPlayer(UUID.fromString(playerUUID));
    }

    public void bind() {
        PCI.bind(RabbitMQ.getChannel(), "nickname", "topic", player.getUniqueId().toString());
    }

    public void unbind() {
        PCI.unbind();
    }

}
