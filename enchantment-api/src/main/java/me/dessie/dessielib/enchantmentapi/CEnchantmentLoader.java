package me.dessie.dessielib.enchantmentapi;

import me.dessie.dessielib.enchantmentapi.listener.CEnchantmentListener;
import me.dessie.dessielib.enchantmentapi.properties.PropertyListener;
import me.dessie.dessielib.core.events.slot.SlotEventHelper;
import org.bukkit.plugin.java.JavaPlugin;

public class CEnchantmentLoader {
    private static JavaPlugin plugin;
    private static boolean registered;

    public static void register(JavaPlugin yourPlugin) {
        if(isRegistered()) {
            throw new IllegalStateException("Cannot register EnchantmentAPI in " + yourPlugin.getName() + ". Already registered by " + getPlugin().getName());
        }

        plugin = yourPlugin;
        SlotEventHelper.register(plugin);

        plugin.getServer().getPluginManager().registerEvents(new CEnchantmentListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PropertyListener(), plugin);

        registered = true;
    }

    public static JavaPlugin getPlugin() {
        return plugin;
    }

    public static boolean isRegistered() {
        return registered;
    }
}
