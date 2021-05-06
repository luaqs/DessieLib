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

    private Scoreboard scoreboard;
    private Player player;
    private Objective sidebarObjective;
    private Objective tablistObjective;

    /**
     * Each player should have their own unique ScoreboardAPI reference.
     * @param player The player to create this Scoreboard for
     * @param name The display name for the objective
     */
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

    /**
     * @return The {@link Scoreboard} related to this object
     */
    public Scoreboard getScoreboard() { return this.scoreboard; }

    /**
     * @return The player this Scoreboard is assigned to
     */
    public Player getPlayer() { return this.player; }

    /**
     * @param collidable If the player can collide with entities
     */
    public void setCollidable(boolean collidable) {
        for(Team team : this.getScoreboard().getTeams()) {
            team.setOption(Team.Option.COLLISION_RULE, collidable ? Team.OptionStatus.ALWAYS : Team.OptionStatus.NEVER);
        }
    }

    /**
     * @param criteria The scoreboard tablist criteria
     * @return The ScoreboardAPI object
     */
    public ScoreboardAPI setTablistCriteria(String criteria) {
        this.tablistObjective.unregister();

        this.tablistObjective = this.scoreboard.registerNewObjective("Tablist", criteria, this.sidebarObjective.getDisplayName());
        this.tablistObjective.setDisplaySlot(DisplaySlot.PLAYER_LIST);
        return this;
    }

    /**
     * @param text The tablist header
     * @return The ScoreboardAPI object
     */
    public ScoreboardAPI setTabHeader(String text) {
        this.player.setPlayerListHeader(Colors.color(text));
        return this;
    }

    /**
     * @param text The tablist footer
     * @return The ScoreboardAPI object
     */
    public ScoreboardAPI setTabFooter(String text) {
        this.player.setPlayerListFooter(Colors.color(text));
        return this;
    }

    /**
     *
     * Creates an organized team that will be sorted by weight on the tablist.
     * The lower the weight, the higher the team will be.
     *
     * @param teamName The name of the team
     * @param color The color of the tean
     * @param prefix The prefix that appears before a player's name
     * @param weight The weight of the team
     * @return The ScoreboardAPI object
     */
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

    /**
     * @param teamName The team to set the player on.
     * @return The ScoreboardAPI object
     */
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

    /**
     * Sets a line of text on the Scoreboard
     *
     * @param text The text
     * @param score The index of the line
     * @return The ScoreboardAPI object
     */
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

    /**
     * @param team The team to request
     * @return If this scoreboard has the requested team registered
     */
    public boolean hasTeam(String team) {
        return this.scoreboard.getTeams().stream().map(Team::getName).anyMatch(name -> name.equalsIgnoreCase(team));
    }

    /**
     * @param team The team to get
     * @return The Team object from it's name
     */
    public Team getTeam(String team) {
        return this.scoreboard.getTeam(team);
    }

    /**
     * Cycles through each frame to simulate animation.
     *
     * @param animation The animation frames
     * @param delay Delay between each frame
     * @return The ScoreboardAPI object
     */
    public ScoreboardAPI animateTitle(List<String> animation, int delay) {
        int multiplier = 1;

        for(String s : animation) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> setTitle(s), delay * multiplier);
            multiplier++;
        }

        return this;
    }

    /**
     * Cycles through each frame to simulate animation.
     *
     * @param animation The animation frames
     * @param delay Delay between each frame
     * @param score The index to execute this animation on
     * @return The ScoreboardAPI object
     */
    public ScoreboardAPI animateScore(List<String> animation, int delay, int score) {
        int multiplier = 1;
        for(String s : animation) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> setLine(s, score), delay * multiplier);
            multiplier++;
        }
        return this;
    }

    /**
     * @param title The title of the Scoreboard
     * @return The ScoreboardAPI object
     */
    public ScoreboardAPI setTitle(String title) {
        this.sidebarObjective.setDisplayName(Colors.color(title));
        return this;
    }

    /**
     * Clears all lines on the Scoreboard
     * @return The ScoreboardAPI object
     */
    public ScoreboardAPI clear() {
        this.scoreboard.getEntries().forEach(this.scoreboard::resetScores);
        return this;
    }

    /**
     * @return A random registered ScoreboardAPI object
     */
    public static ScoreboardAPI randomBoard() {
        return (ScoreboardAPI) boards.values().toArray()[new Random().nextInt(boards.size())];
    }

    /**
     * @param player The player
     * @return That player's ScoreboardAPI object
     */
    public static ScoreboardAPI getBoard(Player player) {
        return boards.get(player);
    }

    /**
     * Main registration method, this must be called with your Plugin instance
     * before creating ScoreboardAPI objects.
     * @param plugin Your plugin instance
     */
    public static void register(JavaPlugin plugin) {
        ScoreboardAPI.plugin = plugin;

        //Register the events.
        new ScoreboardListener().createListeners();
    }
}
