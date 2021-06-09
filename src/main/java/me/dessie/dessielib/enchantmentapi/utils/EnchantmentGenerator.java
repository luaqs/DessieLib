package me.dessie.dessielib.enchantmentapi.utils;

import me.dessie.dessielib.enchantmentapi.CEnchantment;
import me.dessie.dessielib.enchantmentapi.properties.CEnchantProperties;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_16_R3.enchantments.CraftEnchantment;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class EnchantmentGenerator {

    private static Random random = new Random();

    /**
     * Does the random math for generating an Enchantment level.
     * @return The modified power for this enchant
     */
    public static int getModifiedPower(Material material, int base) {
        int enchantability = getEnchantability(material);
        int range = random.nextInt((115 - 85) + 1) + 85; //Get a number between 85 and 115.
        double modifier = range/100.0; //Map to 0.85 and 1.15

        return (int) (modifier * (base + random.nextInt(enchantability / 4 + 1) + random.nextInt(enchantability / 4 + 1) + 1));
    }

    public static Map<Enchantment, Integer> getPossibleEnchantments(ItemStack item, Enchantment clickedEnchantment, int modifiedPower) {
        Map<Enchantment, Integer> enchantments = new HashMap<>();

        //Add all the possible Vanilla enchantments.
        for(Enchantment enchantment : Enchantment.values()) {
            //Enchantment is already granted, because that was the offer.
            if(enchantment == clickedEnchantment) continue;
            if(CEnchantment.getByKey(enchantment.getKey()) != null) continue;
            if(enchantment.isTreasure()) continue;
            if(!enchantment.canEnchantItem(item)) continue;

            for(int level = 1; level < enchantment.getMaxLevel() + 1; level++) {
                int minPower = getVanillaMinModified(enchantment, level);
                int maxPower = getVanillaMaxModified(enchantment, level);
                if(minPower == 0 || maxPower == 0) continue;

                if (modifiedPower >= minPower && modifiedPower <= maxPower) {
                    enchantments.put(enchantment, level);
                }
            }
        }

        //Add all the possible CEnchantments.
        for(CEnchantment enchantment : CEnchantment.getEnchantments()) {
            if(enchantment.isTreasure()) continue;
            if(!enchantment.getEnchantProperties().canEnchantWithTable()) continue;
            if(item.getType() == Material.BOOK) {
                if(!enchantment.getEnchantProperties().canBeOnBook()) continue;
            } else {
                if(!enchantment.canEnchantItem(item)) continue;
            }

            CEnchantProperties properties = enchantment.getEnchantProperties();
            for(int level = 1; level < enchantment.getMaxLevel() + 1; level++) {
                int minPower = properties.getMinModifiedPower(level);
                int maxPower = properties.getMaxModifiedPower(level);
                if(minPower == 0 || maxPower == 0) continue;

                if (modifiedPower >= minPower && modifiedPower <= maxPower) {
                    enchantments.put(enchantment, level);
                }
            }
        }
        return enchantments;
    }

    private static Enchantment chooseEnchantment(List<Enchantment> possibleEnchantments) {
        int totalWeight = 0;

        for(Enchantment possible : possibleEnchantments) {
            if(possible instanceof CEnchantment) {
                CEnchantment cEnchantment = (CEnchantment) possible;

                totalWeight += cEnchantment.getEnchantProperties().getEnchantmentWeight();
            } else {
                totalWeight += getVanillaWeight(possible);
            }
        }

        int w = random.nextInt(totalWeight);
        for(Enchantment possible : possibleEnchantments) {
            int enchantmentWeight;
            if(possible instanceof CEnchantment) {
                enchantmentWeight = ((CEnchantment) possible).getEnchantProperties().getEnchantmentWeight();
            } else {
                enchantmentWeight = getVanillaWeight(possible);
            }

            if((w -= enchantmentWeight) <= 0) {
                return possible;
            }
        }

        return null;
    }

    public static List<Enchantment> chooseEnchantments(List<Enchantment> possible, Enchantment clickedEnchantment, int modifiedPower) {
        List<Enchantment> chosen = new ArrayList<>(Arrays.asList(clickedEnchantment));

        possible.removeIf(possibleEnch -> chosen.stream().anyMatch(chosenEnch -> CEnchantment.conflictsWith(possibleEnch, chosenEnch)));

        double probability = random.nextDouble();
        while(probability < (double) (modifiedPower + 1) / 50 && !possible.isEmpty()) {
            Collections.shuffle(possible);

            //Get the new enchantment
            Enchantment toAdd = chooseEnchantment(possible);
            if(toAdd == null) return chosen;

            //Add another
            chosen.add(toAdd);

            //Update probability
            probability = random.nextDouble();

            //Remove conflicting enchantments from the possible
            possible.removeIf(possibleEnch -> chosen.stream().anyMatch(chosenEnch -> CEnchantment.conflictsWith(possibleEnch, chosenEnch)));

            //Update modified power
            modifiedPower /= 2;
        }

        return chosen;
    }

    private static int getVanillaWeight(Enchantment enchantment) {
        return ((CraftEnchantment) enchantment).getHandle().d().a();
    }

    private static int getVanillaMinModified(Enchantment enchantment, int level) {
        net.minecraft.server.v1_16_R3.Enchantment nmsEnchantment = ((CraftEnchantment) enchantment).getHandle();
        return nmsEnchantment.a(level);
    }

    private static int getVanillaMaxModified(Enchantment enchantment, int level) {
        net.minecraft.server.v1_16_R3.Enchantment nmsEnchantment = ((CraftEnchantment) enchantment).getHandle();
        return nmsEnchantment.b(level);
    }

    private static int getEnchantability(Material material) {
        switch (material) {
            case WOODEN_AXE:
            case WOODEN_HOE:
            case WOODEN_PICKAXE:
            case WOODEN_SHOVEL:
            case WOODEN_SWORD:
            case LEATHER_HELMET:
            case LEATHER_CHESTPLATE:
            case LEATHER_LEGGINGS:
            case LEATHER_BOOTS:
            case NETHERITE_HELMET:
            case NETHERITE_CHESTPLATE:
            case NETHERITE_LEGGINGS:
            case NETHERITE_BOOTS:
            case NETHERITE_AXE:
            case NETHERITE_HOE:
            case NETHERITE_PICKAXE:
            case NETHERITE_SHOVEL:
            case NETHERITE_SWORD:
                return 15;

            case STONE_AXE:
            case STONE_HOE:
            case STONE_PICKAXE:
            case STONE_SHOVEL:
            case STONE_SWORD:
                return 5;

            case CHAINMAIL_HELMET:
            case CHAINMAIL_CHESTPLATE:
            case CHAINMAIL_LEGGINGS:
            case CHAINMAIL_BOOTS:
                return 12;

            case IRON_HELMET:
            case IRON_CHESTPLATE:
            case IRON_LEGGINGS:
            case IRON_BOOTS:
            case TURTLE_HELMET:
                return 9;

            case IRON_AXE:
            case IRON_HOE:
            case IRON_PICKAXE:
            case IRON_SHOVEL:
            case IRON_SWORD:
                return 14;

            case GOLDEN_HELMET:
            case GOLDEN_CHESTPLATE:
            case GOLDEN_LEGGINGS:
            case GOLDEN_BOOTS:
                return 25;

            case GOLDEN_AXE:
            case GOLDEN_HOE:
            case GOLDEN_PICKAXE:
            case GOLDEN_SHOVEL:
            case GOLDEN_SWORD:
                return 22;

            case DIAMOND_HELMET:
            case DIAMOND_CHESTPLATE:
            case DIAMOND_LEGGINGS:
            case DIAMOND_BOOTS:
            case DIAMOND_AXE:
            case DIAMOND_HOE:
            case DIAMOND_PICKAXE:
            case DIAMOND_SHOVEL:
            case DIAMOND_SWORD:
                return 10;

            case BOW:
            case CROSSBOW:
            case TRIDENT:
            case BOOK:
            case FISHING_ROD:
                return 1;

            default: return 0;
        }
    }

}
