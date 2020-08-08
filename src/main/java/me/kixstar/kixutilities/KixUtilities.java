package me.kixstar.kixutilities;

import me.kixstar.kixutilities.commands.SethomeCommand;
import me.kixstar.kixutilities.feature.nickname.NicknameListener;
import me.kixstar.kixutilities.feature.servercommand.ServerCommandListener;
import me.kixstar.kixutilities.feature.teleport.TeleportService;
import me.kixstar.kixutilities.feature.teleport.TeleportTransaction;
import me.kixstar.kixutilities.rabbitmq.RabbitMQ;
import net.luckperms.api.LuckPerms;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class KixUtilities extends JavaPlugin {

    private static RegisteredServiceProvider<LuckPerms> luckPermsRSP;

    public void onEnable() {

        luckPermsRSP = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        Validate.notNull(luckPermsRSP, "This plugin depends on LuckPerms to run");

        RabbitMQ.bind(Config.getServerHandle());

        NicknameListener.getInstance().register(this);
        ServerCommandListener.register();
        TeleportService.register();
        TeleportTransaction.register();

        this.getServer().getCommandMap().register("kix", new SethomeCommand());
    }

    public void onDisable() {

        TeleportTransaction.unregister();
        TeleportService.unregister();
        ServerCommandListener.unregister();
        NicknameListener.getInstance().unregister();

        RabbitMQ.unbind();

    }

    @Nullable
    public static KixUtilities getInstance() {
        return Bukkit.getServer().getPluginManager().getPlugin("KixUtilities") == null ? null : (KixUtilities) Bukkit.getServer().getPluginManager().getPlugin("KixUtilities");
    }

    @NotNull
    public static LuckPerms getLuckPerms() {
        return luckPermsRSP.getProvider();
    }
}
