package me.kixstar.kixutilities;

import me.kixstar.kixutilities.feature.nickname.BungeeNicknameSync;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class KixUtilities extends JavaPlugin {
    public void onEnable() {

        BungeeNicknameSync bungeeNicknameSync = new BungeeNicknameSync();
        getServer().getMessenger().registerIncomingPluginChannel(this, "kixutilities:nickname", bungeeNicknameSync);
        getServer().getPluginManager().registerEvents(bungeeNicknameSync,this);

    }

    public void onDisable() {
        getServer().getMessenger().unregisterIncomingPluginChannel(this);
    }

    public static KixUtilities getInstance() {
        return Bukkit.getServer().getPluginManager().getPlugin("KixUtilities") == null ? null : (KixUtilities) Bukkit.getServer().getPluginManager().getPlugin("KixUtilities");
    }
}
