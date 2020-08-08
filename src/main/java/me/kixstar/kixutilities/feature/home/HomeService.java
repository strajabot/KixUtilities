package me.kixstar.kixutilities.feature.home;


import me.kixstar.kixutilities.Config;
import me.kixstar.kixutilities.KixUtilities;
import me.kixstar.kixutilities.Location;
import me.kixstar.kixutilities.mongodb.player.KixPlayer;
import me.kixstar.kixutilities.mongodb.player.KixPlayerData;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.model.user.User;
import net.luckperms.api.query.QueryOptions;
import org.apache.commons.lang.Validate;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class HomeService {

    /** NOTE: permissions are not checked by this method, instead they are checked inside the command listeners
     *  However, the maximum number of houses of the player is checked here.
     */
    public static CompletableFuture<Boolean> setHome(
            @NotNull String playerUUID,
            @NotNull String homeName,
            @NotNull Location location
    ) {
        Validate.notNull(playerUUID, "Argument \"playerUUID\" can't be null");
        Validate.notNull(homeName, "Argument \"homeName\" can't be null");
        Validate.notNull(location, "Argument \"location\" can't be null");

        KixPlayer kixPlayer = KixPlayer.getPlayer(playerUUID);

        CompletableFuture<Boolean> computeChanges = CompletableFuture.supplyAsync(() -> {
            kixPlayer.lock();
            KixPlayerData playerData = kixPlayer.getData();
            int maxHomes = getMaxHomes(playerUUID);
            Map<String, Location> homes = playerData.getHomes();
            //no need to check number of homes when setting the "default" home or if an existing home is edited
            if(homeName == "default" || homes.containsKey(homeName))  {
                homes.put(homeName, location);
                playerData.setHomes(homes);
                return true;
            }
            if(homes.size() + 1 > maxHomes) return false;
            homes.put(homeName, location);
            playerData.setHomes(homes);
            return true;
        });

        CompletableFuture updateDB = computeChanges.thenApply((executedCorrectly) -> {
            if(!executedCorrectly) return false;
            kixPlayer.updateData();
            kixPlayer.unlock();
            return true;
        });

        return updateDB;

    }

    @NotNull
    private static int getMaxHomes(@NotNull String playerUUID) {
        Validate.notNull(playerUUID, "Argument \"playerUUID\" can't be null");
        LuckPerms luckPerms = KixUtilities.getLuckPerms();
        User user = luckPerms.getUserManager().getUser(playerUUID);
        if(user == null) return 0;
        Optional<QueryOptions> options = luckPerms.getContextManager().getQueryOptions(user);
        if(!options.isPresent()) return 0;
        CachedMetaData metaData = user.getCachedData().getMetaData(options.get());
        String maxHomes = metaData.getMetaValue("max-homes");
        if(maxHomes == null) return 0;
        try {
            return Integer.parseInt(maxHomes);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return 0;
        }
    }

}
