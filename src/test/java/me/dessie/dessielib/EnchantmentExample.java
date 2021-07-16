package me.dessie.dessielib;

import me.dessie.dessielib.enchantmentapi.CEnchantment;
import me.dessie.dessielib.enchantmentapi.activator.Activator;
import me.dessie.dessielib.enchantmentapi.activator.EnchantmentActivator;
import me.dessie.dessielib.enchantmentapi.properties.CEnchantProperties;
import me.dessie.dessielib.enchantmentapi.properties.Rarity;
import me.dessie.dessielib.events.slot.SlotEventHelper;
import me.dessie.dessielib.utils.SoundUtil;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.Random;
import java.util.UUID;

public enum EnchantmentExample {
    POISON(new CEnchantment("Poison")
            .setMaxLevel(5)
            .setEnchantmentTarget(EnchantmentTarget.WEAPON)
            .onEntityAttack((event, result) -> {
                if(event.getEntity() instanceof LivingEntity) {
                    ((LivingEntity) event.getEntity()).addPotionEffect(new PotionEffect(PotionEffectType.POISON, result.getLevel() * 20, 1 + result.getLevel()));
                }
            }), new CEnchantProperties()
            .setAsNormalEnchant()
            .setEnchantmentRarity(Rarity.COMMON).setDisplaysLore(false)),

