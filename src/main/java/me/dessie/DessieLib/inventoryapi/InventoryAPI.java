package me.dessie.DessieLib.inventoryapi;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
    public InventoryAPI(Plugin plugin) {
        this.plugin = plugin;
    }

    static String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
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

                        //Items are the same, do all ItemBuilder things now
                        item.getBuilder().clickedItem = item;

                        if(event.getClick() == ClickType.LEFT) {
                            item.clickType = ClickType.LEFT;
                        } else if(event.getClick() == ClickType.RIGHT) {
                            item.clickType = ClickType.RIGHT;
                        } else if(event.getClick() == ClickType.MIDDLE) {
                            item.clickType = ClickType.MIDDLE;
                        }

                        if(event.getCursor() != null) {
                            item.heldItem = event.getCursor();
                        }

                        if(item.isCancel()) {
                            event.setCancelled(true);
                        }

                        item.executeClick();
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
            InventoryBuilder invBuilder = InventoryBuilder.inventories.get(player.getName());

            if (invBuilder.preventClose) {
                Bukkit.getScheduler().runTaskLater(plugin, () -> event.getPlayer().openInventory(invBuilder.getInventory()), 1);
            } else {
                invBuilder.executeClose();
                InventoryBuilder.inventories.remove(player.getName());
            }
        }
    }
}
