package me.dessie.dessielib.enchantmentapi.activator;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EnchantmentActivator {

    private List<Activator> activators = new ArrayList<>();

    public EnchantmentActivator(Activator... activators) {
        this.addActivators(activators);
    }

    public List<Activator> getActivators() {
        return activators;
    }

    public void addActivators(Activator... activators) {
        this.activators.addAll(Arrays.asList(activators));
    }

    public boolean hasActivator(Activator activator) {
        if(activator == Activator.MAINHAND) {
            return this.getActivators().contains(Activator.MAINHAND)
                    || this.getActivators().contains(Activator.HAND)
                    || this.getActivators().contains(Activator.ALL);
        } else if(activator == Activator.OFFHAND) {
            return this.getActivators().contains(Activator.OFFHAND)
                    || this.getActivators().contains(Activator.HAND)
                    || this.getActivators().contains(Activator.ALL);
        }

        return this.getActivators().contains(activator) || this.getActivators().contains(Activator.ALL);
    }

    public List<ItemStack> getItems(LivingEntity entity) {
        List<ItemStack> items = new ArrayList<>();
        if(entity.getEquipment() == null) return items;

        EntityEquipment equipment = entity.getEquipment();

        //If it has Inventory activator, this item will be given twice, so we only give the mainhand
        //if they aren't going to request inventory.
        if((hasActivator(Activator.MAINHAND) || hasActivator(Activator.HAND)) && !hasActivator(Activator.INVENTORY)) {
            items.add(equipment.getItemInMainHand());
        }

        if(hasActivator(Activator.OFFHAND) || hasActivator(Activator.HAND)) {
            items.add(equipment.getItemInOffHand());
        }

        if(hasActivator(Activator.ARMOR)) {
            items.addAll(Arrays.asList(equipment.getArmorContents()));
        }

        if(hasActivator(Activator.INVENTORY)) {
            if(entity instanceof Player) {
                PlayerInventory inv = ((Player) entity).getInventory();
                for(int i = 0; i < 36; i++) {
                    items.add(inv.getItem(i));
                }
            }
        }

        return items;
    }


}
