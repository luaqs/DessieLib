package me.dessie.dessielib.core.events.slot;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Jukebox;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDispenseArmorEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/*
Handles MOST possible ways that an Inventory slot can be changed.
All possible Inventory Movements, Picking up items, Dropping items are all supported.
Putting on and taking off armor, breaking items, and swapping offhands are also supported.
 */
public class SlotEventHelper implements Listener {

    private static JavaPlugin plugin;
    private static boolean registered;

    //InventoryDropEvent is fired when dropping items through the inventory.
    //As of writing, there's no way to differentiate whether an item was dropped
    //through the inventory or through the drop hotkey.

    //InventoryClickEvent is fired first, so we can ignore the DropEvent
    //if the UUID is in this list.

    //Entries are removed 1 tick after they're added.
    private static final List<UUID> didInventoryDrop = new ArrayList<>();

    public static void register(JavaPlugin yourPlugin) {
        if(isRegistered()) {
            throw new IllegalStateException("Cannot register SlotUpdateEvent in " + yourPlugin.getName() + ". Already registered by " + getPlugin().getName());
        }

        plugin = yourPlugin;
        registered = true;
        Bukkit.getServer().getPluginManager().registerEvents(new SlotEventHelper(), getPlugin());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onArmorDispense(BlockDispenseArmorEvent event) {
        if(event.isCancelled()) return;
        if(event.getTargetEntity() instanceof Player) {
            doArmorUpdate((Player) event.getTargetEntity(), event.getItem());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onArmorEquip(PlayerInteractEvent event) {
        if(event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if(event.getClickedBlock() != null && isInteractable(event.getPlayer(), event.getClickedBlock())) return;
        if(event.getHand() != EquipmentSlot.HAND && event.getHand() != EquipmentSlot.OFF_HAND) return;

        ItemStack heldItem = event.getHand() == EquipmentSlot.HAND
                ? event.getPlayer().getInventory().getItemInMainHand()
                : event.getPlayer().getInventory().getItemInOffHand();

        if(isNullOrAir(heldItem)) return;

        Material type = heldItem.getType();
        //Only attempt the update if the armor is equipable through right clicking.
        //For example, pumpkins and skulls are valid helmets, but aren't equippable this way.
        if(type.name().contains("HELMET")
                || type.name().contains("CHESTPLATE")
                || type.name().contains("LEGGINGS")
                || type.name().contains("BOOTS") || type == Material.ELYTRA) {
            doArmorUpdate(event.getPlayer(), heldItem);
        }
    }

    //May not work if they have more than one of the EXACT same item, but likelihood is small.
    @EventHandler
    public void onBreak(PlayerItemBreakEvent event) {
        ItemStack broken = event.getBrokenItem();
        int slot = event.getPlayer().getInventory().first(broken);
        if(slot == -1) {
            PlayerInventory inventory = event.getPlayer().getInventory();

            if(inventory.getItemInOffHand().equals(broken)) slot = 40;
            if(inventory.getHelmet() != null && inventory.getHelmet().equals(broken)) slot = 39;
            if(inventory.getChestplate() != null && inventory.getChestplate().equals(broken)) slot = 38;
            if(inventory.getLeggings() != null && inventory.getLeggings().equals(broken)) slot = 37;
            if(inventory.getBoots() != null && inventory.getBoots().equals(broken)) slot = 36;
        }

        SlotUpdateEvent.attemptFire(event.getPlayer(), event.getPlayer().getInventory(), slot, new ItemStack(Material.AIR), event.getBrokenItem(), UpdateType.ITEM_BREAK);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDrop(PlayerDropItemEvent event) {
        if(event.isCancelled()) return;

        if(didInventoryDrop.contains(event.getPlayer().getUniqueId())) {
            didInventoryDrop.remove(event.getPlayer().getUniqueId());
            return;
        }

        int slot = event.getPlayer().getInventory().getHeldItemSlot();
        ItemStack droppedItem = event.getItemDrop().getItemStack();
        ItemStack newItem = event.getPlayer().getInventory().getItem(slot);

        if(isNullOrAir(droppedItem)) return;

        SlotUpdateEvent slotEvent;
        //If it's null, they dropped the entire stack.
        if(newItem == null) {
            slotEvent = SlotUpdateEvent.attemptFire(event.getPlayer(), event.getPlayer().getInventory(), slot, new ItemStack(Material.AIR), droppedItem, UpdateType.DROP);
        } else {
            ItemStack oldItem = new ItemStack(newItem);
            oldItem.setAmount(oldItem.getAmount() + droppedItem.getAmount());
            slotEvent = SlotUpdateEvent.attemptFire(event.getPlayer(), event.getPlayer().getInventory(), slot, newItem, newItem, UpdateType.DROP);
        }

        event.setCancelled(slotEvent.isCancelled());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPickup(EntityPickupItemEvent event) {
        if(!(event.getEntity() instanceof Player)) return;
        if(event.isCancelled()) return;
        Player player = (Player) event.getEntity();

        Inventory inv = player.getInventory();
        ItemStack item = event.getItem().getItemStack();
        int currentAmount = event.getItem().getItemStack().getAmount();

        currentAmount = doPickupSlotUpdate(currentAmount, inv, event, 0, 9);
        currentAmount = doPickupSlotUpdate(currentAmount, inv, event, 9, 36);

        if(currentAmount > 0) {
            ItemStack newItem = new ItemStack(item);
            newItem.setAmount(currentAmount);
            SlotUpdateEvent slotEvent = SlotUpdateEvent.attemptFire(player, player.getInventory(), inv.firstEmpty(), newItem, new ItemStack(Material.AIR), UpdateType.PICKUP);
            event.setCancelled(slotEvent.isCancelled());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSwap(PlayerSwapHandItemsEvent event) {
        if(event.isCancelled()) return;

        int slot = event.getPlayer().getInventory().getHeldItemSlot();
        ItemStack offHand = event.getOffHandItem();
        ItemStack hotbar = event.getMainHandItem();

        if(isNullOrAir(offHand) && isNullOrAir(hotbar)) return;

        SlotUpdateEvent hotbarEvent = SlotUpdateEvent.attemptFire(event.getPlayer(), event.getPlayer().getInventory(), slot, hotbar, offHand, UpdateType.SWAP_HAND);
        event.setCancelled(hotbarEvent.isCancelled());

        SlotUpdateEvent offHandEvent = SlotUpdateEvent.attemptFire(event.getPlayer(), event.getPlayer().getInventory(), 40, offHand, hotbar, UpdateType.SWAP_HAND);
        event.setCancelled(offHandEvent.isCancelled());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDrag(InventoryDragEvent event) {
        for(int slot : event.getRawSlots()) {
            SlotUpdateEvent slotEvent = SlotUpdateEvent.attemptFire((Player) event.getWhoClicked(), event.getView().getInventory(slot),
                    event.getView().convertSlot(slot),
                    event.getNewItems().get(slot), event.getView().getItem(slot), UpdateType.INVENTORY_INTERACT);
            event.setCancelled(slotEvent.isCancelled());
        }
    }

    //Used for when a player closes an inventory such as a Crafting Table, and items go back into their inventory.
    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();

        Inventory playerInventory = Bukkit.createInventory(player, player.getInventory().getType());
        playerInventory.setContents(player.getInventory().getContents().clone());

        Bukkit.getScheduler().runTask(plugin, () -> {
            PlayerInventory playerInventoryAfter = player.getInventory();
            doInventoryCompare(player, playerInventory, playerInventoryAfter, null);
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onClick(InventoryClickEvent event) {
        if(event.isCancelled()) return;
        if(isNullOrAir(event.getCursor()) && isNullOrAir(event.getCurrentItem())) return;

        //This was a drop event, so add this player to didInventoryDrop.
        if(event.getAction().name().contains("DROP")) {
            didInventoryDrop.add(event.getWhoClicked().getUniqueId());
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                didInventoryDrop.remove(event.getWhoClicked().getUniqueId());
            }, 1);
        }

        Player player = (Player) event.getWhoClicked();

        Inventory playerInventory = Bukkit.createInventory(player, player.getInventory().getType());
        playerInventory.setContents(player.getInventory().getContents().clone());

        Inventory topInventory = null;
        if(player.getOpenInventory().getTopInventory().getType() != InventoryType.CRAFTING && player.getOpenInventory().getTopInventory().getType() != InventoryType.PLAYER && player.getOpenInventory().getTopInventory().getType() != InventoryType.MERCHANT) {
            topInventory = Bukkit.createInventory(player, player.getOpenInventory().getTopInventory().getType());
            Inventory openInv = player.getOpenInventory().getTopInventory();
            for(int i = 0; i < topInventory.getSize(); i++) {
                if(isNullOrAir(openInv.getItem(i))) continue;
                topInventory.setItem(i, openInv.getItem(i).clone());
            }
        }

        Inventory finalTopInventory = topInventory;

        //Because of the 1 tick delay, SlotUpdateEvent is unable to cancel an InventoryClickEvent.
        Bukkit.getScheduler().runTask(plugin, () -> {
            PlayerInventory playerInventoryAfter = player.getInventory();
            Inventory topInventoryAfter = player.getOpenInventory().getTopInventory();

            doInventoryCompare(player, playerInventory, playerInventoryAfter, event);
            doInventoryCompare(player, finalTopInventory, topInventoryAfter, event);
        });
    }

    /**
     * Compares an inventory at two different states and fires the proper SlotEvents.
     * @param player The Player
     * @param oldInventory The first Inventory state
     * @param newInventory The second Inventory state
     */
    private void doInventoryCompare(Player player, Inventory oldInventory, Inventory newInventory, InventoryClickEvent event) {
        if(oldInventory == null || newInventory == null) return;
        if(oldInventory.getType() == newInventory.getType() && oldInventory.getSize() == newInventory.getSize()) {
            for(int slot = 0; slot < oldInventory.getSize(); slot++) {
                UpdateType type = UpdateType.INVENTORY_INTERACT;
                if(event != null && (event.getView().getSlotType(slot) == InventoryType.SlotType.RESULT && event.getSlotType() != InventoryType.SlotType.RESULT)) {
                    type = UpdateType.RESULT_GENERATE;
                } else if(event != null && (event.getView().getSlotType(slot) == InventoryType.SlotType.CRAFTING && event.getSlotType() == InventoryType.SlotType.RESULT)){
                    type = UpdateType.CRAFT;
                }

                ItemStack oldItem = oldInventory.getItem(slot);
                ItemStack newItem = newInventory.getItem(slot);

                if(isNullOrAir(oldItem) && isNullOrAir(newItem)) continue;

                if((oldItem == null && newItem != null) || (newItem == null && oldItem != null) || !oldItem.equals(newItem)) {
                    SlotUpdateEvent.attemptFire(player, newInventory, slot, newInventory.getItem(slot), oldInventory.getItem(slot), type, newInventory, oldInventory);
                }
            }
        }
    }

    /*
    Returns if the armor was equipped and the event was fired.
     */
    private boolean doArmorUpdate(Player player, ItemStack item) {
        if(player.getOpenInventory().getType() != InventoryType.CRAFTING) return false;
        PlayerInventory inventory = player.getInventory();

        //Loop through each Inventory slot and see if the item works.
        for(int i = 39; i > 35; i--) {
            if(getSlotFromArmorPiece(item) == i && isNullOrAir(inventory.getItem(i))) {
                SlotUpdateEvent.attemptFire(player, inventory, i, item, new ItemStack(Material.AIR), UpdateType.ARMOR_EQUIP);
                return true;
            }
        }

        return false;
    }

    private int doUpdate(int currentSize, Inventory inventory, Player player, ItemStack item, int slot, Cancellable event) {
        if(inventory.getItem(slot).isSimilar(item)) {
            ItemStack newItem = new ItemStack(inventory.getItem(slot));
            if(currentSize + inventory.getItem(slot).getAmount() > item.getMaxStackSize()) {
                currentSize -= item.getMaxStackSize() - inventory.getItem(slot).getAmount();

                //This has to be true.
                newItem.setAmount(item.getMaxStackSize());
            } else {
                //We're adding everything to this stack.
                newItem.setAmount(newItem.getAmount() + currentSize);
                currentSize = 0;
            }

            SlotUpdateEvent slotEvent = SlotUpdateEvent.attemptFire(player, inventory, slot, newItem, inventory.getItem(slot), UpdateType.INVENTORY_INTERACT);
            event.setCancelled(slotEvent.isCancelled());
        }

        return currentSize;
    }

    private int doPickupSlotUpdate(int currentAmount, Inventory inventory, EntityPickupItemEvent event, int min, int max) {
        ItemStack item = event.getItem().getItemStack();
        Player player = (Player) event.getEntity();

        for(int i = min; i < max; i++) {
            if(currentAmount == 0) break;
            if(isNullOrAir(inventory.getItem(i))) continue;
            if(inventory.getItem(i).getAmount() == inventory.getItem(i).getMaxStackSize()) continue;

            currentAmount = doUpdate(currentAmount, inventory, player, item, i, event);
        }

        return currentAmount;
    }

    public static int getSlotFromArmorPiece(ItemStack item) {
        return getSlotFromArmorPiece(item.getType());
    }

    public static int getSlotFromArmorPiece(Material material) {
        if((material.name().contains("HELMET")
                || material.name().contains("SKULL")
                || material == Material.CARVED_PUMPKIN)) {
            return 39;
        } else if((material.name().contains("CHESTPLATE") 
                || material == Material.ELYTRA)) {
            return 38;
        } else if(material.name().contains("LEGGINGS")) {
            return 37;
        } else if(material.name().contains("BOOTS")) {
            return 36;
        }
        
        return -1;
    }

    /**
     * @param player The player who clicked
     * @param block The clicked block
     * @return if a block allows armor to be equipped when right clicking it
     */
    public static boolean isInteractable(Player player, Block block) {
        Material material = block.getType();
        if(material.isInteractable()) {
            String name = material.name();

            if(name.contains("FENCE") || name.contains("STAIRS")) {
                return false;
            }

            if(material == Material.TNT || material == Material.PUMPKIN) return false;
            if(material == Material.JUKEBOX) {
                return ((Jukebox) block.getState()).isPlaying();
            } else if(material == Material.CAKE) {
                return player.getFoodLevel() != 20;
            }

            return true;
        }

        return false;
    }

    public static boolean isNullOrAir(ItemStack item) {
        return item == null || item.getType() == Material.AIR;
    }

    public static JavaPlugin getPlugin() {
        return plugin;
    }

    public static boolean isRegistered() {
        return registered;
    }
}
