package me.dessie.dessielib.utils;

import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_17_R1.block.CraftBlock;

public class SoundUtil {

    public static Sound getBreakSound(Block block) {
        return Sound.valueOf(((CraftBlock) block).getNMS().getSoundType().getBreakSound().getLocation().getPath());
    }

    public static Sound getPlaceSound(Block block) {
        return Sound.valueOf(((CraftBlock) block).getNMS().getSoundType().getPlaceSound().getLocation().getPath());
    }
}