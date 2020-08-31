package me.kixstar.kixutilities.database.abstraction.player;

import com.google.common.base.Preconditions;
import me.kixstar.kixutilities.KixUtilities;
import me.kixstar.kixutilities.database.abstraction.HomeNotExistException;
import me.kixstar.kixutilities.database.abstraction.HomeSlotsMaxedException;
import me.kixstar.kixutilities.database.entities.HomeData;
import me.kixstar.kixutilities.database.entities.Location;
import me.kixstar.kixutilities.database.Database;
import me.kixstar.kixutilities.database.entities.KixPlayerData;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.model.user.User;
import net.luckperms.api.query.QueryOptions;
import org.hibernate.LockMode;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jetbrains.annotations.NotNull;

import javax.persistence.LockModeType;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

//todo: implement database storage
public class KixPlayer {

    @NotNull
    public static KixPlayer get(@NotNull String playerUUID) {
        //syntactic sugar
        Preconditions.checkNotNull(playerUUID, "Argument \"playerUUID\" can't be null");
        return new KixPlayer(playerUUID);
    }

    /** Checks if the provided string would be a valid Minecraft username
     *  this is also used for home and warp names to disallow nonsensical names
     *
     *  Minecraft username info: https://help.mojang.com/customer/en/portal/articles/928638-minecraft-usernames
     *      -Username can contain all letters of the English Alphabet
     *      -Username can contain all digits
     *      -Username can contain underscores
     *      -Username mustn't be shorter than 3 characters
     *      -Username mustn't be longer than 16 characters
     *
     * @param string string to check
     * @return true if if string fulfils the criteria for a valid username
     */
    public static boolean validUsername(String string) {
        String usernameRegex = "^[a-zA-Z0-9_]{3,16}$";
        return string.matches(usernameRegex);
    }

    @NotNull
    private String playerUUID;

    private KixPlayer(@NotNull String playerUUID) {
        Preconditions.checkNotNull(playerUUID, "Argument \"playerUUID\" can't be null");
        this.playerUUID = playerUUID;
        this.initialData();
    }

    /**
     * Writes initial data to the database if needed
     * This is used to bypass "attempted to lock null" exceptions
     */
    private void initialData() {
        Session session = Database.getNewSession();
        Transaction transaction = session.beginTransaction();

        try{
            KixPlayerData playerData = session.find(KixPlayerData.class, this.playerUUID);
            if(playerData == null) session.merge(new KixPlayerData(this.playerUUID));
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            throw e;
        } finally {
            session.close();
        }

    }

    /**
     * Completes with the database data of this player
     *
     * @return CompletableFuture which completes with the data of the player.
     */
    @NotNull
    public CompletableFuture<KixPlayerData> getData() {
        CompletableFuture<KixPlayerData> getPlayerFuture = CompletableFuture.supplyAsync(() -> {

            Session session = Database.getNewSession();
            Transaction transaction = session.beginTransaction();
            try {
                KixPlayerData playerData = session.find(KixPlayerData.class, this.playerUUID, LockModeType.PESSIMISTIC_READ);
                //explicitly unlock
                session.lock(playerData, LockMode.NONE);
                transaction.commit();
                return playerData;

            } catch (Exception e) {
                transaction.rollback();
                throw e;
            } finally {
                session.close();
            }
        });

        return getPlayerFuture;

    }

    //todo: write javadoc
    /** NOTE: permissions are not checked by this method, instead they are checked inside the command listeners
     *  However, the maximum number of houses of the player is checked here.
     */
    public CompletableFuture<Void> setHome(
            @NotNull String homeName,
            @NotNull Location location
    ) {
        Preconditions.checkNotNull(homeName, "Argument \"homeName\" can't be null");
        Preconditions.checkNotNull(location, "Argument \"location\" can't be null");

        CompletableFuture<Void> setHomeFuture = CompletableFuture.runAsync(() -> {
            Session session = Database.getNewSession();
            Transaction transaction = session.beginTransaction();
            try {
                KixPlayerData playerData = session.find(KixPlayerData.class, this.playerUUID, LockModeType.PESSIMISTIC_WRITE);
                //explicitly unlock
                session.lock(playerData, LockMode.NONE);
                int maxHomes = this.getMaxHomes();
                List<HomeData> homes = playerData.getHomes();

                for (HomeData home : homes) {
                    if (!home.getName().equals(homeName)) continue;
                    //replace the old location with new one
                    home.setLocation(location);

                    //update the local cache and then trigger an update to the database
                    playerData.setHomes(homes);
                    session.merge(playerData);
                    transaction.commit();
                    return;
                }
                //todo: remove before commit
                int size = homes.size();
                System.out.println(size);
                if(size + 1 > maxHomes) throw new HomeSlotsMaxedException(this.playerUUID, homeName);

                HomeData homeData = new HomeData(this.playerUUID, homeName, location);

                homes.add(homeData);
                //update the local cache and then trigger an update to the database
                playerData.setHomes(homes);
                session.merge(playerData);
                transaction.commit();
            } catch (Exception e) {
                transaction.rollback();
                throw e;
            } finally {
                session.close();
            }
        });

        return setHomeFuture;

    }

