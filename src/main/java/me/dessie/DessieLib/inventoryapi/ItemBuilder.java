package me.dessie.DessieLib.inventoryapi;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;

public class ItemBuilder {

    private InventoryBuilder builder;
    private ItemStack item;
    private Runnable clickEvent = null;
    private List<ItemStack> cycle = new ArrayList<>();

    //Index of the cycle, where 0 is the item the builder was created with.
    private int cycleIndex = 0;

    private boolean cancel;
    private boolean glowing;
    int slot;
    ItemStack heldItem;
    ClickType clickType;

    public ItemBuilder(ItemStack item) {
        this.item = item;
        this.glowing = false;
    }

    //Makes a copy
    public ItemBuilder(ItemBuilder itemBuilder) {
        this.builder = itemBuilder.getBuilder();
        this.item = itemBuilder.getItem();
        this.cancel = itemBuilder.isCancel();
        this.clickEvent = itemBuilder.getClickEvent();
        this.clickType = itemBuilder.getClick();
        this.slot = itemBuilder.getSlot();
        this.glowing = itemBuilder.isGlowing();
        this.heldItem = itemBuilder.getHeldItem();
        this.cycle = itemBuilder.getCycle();
    }

    public List<ItemStack> getCycle() { return cycle; }
    public ItemStack getItem() { return this.item; }
    public InventoryBuilder getBuilder() { return this.builder; }
    public int getSlot() { return this.slot; }
    public Runnable getClickEvent() { return this.clickEvent; }
    public boolean isCancel() { return this.cancel; }
    public ClickType getClick() { return this.clickType; }
    public boolean isGlowing() { return this.glowing; }
    public ItemStack getHeldItem() {return this.heldItem; }
    public String getName() { return this.getItem().getItemMeta().hasDisplayName() ? this.getItem().getItemMeta().getDisplayName() : this.getItem().getType().toString(); }

    //Set a new ItemStack for ItemBuilder.
    public ItemBuilder setMaterial(Material type) {
        this.item.setType(type);
        this.getBuilder().update();
        return this;
    }

    //Check if two ItemBuilders have the same properties.
    public boolean isSimilar(ItemBuilder compare) {
        if(compare == null) return false;

        if (this.getItem().isSimilar(compare.getItem()) && this.isGlowing() == compare.isGlowing()) {
            if (this.isCancel() == compare.isCancel() && this.getClickEvent() == compare.getClickEvent()) {
                if(this.getCycle() == compare.getCycle()) {
                    return true;
                }
            }
        }

        return false;
    }

    public ItemBuilder setCursorOnClick(ItemStack item) {
        getBuilder().getPlayer().getOpenInventory().setCursor(item);
        return this;
    }

    //Set a new ItemStack for ItemBuilder.
    public ItemBuilder setItem(ItemStack item) {
        this.item = item.clone();
        updateBuilder();
        return this;
    }

    //Set ItemStack amount.
    public ItemBuilder setAmount(int amount) {
        return setAmount(amount, true);
    }

    //Set ItemStack amount.
    public ItemBuilder setAmount(int amount, boolean update) {
        this.item.setAmount(amount);

        if (update) {
            updateBuilder();
        }

        return this;
    }

    public int getAmount() {
        return this.item.getAmount();
    }

    //Set ItemStack name.
    public ItemBuilder setName(String name) {
        ItemMeta meta = this.item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        this.item.setItemMeta(meta);
        updateBuilder();
        return this;
    }

    //Set ItemStack lore.
    public ItemBuilder setLore(List<String> lore) {
        ItemMeta meta = this.item.getItemMeta();

        List<String> newLore = new ArrayList<>();
        for(String s : lore) {
            newLore.add(InventoryAPI.color(s));
        }
        meta.setLore(newLore);

        this.item.setItemMeta(meta);
        updateBuilder();
        return this;
    }

    //Internal method
    //Sets the builder for the item.
    ItemBuilder setBuilder(InventoryBuilder builder) {
        this.builder = builder;
        return this;
    }

    //Code to run when item is clicked.
    public ItemBuilder onClick(Runnable runnable) {
        this.clickEvent = runnable;
        return this;
    }

    //Cancel the event when clicked
    public ItemBuilder cancel() {
        this.cancel = true;
        return this;
    }

    /*
    Cycles through each item in the list when this ItemBuilder is clicked.
     */
    public ItemBuilder cyclesWith(ItemStack... items) {
        List<ItemStack> cycle = new ArrayList<>();
        cycle.add(this.getItem());
        cycle.addAll(Arrays.asList(items));
        this.cycle = cycle;
        return this;
    }

    public ItemBuilder toggleGlow() {
        if(!this.isGlowing()) {
            this.item.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
            ItemMeta meta = this.item.getItemMeta();
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            this.item.setItemMeta(meta);
            this.glowing = true;
        } else {
            this.item.removeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL);
            this.glowing = false;
        }

        if(this.getBuilder() != null) {
            this.getBuilder().update();
        }

        return this;
    }

    public static ItemStack buildSkull(OfflinePlayer player, int amount, String name, String... lore) {
        ItemStack item = buildItem(Material.PLAYER_HEAD, amount, name, lore);

        SkullMeta meta = (SkullMeta) item.getItemMeta();
        meta.setOwningPlayer(player);
        item.setItemMeta(meta);

        return item;
    }

    //Build an ItemStack
    public static ItemStack buildItem(Material material, int amount, @Nullable String name, @Nullable List<String> lore) {
        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();

        if(name != null) {
            meta.setDisplayName(InventoryAPI.color(name));
        }

        if(lore != null) {
            List<String> newLore = new ArrayList<>();
            for(String s : lore) {
                newLore.add(InventoryAPI.color(s));
            }
            meta.setLore(newLore);
        }

        item.setItemMeta(meta);
        return item;
    }

    //Build an ItemStack
    public static ItemStack buildItem(Material material, int amount, @Nullable String name, String... lore) {
        return buildItem(material, amount, name, Arrays.asList(lore));
    }

    //Internal method.
    //Executes the onClick runnable
    void executeClick() {
        if(this.clickEvent != null) {
            this.clickEvent.run();
        }
    }

    //Internal method.
    //Updates builder if there is one.
    private void updateBuilder() {
        if(this.getBuilder() != null) {
            this.getBuilder().update();
        }
    }

    void swap() {
        if(this.getCycle().isEmpty()) return;

        InventoryBuilder builder = this.getBuilder();

        this.cycleIndex = this.cycleIndex + 1 >= this.getCycle().size() ? 0 : cycleIndex + 1;
        this.setItem(this.getCycle().get(this.cycleIndex));

        builder.setItem(this, this.getSlot());
    }
}
