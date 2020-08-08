package me.kixstar.kixutilities.commands;

import me.kixstar.kixutilities.Config;
import me.kixstar.kixutilities.KixUtilities;
import me.kixstar.kixutilities.Location;
import me.kixstar.kixutilities.feature.home.HomeService;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

/**
 * Permissions:
 * //setting homes
 * - kix.utilities.home.self.set - lets player set his homes
 * - kix.utilities.home.target.set - lets player set homes for other players
 * <p>
 * //teleporting to homes
 * - kix.utilities.home.self.tp - lets player teleport to his homes
 * - kix.utilities.home.target.tp - lets player teleport to homes of other players
 * <p>
 * //listing homes
 * - kix.utilities.home.self.list - lets player list his homes
 * - kix.utilities.home.target.list - lets player list homes of other players
 * <p>
 * //deleting homes
 * - kix.utilities.home.self.delete - lets player delete his homes
 * - kix.utilities.home.target.delete - lets player delete homes of other players
 */
public class SethomeCommand extends Command {


    public SethomeCommand() {
        super("sethome");
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            log(Level.WARNING, "Only \"/sethome <playerName> <homeName>\" can be run from console");
            return false;
        }
        Player player = (Player) sender;
        if (args.length == 0) {
            /* "/sethome" */
            this.selfSetHome(player, "default");
        } else if (args.length == 1) {
            /* "/sethome <homeName>" */
            this.selfSetHome(player, args[0]);
        } else if (args.length == 2) {
            /* "/sethome <playerName> <homeName>" */
            this.targetSetHome(player, args[0], args[1]);
        }

        return true;
    }

    /**
     * This method is called when the command has 0 or 1 argument. When this happens it is
     * considered that a player called the command to set their own home.
     *
     * This method returns false if the player doesn't have the permission "kix.utilities.home.self.set".
     * (also sends the player a message saying that he doesn't have permissions to run this command)
     * otherwise it returns true.
     *
     * @param player from which the command was sent
     * @param homeName the name of the home that is being set
     * @return
     */
    public boolean selfSetHome(@NotNull Player player, @NotNull String homeName) {
        if (!player.hasPermission("kix.utilities.home.self.set")) {
            this.noPermission(player);
            return false;
        }
        HomeService.setHome(player.getUniqueId().toString(), homeName, this.getLocation(player));
        return true;
    }

    /**
     * This method is called when the command has 2 arguments. When this happens it is
     * considered that a player called the command to set the home of another player.
     *
     * This method returns false if :
     *  1. The player doesn't have the permission "kix.utilities.home.target.set".
     *      (also sends the player a message saying that he doesn't have permissions to run this command)
     *  2. The target player isn't online.
     *      (also sends the player a message saying that the target player isn't online).
     * otherwise it returns true.
     *
     * @param player player who sent the command
     * @param targetName name of the target player
     * @param homeName the name of the home that is being set
     * @return
     */
    private boolean targetSetHome(@NotNull Player player, @NotNull String targetName, @NotNull String homeName) {
        Validate.notNull(player, "Argument \"player\" can't be null");
        Validate.notNull(targetName, "Argument \"targetName\" can't be null");
        Validate.notNull(homeName, "Argument \"homeName\" can't be null");
        Player target = Bukkit.getServer().getPlayer(targetName);
        if(!player.hasPermission("kix.utilities.home.target.set")) {
            noPermission(player);
            return false;
        }
        if(target == null) {
            targetOffline(player, targetName);
            return false;
        }
        HomeService.setHome(target.getUniqueId().toString(), homeName, this.getLocation(player));
        return true;
    }

    public void log(@NotNull Level level, @NotNull String string) {
        Validate.notNull(level, "Argument \"level\" can't be null");
        Validate.notNull(string, "Argument \"string\" can't be null");
        Plugin plugin = KixUtilities.getInstance();
        plugin.getLogger().log(level, string);
    }

    private void targetOffline(@NotNull Player player, @NotNull String targetName) {
        Validate.notNull(player, "Argument \"player\" can't be null");
        Validate.notNull(targetName, "Argument \"targetName\" can't be null");
        TextComponent targetOffline = new TextComponent("The player " + targetName + " isn't online, so his homes can't be set");
        targetOffline.setColor(ChatColor.RED);
        player.sendMessage(targetOffline);
    }

    private void noPermission(@NotNull Player player) {
        Validate.notNull(player, "Argument \"player\" can't be null");
        TextComponent noPermission = new TextComponent("You don't have permissions needed to run this command");
        noPermission.setColor(ChatColor.RED);
        player.sendMessage(noPermission);
    }

    @NotNull
    private Location getLocation(@NotNull Player player) {
        return Location.convertLocation(player.getLocation(), Config.getServerHandle());
    }

}