    //todo: write javadoc
    //todo: test this
    @NotNull
    public CompletableFuture<Void> deleteHome(
            @NotNull String homeName
    ) {
        Preconditions.checkNotNull(homeName, "Argument \"homeName\" can't be null");
        //todo: simplify by directly deleting the home using the HomeID if possible
        CompletableFuture<Void> deleteHomeFuture = CompletableFuture.runAsync(() -> {
            Session session = Database.getNewSession();
            Transaction transaction = session.beginTransaction();
            try {
                KixPlayerData playerData = session.find(KixPlayerData.class, this.playerUUID, LockModeType.PESSIMISTIC_WRITE);
                //explicitly unlock
                session.lock(playerData, LockMode.NONE);
                List<HomeData> homes = playerData.getHomes();
                ListIterator<HomeData> iterator = homes.listIterator();

                while(iterator.hasNext()) {
                    HomeData home = iterator.next();
                    if(!home.getName().equals(homeName)) continue;
                    //remove the home
                    iterator.remove();
                    //update the local cache and then trigger an update to the database
                    playerData.setHomes(homes);
                    session.merge(playerData);
                    transaction.commit();
                    return;
                }
                transaction.commit();
                //runs only if home couldn't be found
                throw new HomeNotExistException(this.playerUUID, homeName);

            } catch (Exception e) {
                transaction.rollback();
                throw e;
            } finally {
                session.close();
            }


        });

        return deleteHomeFuture;
    }

    /**
     * Completes with the Location of the Player's home that is named homeName
     * Completes with null if the Player doesn't have a home named homeName.
     *
     * @param homeName name of the home
     * @return CompletableFuture which completes with the Location of the home or null (read desc.)
     */
    @NotNull
    public CompletableFuture<Location> getHomeLocation(
            @NotNull String homeName
    ) {
        Preconditions.checkNotNull(homeName, "Argument \"homeName\" can't be null");
        //todo: simplify by directly accessing the home using the HomeID
        CompletableFuture<Location> getHomeFuture = CompletableFuture.supplyAsync(() -> {

            Session session = Database.getNewSession();
            Transaction transaction = session.beginTransaction();
            try {
                KixPlayerData playerData = session.find(KixPlayerData.class, this.playerUUID, LockModeType.PESSIMISTIC_READ);
                //explicitly unlock
                session.lock(playerData, LockMode.NONE);
                transaction.commit();
                List<HomeData> homes = playerData.getHomes();

                for (HomeData home : homes) {
                    if (home.getName().equals(homeName)) return home.getLocation();
                }
                //return null if home wasn't found
                return null;

            } catch (Exception e) {
                transaction.rollback();
                throw e;
            } finally {
                session.close();
            }
        });

        return getHomeFuture;

    }

    /**
     * Returns A map of home names and their locations
     * note: home "default" is the home that gets set the player runs /sethome without arguments
     * the Location of the "default" home can be null, this means that it hasn't been set yet.
     *
     * @return CompletableFuture
     */
    @NotNull
    public CompletableFuture<List<HomeData>> getHomes() {

        CompletableFuture<List<HomeData>> getHomesFuture = CompletableFuture.supplyAsync(() -> {

            Session session = Database.getNewSession();
            Transaction transaction = session.beginTransaction();
            try {
                KixPlayerData playerData = session.find(KixPlayerData.class, this.playerUUID, LockModeType.PESSIMISTIC_READ);
                //explicitly unlock
                session.lock(playerData, LockMode.NONE);

                transaction.commit();
                return playerData.getHomes();
            } catch (Exception e) {
                transaction.rollback();
                throw e;
            } finally {
                session.close();
            }
        });

        return getHomesFuture;

    }

    /**
     * Gets the maximum number of homes that this player can own
     * This is read from LuckPerms metadata, the key is "max-homes"
     * @return maximum number of homes
     */
    public int getMaxHomes() {
        return 5;
        /*
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
        */

    }
}
