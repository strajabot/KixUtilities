package me.kixstar.kixutilities.commands;

import com.google.common.base.Preconditions;
import me.kixstar.kixutilities.Config;
import me.kixstar.kixutilities.KixUtilities;
import me.kixstar.kixutilities.database.abstraction.HomeSlotsMaxedException;
import me.kixstar.kixutilities.database.abstraction.player.KixPlayer;
import me.kixstar.kixutilities.database.entities.Location;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.concurrent.CompletableFuture;
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

        boolean isSuccessful = false;
        if (args.length == 0) {
            /* "/sethome" */
            isSuccessful = this.selfSetHome(player, "default");
        } else if (args.length == 1) {
            /* "/sethome <homeName>" */
            isSuccessful = this.selfSetHome(player, args[0]);
        } else if (args.length == 2) {
            /* "/sethome <playerName> <homeName>" */
            isSuccessful = this.targetSetHome(player, args[0], args[1]);
        }

        return isSuccessful;
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
    private boolean selfSetHome(@NotNull Player player, @NotNull String homeName) {
        if(!player.hasPermission("kix.utilities.home.self.set")) {
            this.noPermission(player);
            return false;
        }
        //we reuse the username validation to disallow nonsensical home names.
        if(!KixPlayer.validUsername(homeName)) {
            this.invalidHomeName(player, homeName);
            return false;
        }
        KixPlayer.get(player.getUniqueId().toString()).setHome(homeName, this.getLocation(player));

        TextComponent sucessfulMessage = new TextComponent("Home \"" + homeName +"\" successfully set");
        sucessfulMessage.setColor(ChatColor.GREEN);
        player.sendMessage(sucessfulMessage);

        return true;
    }

    /**
     * This method is called when the command has 2 arguments. When this happens it is
     * considered that a player called the command to set the home of another player.
     *
     * This method returns false if :
     *  1. The player doesn't have the permission "kix.utilities.home.target.set".
     *      (also sends the player a message saying that he doesn't have permissions to run this command)
     * otherwise it returns true.
     *
     * @param player player who sent the command
     * @param targetName name of the target player
     * @param homeName the name of the home that is being set
     * @return true if home setting was successful
     */
    private boolean targetSetHome(@NotNull Player player, @NotNull String targetName, @NotNull String homeName) {
        Preconditions.checkNotNull(player, "Argument \"player\" can't be null");
        Preconditions.checkNotNull(targetName, "Argument \"targetName\" can't be null");
        Preconditions.checkNotNull(homeName, "Argument \"homeName\" can't be null");
        if(!player.hasPermission("kix.utilities.home.target.set")) {
            noPermission(player);
            return false;
        }
        //we reuse the username validation to disallow nonsensical home names.
        if(!KixPlayer.validUsername(homeName)) {
            invalidHomeName(player, homeName);
            return false;
        }
        //we reuse the username validation to disallow nonsensical home names.
        if(!KixPlayer.validUsername(targetName)) {
            invalidUsername(player, targetName);
            return false;
        }
        //todo: fix not knowing if a player with that username exists
        OfflinePlayer target = Bukkit.getServer().getOfflinePlayer(targetName);
        KixPlayer targetKixPlayer = KixPlayer.get(target.getUniqueId().toString());
        CompletableFuture<Void> setHomeFuture = targetKixPlayer.setHome(homeName, this.getLocation(player));

        setHomeFuture.whenComplete((ignore, ex) -> {
           if(ex == null) {
               TextComponent successfulMessage = new TextComponent("Home \"" + targetName + ":" +homeName +"\" successfully set");
               successfulMessage.setColor(ChatColor.GREEN);
               player.sendMessage(successfulMessage);
           } else if(ex instanceof HomeSlotsMaxedException) {
               TextComponent slotsMaxedMessage = new TextComponent("Can't set " + targetName+ "'s home since he doesn't have any empty slots");
               slotsMaxedMessage.setColor(ChatColor.RED);
               TextComponent deleteOrOverwriteMessage = new TextComponent("You can either free some slots using /delhome or overwrite existing slots");
               deleteOrOverwriteMessage.setColor(ChatColor.RED);
               player.sendMessage(slotsMaxedMessage);
               player.sendMessage(deleteOrOverwriteMessage);
           } else {
               unexpectedError(player, targetName, homeName, ex);
           }
        });

        //being optimistic that the database request will complete correctly.
        return true;
    }

    private void log(@NotNull Level level, @NotNull String string) {
        Preconditions.checkNotNull(level, "Argument \"level\" can't be null");
        Preconditions.checkNotNull(string, "Argument \"string\" can't be null");
        Plugin plugin = KixUtilities.getInstance();
        plugin.getLogger().log(level, string);
    }

    private void noPermission(@NotNull Player player) {
        Preconditions.checkNotNull(player, "Argument \"player\" can't be null");
        TextComponent noPermission = new TextComponent("You don't have permissions needed to run this command");
        noPermission.setColor(ChatColor.RED);
        player.sendMessage(noPermission);
    }

    private void invalidHomeName(@NotNull Player player, @NotNull String homeName) {
        Preconditions.checkNotNull(player, "Argument \"player\" can't be null");
        Preconditions.checkNotNull(homeName, "Argument \"homeName\" can't be null");
        TextComponent invalidHomeName = new TextComponent("\"" + homeName + "\" isn't a valid home name\n");
        invalidHomeName.setColor(ChatColor.RED);
        String[] homeNameCriteria = new String[]{
                ChatColor.RED + "Home names can contain all letters of the English Alphabet",
                ChatColor.RED + "Home names can contain all digits",
                ChatColor.RED + "Home names can contain underscores",
                ChatColor.RED + "Home names mustn't be shorter than 3 characters",
                ChatColor.RED + "Home names mustn't be longer than 16 characters"
        };
        player.sendMessage(invalidHomeName);
        player.sendMessage(homeNameCriteria);
    }

    private void invalidUsername(@NotNull Player player, @NotNull String username) {
        Preconditions.checkNotNull(player, "Argument \"player\" can't be null");
        Preconditions.checkNotNull(username, "Argument \"username\" can't be null");
        TextComponent invalidHomeName = new TextComponent("\"" + username + "\" isn't a valid username\n");
        invalidHomeName.setColor(ChatColor.RED);
        String[] homeNameCriteria = new String[]{
                ChatColor.RED + "Username can contain all letters of the English Alphabet",
                ChatColor.RED + "Username can contain all digits",
                ChatColor.RED + "Username can contain underscores",
                ChatColor.RED + "Username mustn't be shorter than 3 characters",
                ChatColor.RED + "Username mustn't be longer than 16 characters"
        };
        player.sendMessage(invalidHomeName);
        player.sendMessage(homeNameCriteria);
    }

    private void unexpectedError(
            @NotNull Player player,
            @NotNull String targetName,
            @NotNull String homeName,
            @NotNull Throwable throwable
    ) {
        Preconditions.checkNotNull(player, "Argument \"player\" can't be null");
        Preconditions.checkNotNull(targetName, "Argument \"targetName\" can't be null");
        Preconditions.checkNotNull(homeName, "Argument \"homeName\" can't be null");
        Preconditions.checkNotNull(throwable, "Argument \"throwable\" can't be null");

        String formattedStackTrace = getFormattedTrace(throwable);

        TextComponent stackTrace = new TextComponent();
        stackTrace.setText(formattedStackTrace);
        stackTrace.setColor(ChatColor.RED);
        TextComponent errMessage = new TextComponent();
        errMessage.setText("Couldn't set home " + targetName + ":" + homeName + " because of an internal error:\n");
        errMessage.setColor(ChatColor.RED);
        errMessage.addExtra(stackTrace);
        errMessage.addExtra("\nPlease report to staff as soon as possible");

        this.log(Level.SEVERE, errMessage.toString());

        player.sendMessage(errMessage);
    }

    @NotNull
    private String getFormattedTrace(@NotNull Throwable throwable) {
        Preconditions.checkNotNull(throwable, "Argument \"throwable\" can't be null");
        Writer stringWriter = new StringWriter();
        throwable.printStackTrace(new PrintWriter(stringWriter));
        return stringWriter.toString();
    }

    @NotNull
    private Location getLocation(@NotNull Player player) {
        org.bukkit.Location playerLocation = player.getLocation();
        return new Location(
                Config.getServerHandle(),
                playerLocation.getWorld().getName(),
                playerLocation.getX(),
                playerLocation.getY(),
                playerLocation.getZ(),
                playerLocation.getYaw(),
                playerLocation.getPitch()
        );
    }

}
