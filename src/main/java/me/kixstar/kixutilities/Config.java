package me.kixstar.kixutilities;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;

public class Config {

    private static Configuration config;

    private static Configuration getConfig() {
        Plugin plugin = KixUtilities.getInstance();
        if(config instanceof Configuration) return config;
        config = plugin.getConfig();
        return config;
    }

    public static boolean isProd() {
        return getConfig().getBoolean("environment", false);
    }

    public static String getRabbitMQ() {
        return getConfig().getString("rabbit-mq", "amqp://admin:root@localhost:5672");
    }

    public static String getServerHandle() {
        Object result = getConfig().get("server-handle");
        if(result instanceof String) return (String) result;
        throw new RuntimeException("Property \"server-handle\" must be provided in the config.yml so the plugin can function properly");
    }
}
