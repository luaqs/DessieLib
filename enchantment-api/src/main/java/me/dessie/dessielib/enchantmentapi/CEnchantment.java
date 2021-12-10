package me.dessie.dessielib.enchantmentapi;

import me.dessie.dessielib.enchantmentapi.activator.Activator;
import me.dessie.dessielib.enchantmentapi.activator.EnchantmentActivator;
import me.dessie.dessielib.enchantmentapi.listener.CEventResult;
import me.dessie.dessielib.enchantmentapi.properties.CEnchantProperties;
import me.dessie.dessielib.enchantmentapi.utils.RomanNumeral;
import me.dessie.dessielib.core.events.slot.SlotEventHelper;
import me.dessie.dessielib.core.events.slot.SlotUpdateEvent;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class CEnchantment extends Enchantment {

    private static List<CEnchantment> enchantments = new ArrayList<>();

    private boolean registered;

    private final String name;
    private String displayName;

    private int maxLevel = 1;
    private boolean treasure;
    private boolean cursed;
    private Predicate<ItemStack> canEnchantPredicate;
    private final List<Material> canEnchant = new ArrayList<>();
    private final List<Enchantment> conflicts = new ArrayList<>();
    private EnchantmentTarget target;
    private EnchantmentActivator activator = new EnchantmentActivator(Activator.HAND);
    private CEnchantProperties properties = new CEnchantProperties().setAsNormalEnchant();

    private boolean usesRomanNumerals = true;

    private BiConsumer<EntityDamageByEntityEvent, CEventResult> entityAttack;
    private BiConsumer<PlayerInteractEvent, CEventResult> rightClick;
    private BiConsumer<BlockBreakEvent, CEventResult> blockBreak;
    private BiConsumer<BlockPlaceEvent, CEventResult> blockPlace;

    private BiConsumer<ProjectileLaunchEvent, CEventResult> arrowShoot;
    private BiConsumer<ProjectileHitEvent, CEventResult> arrowLand;
    private BiConsumer<EntityDamageEvent, CEventResult> damaged;
    private BiConsumer<EntityPickupItemEvent, CEventResult> pickup;
    private BiConsumer<PlayerDropItemEvent, CEventResult> dropped;
    private BiConsumer<EntityDeathEvent, CEventResult> death;

    private BiConsumer<SlotUpdateEvent, CEventResult> hold;
    private BiConsumer<SlotUpdateEvent, CEventResult> unhold;

    private BiConsumer<SlotUpdateEvent, CEventResult> armorEquip;
    private BiConsumer<SlotUpdateEvent, CEventResult> armorUnequip;

    private Consumer<CEventResult> enchant;
    private Consumer<CEventResult> disenchant;

    public CEnchantment(String name) {
        super(new NamespacedKey(CEnchantmentLoader.getPlugin(), name));
        if(!CEnchantmentLoader.isRegistered()) {
            throw new IllegalStateException("You need to register CEnchantmentLoader before creating CEnchantments!");
        }

        this.name = name;
        this.registerEnchantment();
    }

    public CEnchantProperties getEnchantProperties() { return this.properties; }
    public String getDisplayName() { return this.displayName == null ? this.getName() : this.displayName;  }
    public EnchantmentActivator getEnchantmentActivator() { return activator; }
    public boolean isUsesRomanNumerals() { return usesRomanNumerals; }
    public boolean isRegistered() {
        return registered;
    }

    public BiConsumer<EntityDamageByEntityEvent, CEventResult> getEntityAttack() { return entityAttack; }
    public BiConsumer<PlayerInteractEvent, CEventResult> getRightClick() { return rightClick; }
    public BiConsumer<BlockBreakEvent, CEventResult> getBlockBreak() { return blockBreak; }
    public BiConsumer<BlockPlaceEvent, CEventResult> getBlockPlace() { return blockPlace; }
    public BiConsumer<ProjectileLaunchEvent, CEventResult> getArrowShoot() { return arrowShoot; }
    public BiConsumer<ProjectileHitEvent, CEventResult> getArrowLand() { return arrowLand; }
    public BiConsumer<PlayerDropItemEvent, CEventResult> getDropped() { return dropped; }
    public BiConsumer<EntityPickupItemEvent, CEventResult> getPickup() { return pickup; }
    public BiConsumer<EntityDamageEvent, CEventResult> getDamaged() { return damaged; }
    public BiConsumer<EntityDeathEvent, CEventResult> getDeath() { return death; }

    public BiConsumer<SlotUpdateEvent, CEventResult> getHold() { return hold; }
    public BiConsumer<SlotUpdateEvent, CEventResult> getUnhold() { return unhold; }
    public BiConsumer<SlotUpdateEvent, CEventResult> getArmorEquip() { return armorEquip; }
    public BiConsumer<SlotUpdateEvent, CEventResult> getArmorUnequip() { return armorUnequip; }
    public Consumer<CEventResult> getEnchant() { return enchant; }
    public Consumer<CEventResult> getDisenchant() { return disenchant; }

    public void enchant(ItemStack item, int level) { enchant(item, level, false); }
    private void enchant(ItemStack item, int level, boolean unsafe) { enchant(item, level, unsafe, true); }
    public void unsafeEnchant(ItemStack item, int level) { enchant(item, level, true); }

    public void enchant(ItemStack item, int level, boolean unsafe, boolean doEnchantEvent) {
        if(item == null || item.getItemMeta() == null) return;

        if(item.getItemMeta().hasEnchant(this)) {
            removeEnchantment(item, false);
        }

        if(item.getType() == Material.BOOK || item.getType() == Material.ENCHANTED_BOOK) {
            if(!this.getEnchantProperties().canBeOnBook()) return;

            item.setType(Material.ENCHANTED_BOOK);
            EnchantmentStorageMeta meta = (EnchantmentStorageMeta) item.getItemMeta();
            meta.addStoredEnchant(this, level, unsafe);
            item.setItemMeta(meta);
        } else {
            if(unsafe) {
                item.addUnsafeEnchantment(this, level);
            } else item.addEnchantment(this, level);
        }

        if(this.getEnchantProperties().isLoreDisplayed()) {
            ItemMeta meta = item.getItemMeta();
            List<String> lore = meta.getLore() != null ? meta.getLore() : new ArrayList<>();
            lore.add(0, (this.isCursed() ? ChatColor.RED : ChatColor.GRAY) + this.getDisplayName() + (this.getMaxLevel() > 1 ? " " +
                    (this.isUsesRomanNumerals() ? RomanNumeral.fromInt(level) : level) : ""));
            meta.setLore(lore);
            item.setItemMeta(meta);
        }

        if(doEnchantEvent && this.getEnchant() != null) {
            this.getEnchant().accept(new CEventResult(this, item, level));
        }
    }

    public void removeEnchantment(ItemStack item) { removeEnchantment(item, true); }
    public void removeEnchantment(ItemStack item, boolean doDisenchantEvent) {
        if(item.getItemMeta() == null) return;
        int level = getLevel(item, this);

        if(item.getType() == Material.ENCHANTED_BOOK) {
            EnchantmentStorageMeta meta = (EnchantmentStorageMeta) item.getItemMeta();
            meta.removeStoredEnchant(this);
            item.setItemMeta(meta);
        } else {
            item.removeEnchantment(this);
        }

        if(this.getEnchantProperties().isLoreDisplayed()) {
            ItemMeta meta = item.getItemMeta();
            List<String> lore = meta.getLore() != null ? meta.getLore() : new ArrayList<>();
            for (String s : lore) {
                if (s.contains((this.isCursed() ? ChatColor.RED : ChatColor.GRAY) + this.getDisplayName())) {
                    lore.remove(s);
                    break;
                }
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
        }

        if(doDisenchantEvent && this.getDisenchant() != null) {
            this.getDisenchant().accept(new CEventResult(this, item, level));
        }
    }

    public CEnchantment setEnchantProperties(CEnchantProperties properties) {
        this.properties = properties;
        properties.setEnchantment(this);
        return this;
    }

    public CEnchantment setDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    public CEnchantment setMaxLevel(int maxLevel) {
        this.maxLevel = maxLevel;
        return this;
    }

    public CEnchantment setUsesRomanNumerals(boolean usesRomanNumerals) {
        this.usesRomanNumerals = usesRomanNumerals;
        return this;
    }

    public CEnchantment setTreasure(boolean treasure) {
        this.treasure = treasure;
        return this;
    }

    public CEnchantment setCursed(boolean cursed) {
        this.cursed = cursed;
        return this;
    }

    public CEnchantment setEnchantmentTarget(EnchantmentTarget target) {
        this.target = target;
        return this;
    }

    public CEnchantment setEnchantmentActivator(EnchantmentActivator activator) {
        this.activator = activator;
        return this;
    }

    public CEnchantment setCanEnchantPredicate(Predicate<ItemStack> predicate) {
        this.canEnchantPredicate = predicate;
        return this;
    }

    public CEnchantment addEnchantables(Material... materials) {
        canEnchant.addAll(Arrays.asList(materials));
        return this;
    }

    public CEnchantment addConflicts(Enchantment... enchantments) {
        conflicts.addAll(Arrays.asList(enchantments));
        return this;
    }

    public CEnchantment onRightClick(BiConsumer<PlayerInteractEvent, CEventResult> consumer) {
        this.rightClick = consumer;
        return this;
    }

    public CEnchantment onEntityAttack(BiConsumer<EntityDamageByEntityEvent, CEventResult> consumer) {
        this.entityAttack = consumer;
        return this;
    }

    public CEnchantment onBlockBreak(BiConsumer<BlockBreakEvent, CEventResult> consumer) {
        this.blockBreak = consumer;
        return this;
    }

    public CEnchantment onBlockPlace(BiConsumer<BlockPlaceEvent, CEventResult> consumer) {
        this.blockPlace = consumer;
        return this;
    }

    public CEnchantment onArrowFire(BiConsumer<ProjectileLaunchEvent, CEventResult> consumer) {
        this.arrowShoot = consumer;
        return this;
    }

    public CEnchantment onArrowLand(BiConsumer<ProjectileHitEvent, CEventResult> consumer) {
        this.arrowLand = consumer;
        return this;
    }

    public CEnchantment onHold(BiConsumer<SlotUpdateEvent, CEventResult> consumer) {
        this.hold = consumer;
        return this;
    }

    public CEnchantment onUnhold(BiConsumer<SlotUpdateEvent, CEventResult> consumer) {
        this.unhold = consumer;
        return this;
    }

    public CEnchantment onArmorEquip(BiConsumer<SlotUpdateEvent, CEventResult> consumer) {
        this.armorEquip = consumer;
        return this;
    }

    public CEnchantment onArmorUnequip(BiConsumer<SlotUpdateEvent, CEventResult> consumer) {
        this.armorUnequip = consumer;
        return this;
    }

    public CEnchantment onDamaged(BiConsumer<EntityDamageEvent, CEventResult> consumer) {
        this.damaged = consumer;
        return this;
    }

    public CEnchantment onDeath(BiConsumer<EntityDeathEvent, CEventResult> consumer) {
        this.death = consumer;
        return this;
    }

    public CEnchantment onDrop(BiConsumer<PlayerDropItemEvent, CEventResult> consumer) {
        this.dropped = consumer;
        return this;
    }

    public CEnchantment onPickup(BiConsumer<EntityPickupItemEvent, CEventResult> consumer) {
        this.pickup = consumer;
        return this;
    }

    public CEnchantment onEnchant(Consumer<CEventResult> consumer) {
        this.enchant = consumer;
        return this;
    }

    public CEnchantment onDisenchant(Consumer<CEventResult> consumer) {
        this.disenchant = consumer;
        return this;
    }


    private void registerEnchantment() {
        if(this.isRegistered()) throw new IllegalStateException("Enchantment is already registered");

        //Server already knows about the enchantment, server was probably reloaded.
        //We need to unregister this enchantment.
        if(Enchantment.getByKey(new NamespacedKey(CEnchantmentLoader.getPlugin(), this.getName())) != null) {
            try {
                Field byKey = Enchantment.class.getDeclaredField("byKey");
                Field byName = Enchantment.class.getDeclaredField("byName");
                byKey.setAccessible(true);
                byName.setAccessible(true);
                Map<NamespacedKey, Enchantment> keys = (Map<NamespacedKey, Enchantment>) byKey.get(null);
                Map<String, Enchantment> names = (Map<String, Enchantment>) byName.get(null);

                keys.remove(new NamespacedKey(CEnchantmentLoader.getPlugin(), this.getName()));
                names.remove(this.getName());

                byKey.setAccessible(false);
                byName.setAccessible(false);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        try {
            Field field = Enchantment.class.getDeclaredField("acceptingNew");
            field.setAccessible(true);
            field.setBoolean(null, true);
            field.setAccessible(false);

            Enchantment.registerEnchantment(this);
            this.registered = true;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        Enchantment.stopAcceptingRegistrations();
        enchantments.add(this);
    }

    @Override
    public String getName() { return this.name; }

    @Override
    public int getMaxLevel() { return this.maxLevel; }

    @Override
    public int getStartLevel() { return 1; }

    @Override
    public EnchantmentTarget getItemTarget() { return target; }

    @Override
    public boolean isTreasure() { return treasure; }

    @Override
    public boolean isCursed() { return cursed; }

    @Override
    public boolean conflictsWith(Enchantment enchantment) {
        return conflicts.contains(enchantment) || this == enchantment;
    }

    /**
     * Checks if two enchantment conflict with each other.
     * For example, it's possible for a CEnchantment to conflict with Sharpness
     * But Sharpness doesn't conflict with Mighty.
     *
     * @param enchantment An Enchantment to check
     * @param enchantment2 A second enchantment to check.
     * @return If either enchantment conflicts with each other.
     */
    public static boolean conflictsWith(Enchantment enchantment, Enchantment enchantment2) {
        return enchantment.conflictsWith(enchantment2) || enchantment2.conflictsWith(enchantment);
    }

    /**
     * @param enchantments All me.dessie.dessielib.enchantments to check
     * @return If this enchantment conflicts with ANY of the provided me.dessie.dessielib.enchantments.
     */
    public boolean conflictsWith(Set<Enchantment> enchantments) {
        for(Enchantment enchantment : enchantments) {
            if(conflictsWith(enchantment)) return true;
        }

        return false;
    }

    public boolean conflictsWith(ItemStack item) {
        for(Enchantment enchantment : item.getEnchantments().keySet()) {
            if(conflictsWith(enchantment)) return true;
        }

        return false;
    }

    @Override
    public boolean canEnchantItem(ItemStack item) {
        return canEnchant.contains(item.getType())
                || (getItemTarget() != null && getItemTarget().includes(item))
                || (this.canEnchantPredicate != null && this.canEnchantPredicate.test(item));
    }

    public static boolean hasEnchantment(ItemStack item, Enchantment enchantment) {
        if(item == null) return false;
        if(item.getItemMeta() == null) return false;
        return item.getItemMeta().hasEnchant(enchantment);
    }

    public static int getLevel(ItemStack item, Enchantment enchantment) {
        if(item.getType() == Material.ENCHANTED_BOOK) {
            EnchantmentStorageMeta meta = (EnchantmentStorageMeta) item.getItemMeta();
            return meta.getStoredEnchants().containsKey(enchantment) ? meta.getStoredEnchants().get(enchantment) : 0;
        }

        if(!hasEnchantment(item, enchantment)) return 0;
        return item.getEnchantments().get(enchantment);
    }

    //Returns all me.dessie.dessielib.enchantments
    public static List<CEnchantment> getEnchantments() {
        return enchantments;
    }

    //Returns all CEnchantments on an item
    public static List<CEnchantment> getEnchantments(ItemStack item) {
        if(SlotEventHelper.isNullOrAir(item)) return new ArrayList<>();

        if(item.getType() == Material.ENCHANTED_BOOK) {
            return (((EnchantmentStorageMeta) item.getItemMeta()).getStoredEnchants().keySet().stream().map(ench -> CEnchantment.getByKey(ench.getKey()))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList()));
        } else {
            return item.getEnchantments().keySet().stream().map(ench -> CEnchantment.getByKey(ench.getKey()))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
    }

    public static CEnchantment getByName(String name) {
        return enchantments.stream().filter(ench -> ench.getName().equalsIgnoreCase(name))
                .findAny().orElse(null);
    }

    public static CEnchantment getByKey(NamespacedKey key) {
        return CEnchantment.getEnchantments().stream().filter(ench -> ench.getKey().equals(key)).findAny().orElse(null);
    }

    @Override
    public String toString() {
        return "CEnchantment[" + this.getKey() + "]";
    }

}
