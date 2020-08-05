package me.kixstar.kixutilities;

import me.kixstar.kixutilities.feature.nickname.NicknameListener;
import me.kixstar.kixutilities.feature.servercommand.ServerCommandListener;
import me.kixstar.kixutilities.feature.teleport.TeleportService;
import me.kixstar.kixutilities.feature.teleport.TeleportTransaction;
import me.kixstar.kixutilities.rabbitmq.RabbitMQ;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class KixUtilities extends JavaPlugin {

    public void onEnable() {

        RabbitMQ.bind(Config.getServerHandle());

        NicknameListener.getInstance().register(this);
        ServerCommandListener.register();
        TeleportService.register();
        TeleportTransaction.register();


    }

    public void onDisable() {

        TeleportTransaction.unregister();
        TeleportService.unregister();
        ServerCommandListener.unregister();
        NicknameListener.getInstance().unregister();

        RabbitMQ.unbind();

    }

    public static KixUtilities getInstance() {
        return Bukkit.getServer().getPluginManager().getPlugin("KixUtilities") == null ? null : (KixUtilities) Bukkit.getServer().getPluginManager().getPlugin("KixUtilities");
    }
}
