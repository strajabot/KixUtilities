package me.kixstar.kixutilities;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class KixUtilities extends JavaPlugin {
    public void onEnable() {
    }

    public void onDisable() {
    }

    public static KixUtilities getInstance() {
        return Bukkit.getServer().getPluginManager().getPlugin("KixUtilities") == null ? null : (KixUtilities) Bukkit.getServer().getPluginManager().getPlugin("KixUtilities");
    }
}
