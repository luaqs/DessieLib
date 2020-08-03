package me.dessie.DessieLib.scoreboardapi;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Team;

public class ScoreboardListener implements Listener {
    static void register(JavaPlugin plugin) {
        ScoreboardAPI.plugin = plugin;

        //Register the events.
        new ScoreboardListener().createListeners();
    }

    private void createListeners() {
        ScoreboardAPI.plugin.getServer().getPluginManager().registerEvents(this, ScoreboardAPI.plugin);
    }

    @EventHandler
    public static void onLeave(PlayerQuitEvent event) {
        ScoreboardAPI.boards.remove(event.getPlayer());

        //Remove that player from all the entries.
        for(Player player : ScoreboardAPI.boards.keySet()) {
            ScoreboardAPI board = ScoreboardAPI.getBoard(player);
            for(Team team : board.getScoreboard().getTeams()) {
                team.removeEntry(event.getPlayer().getName());
            }
        }

    }




}
