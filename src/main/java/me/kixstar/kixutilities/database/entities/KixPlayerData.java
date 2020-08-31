package me.kixstar.kixutilities.database.entities;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/*@Entity
public class Article {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String articleId;

    private String articleTitle;

    @ManyToOne
    private Author author;

    // constructors, getters and setters...
}
*/
@Entity
public class KixPlayerData {

    @Id
    private String playerUUID;

    @Nullable
    private String nickname;

    //We can ignore nullable warnings because FetchType.EAGER makes sure it is initialized.
    @SuppressWarnings("NullableProblems")
    @NotNull
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch= FetchType.EAGER, mappedBy = "id.ownerUUID")
    private Set<HomeData> homes;

    private int balance;

    //UNIX epoch time in seconds when the player first joined the server
    @Column(name = "first_logged_in")
    private Long firstLoggedIn;

    //UNIX epoch time in seconds when the player last joined the server
    @Column(name = "last_logged_in")
    private Long lastLoggedIn;

    //UNIX epoch time in seconds when the player last left the server
    @Column(name = "last_logged_out")
    private Long lastLoggedOut;

    private KixPlayerData() {}

    public KixPlayerData(
            @NotNull String playerUUID
    ) {
        this.playerUUID = playerUUID;
    }

    @Nullable
    public String getNickname() {
        return this.nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    @NotNull
    public List<HomeData> getHomes() {
        if(this.homes == null) return new ArrayList<>();
        return new ArrayList<>(this.homes);
    }

    public void setHomes(@Nullable List<HomeData> homes) {
        if(homes != null) {
            this.homes.clear();
            this.homes.addAll(homes);
        } else {
            this.homes = null;
        }
    }

    public int getBalance() {
        return this.balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }
}
