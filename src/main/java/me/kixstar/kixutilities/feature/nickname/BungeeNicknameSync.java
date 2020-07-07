package me.kixstar.kixutilities.feature.nickname;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.kixstar.kixutilities.KixUtilities;
import me.kixstar.kixutilities.KixstarDB;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

//This listens for nickname
public class BungeeNicknameSync implements PluginMessageListener, Listener {

    @Override
    public void onPluginMessageReceived(String channel, Player bungee, byte[] message) {
        if(!channel.equals("kixutilities:nickname")) return;
        ByteArrayDataInput in = ByteStreams.newDataInput( message );
        String command = in.readUTF();
        Server server = Bukkit.getServer();

        if (command.equals("NicknameChangeEvent")) {
            String uuid = in.readUTF();
            String nickname = in.readUTF();
            Player player = server.getPlayer(uuid);
            if(player == null) return;
            player.setDisplayName(nickname);

        }

        if(command.equals("NicknameClearEvent")) {
            String uuid = in.readUTF();
            Player player = server.getPlayer(uuid);
            if(player == null) return;
            player.setDisplayName(null);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String nickname = KixstarDB.getNickname(player);
        player.setDisplayName(null);
    }
}
