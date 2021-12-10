package me.dessie.dessielib.resourcepack;

import me.dessie.dessielib.packeteer.PacketListener;
import me.dessie.dessielib.packeteer.Packeteer;
import me.dessie.dessielib.resourcepack.assets.BlockStateAsset;
import me.dessie.dessielib.resourcepack.assets.SoundAsset;
import me.dessie.dessielib.resourcepack.listeners.BlockListener;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ResourcePack implements PacketListener {

    private static JavaPlugin plugin;
    private static boolean registered = false;

    private static final List<ResourcePack> resourcePacks = new ArrayList<>();

    private final File zipped;
    private final ResourcePackBuilder builder;
    private final NamespacedKey key;

    public ResourcePack(File zipped, NamespacedKey key, ResourcePackBuilder builder) {
        this.zipped = zipped;
        this.builder = builder;
        this.key = key;

        //Don't allow duplicate keys, the new one should overwrite.
        resourcePacks.removeIf(pack -> pack.getKey() == key);
        resourcePacks.add(this);

        //Register the Pack Listener, if needed.
        if(this.getBuilder().getAssetsOf(BlockStateAsset.class).size() > 0) {
            Packeteer packeteer = Packeteer.register(getPlugin());
            BlockListener listener = new BlockListener(getPlugin(), this);

            getPlugin().getServer().getPluginManager().registerEvents(listener, getPlugin());
            packeteer.addListener(listener);
        }
    }

    /**
     * Plays a SoundAsset to a Player.
     * @param player The Player to play the sound to
     * @param asset The SoundAsset to play
     * @param volume The volume of the sound
     * @param pitch The pitch of the sound
     */
    public void playSound(Player player, SoundAsset asset, float volume, float pitch) {
        player.playSound(player.getLocation(), this.getBuilder().getNamespace() + ":" + asset.getPath(), asset.getCategory(), volume, pitch);
    }

    /**
     * Stops a Sound from playing.
     * @param player The player
     * @param asset The SoundAsset to stop playing
     */
    public void stopSound(Player player, SoundAsset asset) {
        player.stopSound(this.getBuilder().getNamespace() + ":" + asset.getPath());
    }

    /**
     * @param name The path and name of the sound.
     *             Ex:
     *             entity.enderman.scream
     *             block.sand.fall
     * @return The SoundAsset if found
     */
    public SoundAsset getSoundAssetByName(String name) {
        return this.getBuilder().getAssetsOf(SoundAsset.class).stream().filter(asset -> asset.getPath().equalsIgnoreCase(name))
                .findAny().orElse(null);
    }

    public static ResourcePack getResourcePack(Plugin plugin) {
        NamespacedKey pluginKey = new NamespacedKey(plugin, plugin.getName().toLowerCase(Locale.ROOT));

        return resourcePacks.stream().filter(resourcePack -> resourcePack.getKey().equals(pluginKey)).findAny().orElse(null);
    }

    public File getResourcePack() {
        return zipped;
    }
    public ResourcePackBuilder getBuilder() {return builder;}
    public NamespacedKey getKey() {return key;}
    public static List<ResourcePack> getResourcePacks() {return resourcePacks;}

    public static JavaPlugin getPlugin() {
        return plugin;
    }

    /**
     * Register the API to use your plugin.
     * @param yourPlugin Your plugin instance.
     */
    public static void register(JavaPlugin yourPlugin) {
        if(isRegistered()) {
            throw new IllegalStateException("ResourcePack already registered to " + getPlugin().getName());
        }
        plugin = yourPlugin;
        registered = true;
    }

    public static boolean isRegistered() {
        return registered;
    }
}
