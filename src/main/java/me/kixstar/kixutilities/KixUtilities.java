package me.kixstar.kixutilities;

import me.kixstar.kixutilities.feature.nickname.NicknameListener;
import me.kixstar.kixutilities.feature.nickname.NicknameSynchronizer;
import me.kixstar.kixutilities.rabbitmq.RabbitMQ;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class KixUtilities extends JavaPlugin {
    public void onEnable() {

        RabbitMQ.bind();

        NicknameListener.getInstance().register(this);

    }

    public void onDisable() {

        NicknameListener.getInstance().unregister();

        RabbitMQ.unbind();

    }

    public static KixUtilities getInstance() {
        return Bukkit.getServer().getPluginManager().getPlugin("KixUtilities") == null ? null : (KixUtilities) Bukkit.getServer().getPluginManager().getPlugin("KixUtilities");
    }
}
