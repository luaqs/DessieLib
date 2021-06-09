package me.dessie.dessielib.events.slot;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class SlotUpdateEvent extends PlayerEvent implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private boolean cancelled;

    private int slot;
    private ItemStack newItem;
    private ItemStack oldItem;
    private UpdateType updateType;
    private Inventory inventory;


    //Used for InventoryClickEvents.
    //InventoryClickEvent always triggers a tick later, so sometimes items are returning null unexpectedly.
    //These can be used to access these "null" items.
    private Inventory newInventory;
    private Inventory oldInventory;

    public SlotUpdateEvent(Player who, Inventory inventory, int slot, ItemStack newItem, ItemStack oldItem, UpdateType updateType, Inventory newInv, Inventory oldInv) {
        super(who);

        this.inventory = inventory;
        this.slot = slot;
        this.newItem = newItem;
        this.oldItem = oldItem;
        this.updateType = updateType;
        this.newInventory = newInv;
        this.oldInventory = oldInv;
    }

    public SlotUpdateEvent(Player who, Inventory inventory, int slot, ItemStack newItem, ItemStack oldItem, UpdateType updateType) {
        this(who, inventory, slot, newItem, oldItem, updateType, null, null);
    }

    public static SlotUpdateEvent attemptFire(Player who, Inventory inventory, int slot, ItemStack newItem, ItemStack oldItem, UpdateType updateType, Inventory newInv, Inventory oldInv) {
        SlotUpdateEvent event = new SlotUpdateEvent(who, inventory, slot, newItem, oldItem, updateType, newInv, oldInv);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static SlotUpdateEvent attemptFire(Player who, Inventory inventory, int slot, ItemStack newItem, ItemStack oldItem, UpdateType updateType) {
        SlotUpdateEvent event = new SlotUpdateEvent(who, inventory, slot, newItem, oldItem, updateType);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public int getSlot() {
        return slot;
    }

    public ItemStack getNewItem() { return newItem; }
    public ItemStack getOldItem() { return oldItem; }
    public UpdateType getUpdateType() { return updateType; }
    public Inventory getInventory() { return inventory; }

    public Inventory getNewInventory() { return newInventory; }
    public Inventory getOldInventory() { return oldInventory; }

    public boolean isPlayerInventory() { return this.getInventory().getType() == InventoryType.PLAYER; }
    public boolean isOffhand() { return this.getSlot() == 40; }
    public boolean isBoots() { return this.getSlot() == 36; }
    public boolean isLeggings() { return this.getSlot() == 37; }
    public boolean isChestplate() { return this.getSlot() == 38; }
    public boolean isHelmet() { return this.getSlot() == 39; }
    public boolean isArmor() { return isBoots() || isLeggings() || isChestplate() || isHelmet(); }
    public boolean isInMainHand() { return player.getInventory().getHeldItemSlot() == this.getSlot(); }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        this.cancelled = b;
    }
}