    FLIGHT(new CEnchantment("Flight")
            .setMaxLevel(1)
            .addEnchantables(Material.STICK)
            .setEnchantmentActivator(new EnchantmentActivator(Activator.OFFHAND))
            .onRightClick((event, result) -> {
                if(event.getAction() != Action.RIGHT_CLICK_AIR) return;

                Player player = event.getPlayer();
                player.setAllowFlight(true);
                player.setFlying(true);
                player.sendMessage("You can fly for 5 seconds!");

                Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
                    player.setAllowFlight(false);
                    player.setFlying(false);
                }, 100);
            }), new CEnchantProperties()
            .setAsNormalEnchant()
            .setLevelPower(1, 45, 78)
            .setEnchantmentWeight(1)),

    TELEKINESIS(new CEnchantment("Telekinesis")
            .setMaxLevel(1)
            .setEnchantmentTarget(EnchantmentTarget.TOOL)
            .onBlockBreak((event, result) -> {
                event.getBlock().getDrops(event.getPlayer().getInventory().getItemInMainHand())
                        .forEach(item -> event.getPlayer().getInventory().addItem(item));
                event.setDropItems(false);
            }), new CEnchantProperties()
            .setAsNormalEnchant()
            .setLevelPower(1, 34, 64)
            .setEnchantmentWeight(1)),

    EXPLOSIVE(new CEnchantment("Explosive")
            .setMaxLevel(3)
            .addEnchantables(Material.BOW, Material.CROSSBOW)
            .onArrowLand((event, result) -> {
                event.getEntity().getLocation().getWorld().createExplosion(event.getEntity().getLocation(), result.getLevel(), false, false);
                event.getEntity().remove();
            }), new CEnchantProperties()
            .setAsNormalEnchant()
            .setEnchantmentRarity(Rarity.RARE)),

    JUMP(new CEnchantment("Jump")
            .setMaxLevel(4)
            .setEnchantmentActivator(new EnchantmentActivator(Activator.ARMOR))
            .setEnchantmentTarget(EnchantmentTarget.ARMOR_FEET)
            .onArmorEquip((event, result) -> {
                event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, result.getLevel() - 1));
            })
            .onArmorUnequip((event, result) -> {
                event.getPlayer().removePotionEffect(PotionEffectType.JUMP);
            }), new CEnchantProperties()
            .setAsNormalEnchant()
            .setEnchantmentRarity(Rarity.UNCOMMON)),

    SPEED(new CEnchantment("Speed")
            .setMaxLevel(3)
            .setUsesRomanNumerals(false)
            .setEnchantmentActivator(new EnchantmentActivator(Activator.ARMOR))
            .setEnchantmentTarget(EnchantmentTarget.ARMOR_FEET)
            .onArmorEquip((event, result) -> {
                event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, result.getLevel() - 1));
            })
            .onArmorUnequip((event, result) -> {
                event.getPlayer().removePotionEffect(PotionEffectType.SPEED);
            }), new CEnchantProperties()
            .setAsNormalEnchant()
            .setEnchantmentRarity(Rarity.RARE)
            .setAnvilMultiplier(4)),

    GUARDING(new CEnchantment("Guarding")
            .setMaxLevel(5)
            .setEnchantmentActivator(new EnchantmentActivator(Activator.OFFHAND))
            .addEnchantables(Material.SHIELD)
            .onDamaged((event, result) -> {
                if(!(event instanceof EntityDamageByEntityEvent)) return;
                int random = new Random().nextInt(5);
                if(random <= result.getLevel()) {
                    event.setDamage(event.getDamage() / 2);
                }
            }), new CEnchantProperties()
            .setAsNormalEnchant()
            .setEnchantmentRarity(Rarity.RARE)
            .setAnvilMultiplier(2)),

    LOCKED(new CEnchantment("Locked")
            .setCursed(true)
            .setMaxLevel(1)
            .setEnchantmentTarget(EnchantmentTarget.ALL)
            .setEnchantmentActivator(new EnchantmentActivator(Activator.ALL))
            .onDrop((event, result) -> {
                event.setCancelled(true);
            }), new CEnchantProperties().setAsNormalCurse()),

    POISONED(new CEnchantment("Poisoned").setMaxLevel(3)
            .setCursed(true)
            .addEnchantables(Material.BEEF, Material.PORKCHOP, Material.CHICKEN, Material.MUTTON)
            , new CEnchantProperties().setAsNormalCurse()),

    POISON_PROT(new CEnchantment("PoisonProtection")
            .setDisplayName("Poison Protection")
            .setMaxLevel(1)
            .setEnchantmentTarget(EnchantmentTarget.ARMOR_HEAD)
            .setEnchantmentActivator(new EnchantmentActivator(Activator.ARMOR))
            .onDamaged((event, result) -> {
                if(event.getCause() == EntityDamageEvent.DamageCause.POISON) {
                    event.setCancelled(true);
                }
            }), new CEnchantProperties()
            .setAsNormalEnchant()
            .setLevelPower(1, 25, 70)
            .setEnchantmentWeight(1)),

    FIREPROOF(new CEnchantment("Fireproof")
            .setMaxLevel(1)
            .setEnchantmentTarget(EnchantmentTarget.ARMOR_LEGS)
            .setEnchantmentActivator(new EnchantmentActivator(Activator.ARMOR))
            .onArmorEquip((event, result) -> {
                event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, result.getLevel() - 1));
            })
            .onArmorUnequip((event, result) -> {
                event.getPlayer().removePotionEffect(PotionEffectType.FIRE_RESISTANCE);
            }), new CEnchantProperties()
            .setAsNormalEnchant()
            .setLevelPower(1, 27, 65)
            .setEnchantmentWeight(1)),

    MIGHTY(new CEnchantment("Mighty")
            .setMaxLevel(3)
            .setEnchantmentTarget(EnchantmentTarget.WEAPON)
            .addConflicts(Enchantment.DAMAGE_ALL, Enchantment.DAMAGE_UNDEAD, Enchantment.DAMAGE_ARTHROPODS)
            .onHold((event, result) -> {
                event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, Integer.MAX_VALUE, result.getLevel() - 1));
            })
            .onUnhold((event, result) -> {
                event.getPlayer().removePotionEffect(PotionEffectType.INCREASE_DAMAGE);
            }), new CEnchantProperties()
            .setAsNormalEnchant()
            .setEnchantmentRarity(Rarity.UNCOMMON)),

    SOULBOUND(new CEnchantment("Soulbound")
            .setEnchantmentTarget(EnchantmentTarget.ALL)
            .setEnchantmentActivator(new EnchantmentActivator(Activator.ALL))
            .setMaxLevel(1)
            .onDeath((event, result) -> {
                if(!(event instanceof PlayerDeathEvent)) return;
                event.getDrops().remove(result.getItem());

                int slot = -1;
                PlayerInventory inventory = ((Player) event.getEntity()).getInventory();
                for(int i = 0; i < 41; i++) {
                    if(SlotEventHelper.isNullOrAir(inventory.getItem(i))) continue;
                    if(inventory.getItem(i).equals(result.getItem())) {
                        slot = i;
                        break;
                    }
                }

                int finalSlot = slot;
                Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
                    if (finalSlot == -1) return;
                    if (!SlotEventHelper.isNullOrAir(inventory.getItem(finalSlot))) {
                        inventory.addItem(result.getItem());
                    } else {
                        inventory.setItem(finalSlot, result.getItem());
                    }
                }, 2);
            }), new CEnchantProperties()
            .setAsNormalEnchant()
            .setLevelPower(1, 27, 64)
            .setEnchantmentWeight(1)),

    IRON_GUARDS(new CEnchantment("IronGuards").setDisplayName("Iron Guards")
            .setMaxLevel(3)
            .setEnchantmentTarget(EnchantmentTarget.ARMOR_TORSO)
            .setEnchantmentActivator(new EnchantmentActivator(Activator.ARMOR))
            .onDamaged((event, result) -> {
                if(!(event instanceof EntityDamageByEntityEvent)) return;
                if(!(((EntityDamageByEntityEvent) event).getDamager() instanceof LivingEntity)) return;
                if(((EntityDamageByEntityEvent) event).getDamager() instanceof Player) return;

                //10% chance
                if(new Random().nextInt(100) < 10) {
                    for(int i = 0; i < result.getLevel(); i++) {
                        IronGolem golem = event.getEntity().getWorld().spawn(event.getEntity().getLocation(), IronGolem.class);
                        golem.setTarget((LivingEntity) ((EntityDamageByEntityEvent) event).getDamager());

                        Main.ironGuards.add(golem);

                        Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
                            golem.setHealth(0);
                        }, 100);
                    }
                }
            }), new CEnchantProperties()
            .setAsNormalEnchant()
            .setEnchantmentRarity(Rarity.VERY_RARE)),

    LAUNCH(new CEnchantment("Launch")
            .setMaxLevel(3)
            .addEnchantables(Material.ELYTRA)
            , new CEnchantProperties()
            .setAsNormalEnchant()
            .setEnchantmentRarity(Rarity.VERY_RARE)),

    RUSH(new CEnchantment("Rush")
            .setMaxLevel(5)
            .setCanEnchantPredicate(item -> item.getType().isBlock())
            .onBlockPlace((event, result) -> {
                Location location = event.getBlockPlaced().getLocation();
                Vector vector = event.getPlayer().getFacing().getDirection();
                for (int i = 1; i < result.getLevel() + 1; i++) {
                    Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
                        location.add(vector.getBlockX(), 0, vector.getBlockZ());
                        location.getWorld().playSound(location, SoundUtil.getPlaceSound(event.getBlockPlaced()), 1, 1);
                        location.getWorld().spawnParticle(Particle.SMOKE_NORMAL, location, 10, 2, 0, 2);
                        location.getBlock().setType(event.getBlockPlaced().getType());
                        location.getBlock().setBlockData(event.getBlockPlaced().getBlockData());
                    }, i * 2L);
                }
            }), new CEnchantProperties()
            .setAsNormalEnchant()
            .setEnchantmentWeight(3)
            .setLevelPower(1, 1, 10)
            .setLevelPower(2, 10, 20)
            .setLevelPower(3, 20, 30)
            .setLevelPower(4, 30, 40)
            .setLevelPower(5, 40, 80)),

    PILLAR(new CEnchantment("Pillar")
            .setMaxLevel(10)
            .setTreasure(true)
            .setCanEnchantPredicate(item -> item.getType().isBlock())
            .onBlockPlace((event, result) -> {
                Location location = event.getBlockPlaced().getLocation();
                for (int i = 1; i < result.getLevel() + 1; i++) {
                    Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
                        location.add(0, 1, 0);
                        location.getWorld().playSound(location, SoundUtil.getPlaceSound(event.getBlockPlaced()), 1, 1);
                        location.getWorld().spawnParticle(Particle.SMOKE_NORMAL, location, 10, 2, 0, 2);
                        location.getBlock().setType(event.getBlockPlaced().getType());
                        location.getBlock().setBlockData(event.getBlockPlaced().getBlockData());
                    }, i * 2L);
                }
            }), new CEnchantProperties()
            .setAsNormalEnchant()
            .setEnchantmentWeight(3)
            .setLevelPower(1, 1, 10)
            .setLevelPower(2, 10, 20)
            .setLevelPower(3, 20, 30)
            .setLevelPower(4, 30, 40)
            .setLevelPower(5, 40, 80)),

    FIRE_FORGED(new CEnchantment("FireForged")
            .setDisplayName("Fire Forged")
            .setMaxLevel(10)
            .addConflicts(Enchantment.MENDING)
            .setEnchantmentActivator(new EnchantmentActivator(Activator.ALL))
            .setEnchantmentTarget(EnchantmentTarget.ALL)
            .onDamaged((event, result) -> {
                if(event.getCause() == EntityDamageEvent.DamageCause.LAVA) {
                    ItemStack item = result.getItem();

                    if(!item.hasItemMeta()) return;
                    if(!(item.getItemMeta() instanceof Damageable)) return;

                    Damageable damageable = (Damageable) item.getItemMeta();
                    damageable.setDamage(damageable.getDamage() - result.getLevel());
                    item.setItemMeta((ItemMeta) damageable);
                }
            }), new CEnchantProperties()
            .setAsNormalCurse()
            .setEnchantmentRarity(Rarity.VERY_RARE)),

    GHOSTLY(new CEnchantment("Ghostly")
            .setCursed(true)
            .setEnchantmentTarget(EnchantmentTarget.ALL)
            .onPickup((event, result) -> {
                event.getItem().remove();
                event.setCancelled(true);
            }), new CEnchantProperties().setAsNormalCurse()),

    REPAIR(new CEnchantment("Repair")
            .setMaxLevel(10)
            .addConflicts(Enchantment.MENDING, FIRE_FORGED.getEnchantment())
            .setEnchantmentActivator(new EnchantmentActivator(Activator.ALL))
            .setEnchantmentTarget(EnchantmentTarget.ALL)
            , new CEnchantProperties()
            .setAsNormalEnchant()
            .setEnchantmentRarity(Rarity.VERY_RARE)),

    LIGHTWEIGHT(new CEnchantment("Lightweight")
            .setMaxLevel(4)
            .setEnchantmentTarget(EnchantmentTarget.WEAPON)
            .onEnchant(result -> {
                ItemMeta meta = result.getItem().getItemMeta();
                meta.addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, new AttributeModifier(UUID.randomUUID(), "attackDamage", 7, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND));
                meta.addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED, new AttributeModifier(UUID.randomUUID(), "attackSpeed", result.getLevel(), AttributeModifier.Operation.ADD_SCALAR, EquipmentSlot.HAND));
                result.getItem().setItemMeta(meta);
            })
            .onDisenchant(result -> {
                ItemMeta meta = result.getItem().getItemMeta();
                meta.removeAttributeModifier(Attribute.GENERIC_ATTACK_SPEED);
                meta.removeAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE);
                result.getItem().setItemMeta(meta);
            }), new CEnchantProperties()
            .setAsNormalEnchant()
            .setEnchantmentRarity(Rarity.VERY_RARE)),

    GODLY(new CEnchantment("Godly")
            .setMaxLevel(1)
            .setDisplayName(ChatColor.GOLD + "Godly")
            .setCanEnchantPredicate(item -> item.getType().isEdible())
            .setTreasure(true)
            , new CEnchantProperties()
            .setAsNormalEnchant()
            .setEnchantmentRarity(Rarity.VERY_RARE)),

    HAT(new CEnchantment("Hat")
            .setMaxLevel(1)
            .setTreasure(true)
            .setEnchantmentTarget(EnchantmentTarget.ALL)
            .onRightClick((event, result) -> {
                Player player = event.getPlayer();
                ItemStack currentHelmet = player.getEquipment().getHelmet();
                player.getEquipment().setHelmet(result.getItem());
                player.getInventory().setItem(player.getInventory().getHeldItemSlot(), currentHelmet);
            }), new CEnchantProperties().setAsNormalEnchant());

    private CEnchantment enchantment;
    EnchantmentExample(CEnchantment enchantment, CEnchantProperties properties) {
        this.enchantment = enchantment;
        enchantment.setEnchantProperties(properties);
    }

    public CEnchantment getEnchantment() {
        return enchantment;
    }
}
