package me.dessie.dessielib.utils;

import net.md_5.bungee.api.ChatColor;

public class Colors {
    public static String color(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }
}
