package me.dessie.DessieLib.compatibility.minecraft1_9.v1_9_R2.Anvil;

import me.dessie.DessieLib.compatibility.VersionHandler;
import net.minecraft.server.v1_9_R2.BlockPosition;
import net.minecraft.server.v1_9_R2.ContainerAnvil;
import net.minecraft.server.v1_9_R2.EntityHuman;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class FakeAnvil extends ContainerAnvil implements VersionHandler {

    public FakeAnvil(EntityHuman entityHuman) {
        super(entityHuman.inventory, entityHuman.world, new BlockPosition(0,0,0), entityHuman);
        this.checkReachable = false;
    }

    public boolean a(EntityHuman entityHuman) {
        return true;
    }

    @Override
    public ItemStack addNBT(ItemStack item, String nbt, Object value) {
        return null;
    }

    @Override
    public Object getNBT(ItemStack item, String nbt, Object as) {
        return null;
    }

    @Override
    public void openAnvilInventory(Player player, ItemStack item) {

    }
}