package me.dessie.DessieLib;

import net.md_5.bungee.api.ChatColor;

public class Colors {

    public String color(String string) {
        string = ChatColor.translateAlternateColorCodes('&', string);
        return string;
    }



}
