package me.dessie.dessielib.enchantmentapi;

import me.dessie.dessielib.enchantmentapi.listener.CEnchantmentListener;
import me.dessie.dessielib.enchantmentapi.properties.PropertyListener;
import me.dessie.dessielib.events.slot.SlotEventHelper;
import org.bukkit.plugin.java.JavaPlugin;

public class CEnchantmentLoader {
    private static JavaPlugin plugin;

    public static void register(JavaPlugin yourPlugin) {
        plugin = yourPlugin;
        SlotEventHelper.register(plugin);

        plugin.getServer().getPluginManager().registerEvents(new CEnchantmentListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PropertyListener(), plugin);
    }

    public static JavaPlugin getPlugin() {
        if(plugin == null) {
            throw new IllegalStateException("Enchantment API not registered!");
        }
        return plugin;
    }


}
