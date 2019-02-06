package me.dessie.DessieLib.compatibility.minecraft1_8.v1_8_R1.Anvil;

import me.dessie.DessieLib.compatibility.VersionHandler;
import net.minecraft.server.v1_8_R1.ChatMessage;
import net.minecraft.server.v1_8_R1.EntityPlayer;
import net.minecraft.server.v1_8_R1.PacketPlayOutOpenWindow;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class Anvil1_8_R1 implements VersionHandler {

    @Override
    public ItemStack addNBT(ItemStack item, String nbt, Object value) {
        return null;
    }

    @Override
    public Object getNBT(ItemStack item, String nbt, Object as) {
        return null;
    }

    @SuppressWarnings("Duplicates")
    public void openAnvilInventory(final Player player, ItemStack item) {
        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
        FakeAnvil fakeAnvil = new FakeAnvil(entityPlayer);
        int containerId = entityPlayer.nextContainerCounter();

        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutOpenWindow(containerId, "minecraft:anvil", new ChatMessage("Repairing"), 0));

        entityPlayer.activeContainer = fakeAnvil;
        entityPlayer.activeContainer.windowId = containerId;
        entityPlayer.activeContainer.addSlotListener(entityPlayer);
        entityPlayer.activeContainer = fakeAnvil;
        entityPlayer.activeContainer.windowId = containerId;

        Inventory inv = fakeAnvil.getBukkitView().getTopInventory();
        inv.setItem(0, item);
    }
}
