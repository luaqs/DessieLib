package me.dessie.dessielib;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;

@SuppressWarnings("Duplicates")
public class ConfigManager implements Listener{

    public void loadConfigs(Plugin plugin, File file, FileConfiguration cfile, CommandSender sender) {
        String prefix = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("prefix"));
        String fileName = file.getName();

        if(!file.exists()) {
            plugin.saveResource(fileName, false);
            if(sender != null) {
                sender.sendMessage( prefix + " " + ChatColor.RED + fileName + " does not exist. Creating it for you.");
            }
        } else {
            try {
                cfile.load(file);
                if(sender != null) {
                    sender.sendMessage(prefix + " " + ChatColor.GREEN + fileName + " successfully reloaded!");
                }
            } catch (IOException | InvalidConfigurationException e) {
                e.printStackTrace();
                if(sender != null) {
                    sender.sendMessage(prefix + " " + ChatColor.RED + fileName + " could not be reloaded! Check console for errors.");
                }
            }
        }
    }

    public void reloadConfig(Plugin plugin, CommandSender sender, File file, FileConfiguration cfile) {
        loadConfigs(plugin, file, cfile, sender);
    }

    public void createFiles(Plugin plugin, File file, FileConfiguration config) {
        if (!file.exists()) {
            plugin.saveResource(file.getName(), false);
            System.out.println(file.getName() + " file not found. Creating one for you.");
        }
        try {
            config.load(file);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
            System.out.println("Unable to load " + file.getName() + "file!");
        }
    }

    public FileConfiguration getConfig(FileConfiguration config) {
        return config;
    }

    public void saveConfig(FileConfiguration cfile, File file) {
        try {
            cfile.save(file);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}
