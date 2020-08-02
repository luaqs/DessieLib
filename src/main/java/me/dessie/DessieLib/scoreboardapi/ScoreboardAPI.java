package me.dessie.DessieLib.scoreboardapi;

import me.dessie.DessieLib.Colors;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScoreboardAPI {

    static Map<Player, ScoreboardAPI> boards = new HashMap<>();
    static JavaPlugin plugin;

    Scoreboard scoreboard;
    Player player;
    Objective objective;

    public ScoreboardAPI(Player player, String name) {
        if(plugin == null) {
            throw new NullPointerException("You need to register your plugin before creating a new ScoreboardAPI!");
        }

        this.player = player;
        this.scoreboard = plugin.getServer().getScoreboardManager().getNewScoreboard();

        //Setup the objectives.
        this.objective = this.scoreboard.registerNewObjective("Sidebar", "dummy", ChatColor.stripColor(Colors.color(name)));
        this.objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        this.objective.setDisplayName(Colors.color(name));

        //Set the scoreboard for the player.
        this.player.setScoreboard(this.scoreboard);
        boards.put(player, this);
    }

    public Scoreboard getScoreboard() { return this.scoreboard; }
    public Player getPlayer() { return this.player; }

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
        this.objective.getScore(randomColor).setScore(score);

        return this;
    }

    public ScoreboardAPI animateTitle(List<String> animation, int delay) {
        int multiplier = 1;
        for(String s : animation) {
            Bukkit.getScheduler().runTaskTimer(plugin, () -> setTitle(s), 0, delay * multiplier);
            multiplier++;
        }

        return this;
    }

    public ScoreboardAPI animateScore(List<String> animation, int delay, int score) {
        int multiplier = 1;
        for(String s : animation) {
            Bukkit.getScheduler().runTaskTimer(plugin, () -> setLine(s, score), 0, delay * multiplier);
            multiplier++;
        }
        return this;
    }

    public ScoreboardAPI setTitle(String name) {
        this.objective.setDisplayName(Colors.color(name));
        return this;
    }

    public ScoreboardAPI clear() {
        this.scoreboard.getEntries().forEach(this.scoreboard::resetScores);
        return this;
    }

    public static ScoreboardAPI getBoard(Player player) {
        return boards.get(player);
    }

    public static void register(JavaPlugin yourPlugin) {
        plugin = yourPlugin;
    }
}
