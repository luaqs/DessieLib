package me.dessie.dessielib.utils;

import net.minecraft.server.v1_16_R3.SoundEffect;
import net.minecraft.server.v1_16_R3.SoundEffectType;
import net.minecraft.server.v1_16_R3.SoundEffects;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_16_R3.block.CraftBlock;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SoundUtil {
    private static Map<SoundEffect, Sound> sounds = new HashMap<>();
    private static Map<Material, SoundEffect> blockBreakSoundMap = new HashMap<>();

    public static Sound getBreakSound(Block block) {
        if(blockBreakSoundMap.containsKey(block.getType())) {
            return getSound(blockBreakSoundMap.get(block.getType()));
        }

        try {
            Field field = SoundEffectType.class.getDeclaredField("breakSound");
            field.setAccessible(true);

            SoundEffect effect = (SoundEffect) field.get(((CraftBlock) block).getNMS().getStepSound());
            blockBreakSoundMap.putIfAbsent(block.getType(), effect);
            return getSound(effect);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Sound getPlaceSound(Block block) {
        return getSound(((CraftBlock) block).getNMS().getStepSound().getPlaceSound());
    }

    public static Sound getSound(SoundEffect effect) {
        if(getSounds().containsKey(effect)) return getSounds().get(effect);

        Field[] fields = SoundEffects.class.getFields();

        Sound sound = Arrays.stream(Sound.values()).filter(sound1 -> Arrays.stream(fields).anyMatch(field -> {
            try {
                return field.getName().equalsIgnoreCase(sound1.name()) && field.get(null) == effect;
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            return false;
        })).findAny().orElse(null);

        try {
            if(sound != null) {
                sounds.put((SoundEffect) Arrays.stream(fields).filter(field -> field.getName().equalsIgnoreCase(sound.name()))
                        .findFirst().get().get(null), sound);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return sound;
    }

    public static Map<SoundEffect, Sound> getSounds() {
        return sounds;
    }
}