package me.dessie.DessieLib.compatibility;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface VersionHandler {

    ItemStack addNBT(ItemStack item, String nbt, Object value);
    Object getNBT(ItemStack item, String nbt, Object as);
    void openAnvilInventory(final Player player, ItemStack item);

}
