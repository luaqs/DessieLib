package me.dessie.dessielib;

import me.dessie.dessielib.enchantmentapi.CEnchantment;
import me.dessie.dessielib.enchantmentapi.CEnchantmentLoader;
import me.dessie.dessielib.utils.Colors;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.StringUtil;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Main extends JavaPlugin implements TabExecutor, Listener {

    private static List<String> positiveEffects = Arrays.asList("SPEED", "NIGHT_VISION", "INCREASE_DAMAGE",
            "JUMP", "REGENERATION", "FIRE_RESISTANCE", "INVISIBILITY", "WATER_BREATHING", "ABSORPTION",
            "CONDUIT_POWER", "DAMAGE_RESISTANCE", "DOLPHINS_GRACE", "FAST_DIGGING", "HEAL", "HEALTH_BOOST",
            "SLOW_FALLING");

    static List<IronGolem> ironGuards = new ArrayList<>();
    static List<Player> isRepairing = new ArrayList<>();

    private static Main instance;

    @Override
    public void onEnable() {
        instance = this;
        this.getServer().getPluginManager().registerEvents(this, this);

        getCommand("cenchant").setTabCompleter(this);
        CEnchantmentLoader.register(this);

        for(EnchantmentExample example : EnchantmentExample.values()) {
            System.out.println("Loading " + example.getEnchantment().getDisplayName() + "...");
        }

        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
            CEnchantment repair = CEnchantment.getByName("Repair");
            List<Player> invalid = new ArrayList<>();
            for(Player player : isRepairing) {
                if(!player.isOnline()) {
                    invalid.add(player);
                    continue;
                }

                for(ItemStack item : player.getInventory().getContents()) {
                    if(!CEnchantment.hasEnchantment(item, repair)) continue;
                    if(!item.hasItemMeta()) continue;
                    if(!(item.getItemMeta() instanceof Damageable)) continue;

                    Damageable damageable = (Damageable) item.getItemMeta();
                    damageable.setDamage(damageable.getDamage() - CEnchantment.getLevel(item, repair));
                    item.setItemMeta((ItemMeta) damageable);
                }
            }
            isRepairing.removeAll(invalid);
        }, 20, 20);

        System.out.println(Colors.color("&c&lPlugin enabled."));
    }

    @EventHandler
    public void onCrouch(PlayerToggleSneakEvent event) {
        if(event.getPlayer().isSneaking()) {
            isRepairing.remove(event.getPlayer());
        } else {
            isRepairing.add(event.getPlayer());
        }
    }

    @EventHandler
    public void onGlide(EntityToggleGlideEvent event) {
        if(!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        if(player.isGliding()) return;
        if(!player.isSneaking()) return;

        CEnchantment launch = CEnchantment.getByName("Launch");
        ItemStack elytra = player.getInventory().getChestplate();
        if(elytra == null || elytra.getType() == Material.AIR) return;

        if(CEnchantment.hasEnchantment(elytra, launch)) {
            player.setVelocity(new Vector(0, (double) CEnchantment.getLevel(elytra, launch) / 2, 0));
        }
    }


    @EventHandler
    public void onGolemDeath(EntityDeathEvent event) {
        if(!(event.getEntity() instanceof IronGolem)) return;
        IronGolem golem = (IronGolem) event.getEntity();

        if(ironGuards.contains(golem)) {
            event.getDrops().clear();
            ironGuards.remove(golem);
        }
    }

    @EventHandler
    public void onFoodConsume(PlayerItemConsumeEvent event) {
        CEnchantment poisoned = CEnchantment.getByName("Poisoned");
        CEnchantment godly = CEnchantment.getByName("Godly");
        if(CEnchantment.hasEnchantment(event.getItem(), poisoned)) {
            int level = CEnchantment.getLevel(event.getItem(), poisoned);
            event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.POISON, 60 * level, level));
        }

        if(CEnchantment.hasEnchantment(event.getItem(), godly)) {
            int level = CEnchantment.getLevel(event.getItem(), godly);
            for(PotionEffectType type : PotionEffectType.values()) {
                if(positiveEffects.contains(type.getName())) {
                    event.getPlayer().addPotionEffect(new PotionEffect(type, 200 * level, level));
                }
            }
        }
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(command.getName().equalsIgnoreCase("cenchant")) {
            Player player = (Player) sender;

            ItemStack item = player.getInventory().getItemInMainHand();
            String itemName = (item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName() : WordUtils.capitalizeFully(item.getType().name().toLowerCase().replace("_", " ")));
            if(item.getType() == Material.AIR) return true;

            if(args.length > 0) {
                if(args[0].equalsIgnoreCase("all")) {
                    for(CEnchantment enchantment : CEnchantment.getEnchantments()) {
                        if(args[args.length - 1].equalsIgnoreCase("-u")) {
                            enchantment.unsafeEnchant(item, enchantment.getMaxLevel());
                        } else {
                            try {
                                enchantment.enchant(item, enchantment.getMaxLevel());
                            } catch (IllegalArgumentException ignored) {}
                        }
                    }

                    player.sendMessage(ChatColor.GREEN + "Enchanted " + ChatColor.YELLOW + itemName + ChatColor.GREEN + " with all " + (args[args.length - 1].equals("-u") ? "" : "possible ") + "enchantments");

                } else {
                    CEnchantment ench = CEnchantment.getByName(args[0]);
                    try {
                        int level;
                        if (args.length > 1 && args[1].equalsIgnoreCase("-u")) {
                            level = ench.getMaxLevel();
                        } else {
                            level = args.length > 1 ? Integer.parseInt(args[1]) : ench.getMaxLevel();
                        }
                        try {
                            if(args[args.length - 1].equalsIgnoreCase("-u")) {
                                ench.unsafeEnchant(item, level);
                            } else {
                                ench.enchant(item, level);
                            }
                            player.sendMessage(ChatColor.GREEN + "Enchanted " + ChatColor.YELLOW + itemName + ChatColor.GREEN + " with " + ChatColor.LIGHT_PURPLE + ench.getDisplayName() + ChatColor.GREEN + " at level " + ChatColor.RED + level);
                        } catch (IllegalArgumentException e) {
                            player.sendMessage(ChatColor.RED + e.getMessage());
                        }
                    } catch (NumberFormatException e) {
                        if(args[1].equalsIgnoreCase("remove")) {
                            ench.removeEnchantment(item);
                            player.sendMessage(ChatColor.GREEN + "Successfully removed " + ChatColor.LIGHT_PURPLE + ench.getDisplayName() + ChatColor.GREEN + " from " + ChatColor.YELLOW + itemName);
                        }
                    }
                }
            } else player.sendMessage(ChatColor.RED + "Not enough arguments!");
            return true;
        }
        return false;
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> results = new ArrayList<>();
        if(command.getName().equalsIgnoreCase("cenchant")) {
            if(args.length == 1) {
                results.addAll(CEnchantment.getEnchantments().stream().map(CEnchantment::getName)
                        .collect(Collectors.toList()));
                results.add("all");
                return StringUtil.copyPartialMatches(args[0], results, new ArrayList<>());
            }

            if(args.length == 2) {
                CEnchantment enchantment = CEnchantment.getByName(args[0]);
                if(enchantment == null) return new ArrayList<>();

                for(int i = 1; i < enchantment.getMaxLevel() + 1; i++) {
                    results.add(String.valueOf(i));
                }

                results.add("-u");
                return StringUtil.copyPartialMatches(args[1], results, new ArrayList<>());
            }

            if(args.length == 3) {
                results.add("-u");
                return StringUtil.copyPartialMatches(args[2], results, new ArrayList<>());
            }

        }

        return new ArrayList<>();
    }

    public static Main getInstance() {
        return instance;
    }
}
