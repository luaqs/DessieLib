package me.dessie.dessielib.inventoryapi;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;

public class InventoryAPI implements Listener {

    private Plugin plugin;
    private static boolean registered = false;

    private InventoryAPI(Plugin plugin) {
        this.plugin = plugin;
        this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
        registered = true;
    }

    /**
     * Main registration method, this must be called with your Plugin instance
     * before creating or accessing any of the Inventory objects.
     * @param plugin Your plugin instance
     */
    public static void register(Plugin plugin) {
        new InventoryAPI(plugin);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        Player player = (Player) event.getWhoClicked();
        Inventory currentInv = event.getInventory();

        if(InventoryBuilder.getBuilder(player) != null && InventoryBuilder.getBuilder(player).getInventory() == currentInv) {
            InventoryBuilder invBuilder = InventoryBuilder.getBuilder(player);
            Bukkit.getScheduler().runTaskLater(plugin, invBuilder::updateBuilder, 1);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Inventory currentInv = event.getInventory();

        if(InventoryBuilder.getBuilder(player) != null && InventoryBuilder.getBuilder(player).getInventory() == currentInv) {
            InventoryBuilder invBuilder = InventoryBuilder.getBuilder(player);
            Bukkit.getScheduler().runTaskLater(plugin, invBuilder::updateBuilder, 1);

            if (event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR) {
                ItemBuilder clicked = invBuilder.getItem(event.getSlot());

                for(ItemBuilder item : invBuilder.getItems().values()) {
                    if(item.isSimilar(clicked) && item.getSlot() == clicked.getSlot()) {
                        switch(event.getClick()) {
                            case LEFT: item.clickType = ClickType.LEFT; break;
                            case RIGHT: item.clickType = ClickType.RIGHT; break;
                            case MIDDLE: item.clickType = ClickType.MIDDLE; break;
                            case SHIFT_LEFT: item.clickType = ClickType.SHIFT_LEFT; break;
                            case SHIFT_RIGHT: item.clickType = ClickType.SHIFT_RIGHT; break;
                        }

                        if(event.getCursor() != null) {
                            item.heldItem = event.getCursor();
                        }

                        if(item.isCancel()) {
                            event.setCancelled(true);
                        }
                        item.executeClick(player, item);
                        item.swap();
                        return;
                    }
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Inventory currentInv = event.getInventory();
        Player player = (Player) event.getPlayer();

        if (InventoryBuilder.getBuilder(player) != null && InventoryBuilder.getBuilder(player).getInventory() == currentInv) {
            InventoryBuilder invBuilder = InventoryBuilder.getBuilder(player);

            if (invBuilder.isPreventClose()) {
                Bukkit.getScheduler().runTaskLater(plugin, () -> event.getPlayer().openInventory(invBuilder.getInventory()), 1);
            } else {
                invBuilder.executeClose(player, invBuilder);
                InventoryBuilder.getInventories().remove(player.getName());
            }
        }
    }

    public static boolean isRegistered() {
        return registered;
    }

}
