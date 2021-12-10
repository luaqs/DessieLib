package me.dessie.dessielib.enchantmentapi.properties;

import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;

import java.util.Arrays;
import java.util.List;

public enum Rarity {

    //Modified Power levels for vanilla me.dessie.dessielib.enchantments can be found at
    //https://minecraft.fandom.com/wiki/Enchanting/Levels
    //Enchantment weights can be found at
    //https://minecraft.fandom.com/wiki/Enchanting_mechanics#Step_three_.E2.80.93_Select_a_set_of_enchantments_from_the_list

    //Mimics rarity of Sharpness

    COMMON(10, Arrays.asList(
            new ImmutableTriple<>(1, 1, 21),
            new ImmutableTriple<>(2, 12, 32),
            new ImmutableTriple<>(3, 23, 43),
            new ImmutableTriple<>(4, 34, 54),
            new ImmutableTriple<>(5, 45, 65))),

    //Mimics the rarity of Smite/Bane of Arthropods
    UNCOMMON(5, Arrays.asList(
            new ImmutableTriple<>(1, 5, 25),
            new ImmutableTriple<>(2, 13, 33),
            new ImmutableTriple<>(3, 21, 41),
            new ImmutableTriple<>(4, 29, 49),
            new ImmutableTriple<>(5, 37, 57))),

    //Mimics the rarity of Respiration
    RARE(2, Arrays.asList(
            new ImmutableTriple<>(1, 10, 40),
            new ImmutableTriple<>(2, 20, 50),
            new ImmutableTriple<>(3, 30, 60),
            //These two are just following the pattern, since Respiration
            //doesn't support lvls 4 and 5.
            new ImmutableTriple<>(4, 40, 70),
            new ImmutableTriple<>(5, 50, 80))),

    //Mimics the rarity of Thorns
    VERY_RARE(1, Arrays.asList(
            new ImmutableTriple<>(1, 10, 61),
            new ImmutableTriple<>(2, 30, 71),
            new ImmutableTriple<>(3, 50, 81),
            //These two are just following the pattern, since Thorns
            //doesn't support lvls 4 and 5.
            new ImmutableTriple<>(4, 60, 91),
            new ImmutableTriple<>(5, 70, 101)));


    private int weight;
    private List<Triple<Integer, Integer, Integer>> powers;

    Rarity(int weight, List<Triple<Integer, Integer, Integer>> powers) {
        this.weight = weight;
        this.powers = powers;
    }

    public int getWeight() {
        return this.weight;
    }

    public int getMinPower(int level) {
        Triple<Integer, Integer, Integer> trip = this.powers.stream().filter(triple -> triple.getLeft() == level)
                .findAny().orElse(null);
        return trip == null ? 0 : trip.getMiddle();
    }

    public int getMaxPower(int level) {
        Triple<Integer, Integer, Integer> trip = this.powers.stream().filter(triple -> triple.getLeft() == level)
                .findAny().orElse(null);
        return trip == null ? 0 : trip.getRight();
    }

}
