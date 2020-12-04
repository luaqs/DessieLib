package me.dessie.DessieLib.scoreboardapi;

import me.dessie.DessieLib.Colors;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import javax.annotation.Nullable;
import java.util.*;

public class ScoreboardAPI implements Listener {

    static Map<Player, ScoreboardAPI> boards = new HashMap<>();
    static JavaPlugin plugin;

    Scoreboard scoreboard;
    Player player;
    Objective sidebarObjective;
    Objective tablistObjective;

    public ScoreboardAPI(Player player, String name) {
        if(plugin == null) {
            throw new NullPointerException("You need to register your plugin before creating a new ScoreboardAPI!");
        } else {

            if(!Bukkit.isPrimaryThread()) {
                throw new IllegalStateException("Unable to register a new Scoreboard in an async thread!");
            }

            this.player = player;
            this.scoreboard = plugin.getServer().getScoreboardManager().getNewScoreboard();

            //Setup the objectives.
            this.sidebarObjective = this.scoreboard.registerNewObjective("Sidebar", "dummy", ChatColor.stripColor(Colors.color(name)));
            this.sidebarObjective.setDisplaySlot(DisplaySlot.SIDEBAR);

            this.tablistObjective = this.scoreboard.registerNewObjective("Tablist", "dummy", ChatColor.stripColor(Colors.color(name)));
            this.tablistObjective.setDisplaySlot(DisplaySlot.PLAYER_LIST);

            //Set the scoreboard for the player.
            this.player.setScoreboard(this.scoreboard);

            boards.put(player, this);
        }
    }

    public Scoreboard getScoreboard() { return this.scoreboard; }
    public Player getPlayer() { return this.player; }

    public ScoreboardAPI setTablistCriteria(String criteria) {
        this.tablistObjective.unregister();

        this.tablistObjective = this.scoreboard.registerNewObjective("Tablist", criteria, this.sidebarObjective.getDisplayName());
        this.tablistObjective.setDisplaySlot(DisplaySlot.PLAYER_LIST);
        return this;
    }

    public ScoreboardAPI setTabHeader(String text) {
        this.player.setPlayerListHeader(Colors.color(text));
        return this;
    }

    public ScoreboardAPI setTabFooter(String text) {
        this.player.setPlayerListFooter(Colors.color(text));
        return this;
    }

    //The higher the weight, the lower they will be on the tablist
    public ScoreboardAPI addTablistTeam(String teamName, @Nullable org.bukkit.ChatColor color, @Nullable String prefix, int weight) {
        if(weight >= 100) {
            throw new IllegalArgumentException("Weight cannot be larger than 100!");
        }
        if(teamName == null) {
            throw new NullPointerException("Team can't be null!");
        }

        StringBuilder weightedName = new StringBuilder();

        if(weight < 10) {
            weightedName.append("0" + weight);
        } else {
            weightedName.append(weight);
        }

        Team team = this.scoreboard.registerNewTeam(weightedName.append(teamName).toString());

        if(prefix != null && !prefix.equalsIgnoreCase("")) { team.setPrefix(Colors.color(prefix)); }
        if(color != null) { team.setColor(color); }

        //When we create a team, we want to add all the entries
        //That everyone else has. This can be done by getting a
        //random person's board that has this team, and adding all their
        //entries.
        if(boards.size() > 1) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                ScoreboardAPI randomBoard = randomBoard();

                //Scoreboard that we got cannot be the same as this one.
                while (randomBoard.getPlayer() == this.player || !randomBoard.hasTeam(team.getName())) {
                    randomBoard = randomBoard();
                }

                for (String entry : randomBoard.getScoreboard().getTeam(team.getName()).getEntries()) {
                    team.addEntry(entry);
                }
            });
        }

        return this;
    }

    public ScoreboardAPI setPlayerTeam(String teamName) {
        String team;
        Optional<String> opTeam = this.scoreboard.getTeams().stream()
                .map(Team::getName)
                .filter(team1 -> team1.contains(teamName)).findAny();

        if(opTeam.isPresent()) {
            team = opTeam.get();
        } else {
            throw new IllegalArgumentException("Could not find a team that contains the string '" + teamName + "'");
        }

        this.scoreboard.getTeam(team).addEntry(this.player.getName());

        //Update their own health.
        this.tablistObjective.getScore(this.player.getName()).setScore((int) this.player.getHealth());

        //Update everyone else's boards with the new entry.
        for(Player player : boards.keySet()) {
            if(player == this.player) continue;
            ScoreboardAPI board = getBoard(player);

            //Add the new entry if their scoreboard has that team.
            if(board.hasTeam(team)) {
                board.getTeam(team).addEntry(this.player.getName());
            }

            //If the criteria is health, set everyone's current health as the score for the player
            //who joined the team.
            if(this.tablistObjective.getCriteria().equalsIgnoreCase("health")) {
                this.tablistObjective.getScore(board.getPlayer().getName()).setScore((int) board.getPlayer().getHealth());
            }
        }

        return this;
    }

    public ScoreboardAPI setLine(String text, int score) {
        Team team;
        String scoreAsString = String.valueOf(score);

        if(this.scoreboard.getTeam(scoreAsString) != null) {
            team = this.scoreboard.getTeam(scoreAsString);
        } else {
            team = this.scoreboard.registerNewTeam(scoreAsString);
        }

        String randomColor = "" + ChatColor.getByChar(ChatColor.ALL_CODES.replaceAll("([A-Z])", "").split("k")[0].charAt(score));

        //Add the entry so we can display it.
        //We use colors because they don't show up.
        team.addEntry(randomColor);

        //Set the prefix (so the entry has the display text)
        team.setPrefix(Colors.color((text)));

        //Add to the objective.
        this.sidebarObjective.getScore(randomColor).setScore(score);

        return this;
    }

    public boolean hasTeam(String team) {
        return this.scoreboard.getTeams().stream().map(Team::getName).anyMatch(name -> name.equalsIgnoreCase(team));
    }

    public Team getTeam(String team) {
        return this.scoreboard.getTeam(team);
    }

    public ScoreboardAPI animateTitle(List<String> animation, int delay) {
        int multiplier = 1;

        for(String s : animation) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> setTitle(s), delay * multiplier);
            multiplier++;
        }

        return this;
    }

    public ScoreboardAPI animateScore(List<String> animation, int delay, int score) {
        int multiplier = 1;
        for(String s : animation) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> setLine(s, score), delay * multiplier);
            multiplier++;
        }
        return this;
    }

    public ScoreboardAPI setTitle(String name) {
        this.sidebarObjective.setDisplayName(Colors.color(name));
        return this;
    }

    public ScoreboardAPI clear() {
        this.scoreboard.getEntries().forEach(this.scoreboard::resetScores);
        return this;
    }

    public static ScoreboardAPI randomBoard() {
        return (ScoreboardAPI) boards.values().toArray()[new Random().nextInt(boards.size())];
    }

    public static ScoreboardAPI getBoard(Player player) {
        return boards.get(player);
    }

    public static void register(JavaPlugin yourPlugin) {
        ScoreboardListener.register(yourPlugin);
    }
}
