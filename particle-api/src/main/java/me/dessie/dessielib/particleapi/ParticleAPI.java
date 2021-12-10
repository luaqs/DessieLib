package me.dessie.dessielib.particleapi;

import org.bukkit.plugin.java.JavaPlugin;

public class ParticleAPI {
    private static JavaPlugin plugin;
    private static boolean registered;

    public static void register(JavaPlugin yourPlugin) {
        if(isRegistered()) {
            throw new IllegalStateException("Cannot register ParticleAPI in " + yourPlugin.getName() + ". Already registered by " + getPlugin().getName());
        }

        plugin = yourPlugin;
        registered = true;
    }

    public static JavaPlugin getPlugin() {
        return plugin;
    }

    public static boolean isRegistered() {
        return registered;
    }
}
