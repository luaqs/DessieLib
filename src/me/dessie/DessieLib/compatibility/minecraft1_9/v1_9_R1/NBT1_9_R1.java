package me.dessie.DessieLib.compatibility.minecraft1_9.v1_9_R1;

import me.dessie.DessieLib.compatibility.VersionHandler;
import net.minecraft.server.v1_9_R1.NBTTagCompound;
import org.bukkit.craftbukkit.v1_9_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class NBT1_9_R1 implements VersionHandler {

    public ItemStack addNBT(ItemStack item, String nbt, Object value) {
        net.minecraft.server.v1_9_R1.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);

        if(nmsItem.getTag() == null) {
            nmsItem.setTag(new NBTTagCompound());
        }

        NBTTagCompound base = nmsItem.getTag();
        if(base.get(nbt) == null) {
            base.set(nbt, new NBTTagCompound());
        }

        NBTTagCompound nbtTag = (NBTTagCompound)base.get(nbt);
        if(value instanceof String) {
            nbtTag.setString(nbt, (String) value);
        } else if (value instanceof Integer) {
            nbtTag.setInt(nbt, (Integer) value);
        } else if (value instanceof Boolean) {
            nbtTag.setBoolean(nbt, (Boolean) value);
        } else {
            nbtTag.setString(nbt, null);
        }
        item = CraftItemStack.asBukkitCopy(nmsItem);

        return item;
    }

    public Object getNBT(ItemStack item, String nbt, Object as) {
        net.minecraft.server.v1_9_R1.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
        if(nmsItem.getTag() == null) {
            nmsItem.setTag(new NBTTagCompound());
        }

        NBTTagCompound base = nmsItem.getTag();
        if(base.get(nbt) == null) {
            base.set(nbt, new NBTTagCompound());
        }
        NBTTagCompound nbtTag = (NBTTagCompound)base.get(nbt);

        if(as instanceof String) {
            return nbtTag.getString(nbt);
        } else if (as instanceof Integer) {
            return nbtTag.getInt(nbt);
        } else if (as instanceof Boolean) {
            return nbtTag.getBoolean(nbt);
        } else {
            return null;
        }
    }

    @Override
    public void openAnvilInventory(Player player, ItemStack item) {

    }

}
