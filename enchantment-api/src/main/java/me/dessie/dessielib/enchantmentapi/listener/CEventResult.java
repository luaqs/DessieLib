package me.dessie.dessielib.enchantmentapi.listener;

import me.dessie.dessielib.enchantmentapi.CEnchantment;
import org.bukkit.inventory.ItemStack;

public class CEventResult {

    private CEnchantment enchantment;
    private ItemStack item;
    private int level;

    public CEventResult(CEnchantment enchantment, ItemStack item, int level) {
        this.enchantment = enchantment;
        this.item = item;
        this.level = level;
    }

    public CEnchantment getEnchantment() {
        return enchantment;
    }

    public ItemStack getItem() {
        return item;
    }

    public int getLevel() {
        return level;
    }
}
