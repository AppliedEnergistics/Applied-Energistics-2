package appeng.util.item;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Arrays;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import appeng.api.config.FuzzyMode;

public class FuzzyItemVariantListTest {

    @Test
    void testOrderForDamagedItems() {
        // Diamond Sword @ 100% durability
        ItemStack undamagedSword = new ItemStack(Items.DIAMOND_SWORD);
        AESharedItemStack undamagedStack = new AESharedItemStack(undamagedSword);

        // Unbreakable Diamond Sword @ 50% durability
        ItemStack unbreakableSword = new ItemStack(Items.DIAMOND_SWORD);
        unbreakableSword.setDamage(unbreakableSword.getMaxDamage() / 2);
        unbreakableSword.getOrCreateTag().putBoolean("Unbreakable", true);
        assertFalse(unbreakableSword.isDamageable());
        AESharedItemStack unbreakableStack = new AESharedItemStack(unbreakableSword);

        // Unenchanted Diamond Sword @ 0% durability
        ItemStack damagedSword = new ItemStack(Items.DIAMOND_SWORD);
        damagedSword.setDamage(damagedSword.getMaxDamage());
        AESharedItemStack damagedStack = new AESharedItemStack(damagedSword);

        // Create a list of stacks and sort by their natural order
        AESharedItemStack[] stacks = new AESharedItemStack[] {
                damagedStack, undamagedStack, unbreakableStack
        };
        Arrays.sort(stacks, FuzzyItemVariantList.COMPARATOR);
        assertThat(stacks).containsExactly(damagedStack, unbreakableStack, undamagedStack);
    }

    @Nested
    class Bounds {
        final ItemStack stack = new ItemStack(Items.DIAMOND_SWORD);
        final ItemStack damagedStack;

        {
            damagedStack = stack.copy();
            damagedStack.setDamage(damagedStack.getMaxDamage());
        }

        @Test
        void testIgnoreAll() {
            DamageBounds bounds = new DamageBounds(stack, FuzzyMode.IGNORE_ALL);
            assertEquals(stack.getMaxDamage(), bounds.lower.itemDamage);
            assertEquals(-1, bounds.upper.itemDamage);
        }

        /**
         * Unbreakable items are still considered at their current damage value.
         */
        @Test
        void test99PercentDurabilityWithUnbreakable() {
            ItemStack unbreakableStack = damagedStack.copy();
            unbreakableStack.getOrCreateTag().putBoolean("Unbreakable", true);
            assertFalse(unbreakableStack.isDamageable());

            DamageBounds bounds = new DamageBounds(unbreakableStack, FuzzyMode.PERCENT_99);
            assertEquals(stack.getMaxDamage(), bounds.lower.itemDamage);
            assertEquals(0, bounds.upper.itemDamage);
        }

        /**
         * PERCENT_99 with an undamaged item should select only undamaged items, which translates to a damage range of
         * [0, -1).
         */
        @Test
        void test99PercentDurabilityWithUndamagedItem() {
            DamageBounds bounds = new DamageBounds(stack, FuzzyMode.PERCENT_99);
            assertEquals(0, bounds.lower.itemDamage);
            assertEquals(-1, bounds.upper.itemDamage);
        }

        /**
         * PERCENT_99 with a damaged item should select only damaged items, which translates to a damage range of
         * [maxDmg, 0).
         */
        @Test
        void test99PercentDurabilityWithDamagedItem() {
            DamageBounds bounds = new DamageBounds(damagedStack, FuzzyMode.PERCENT_99);
            assertEquals(stack.getMaxDamage(), bounds.lower.itemDamage);
            assertEquals(0, bounds.upper.itemDamage);
        }

        /**
         * PERCENT_75 with an undamaged item should select items that have 75% or more durability, which should
         * translate to a damage range of [0.25*maxDmg, -1).
         */
        @Test
        void test75PercentWithUndamagedItem() {
            DamageBounds bounds = new DamageBounds(stack, FuzzyMode.PERCENT_75);
            assertEquals((int) (0.25 * stack.getMaxDamage()), bounds.lower.itemDamage);
            assertEquals(-1, bounds.upper.itemDamage);
        }

        /**
         * PERCENT_75 with a damaged item should select items that have less than 75% durability, which should translate
         * to a damage range of [maxDmg, 0.25*maxDmg).
         */
        @Test
        void test75PercentWithDamagedItem() {
            DamageBounds bounds = new DamageBounds(damagedStack, FuzzyMode.PERCENT_75);
            assertEquals(stack.getMaxDamage(), bounds.lower.itemDamage);
            assertEquals((int) (0.25 * stack.getMaxDamage()), bounds.upper.itemDamage);
        }

        /**
         * PERCENT_50 with an undamaged item should select items that have 50% or more durability, which should
         * translate to a damage range of [0.50*maxDmg, -1).
         */
        @Test
        void test50PercentWithUndamagedItem() {
            DamageBounds bounds = new DamageBounds(stack, FuzzyMode.PERCENT_50);
            assertEquals((int) (0.50 * stack.getMaxDamage()), bounds.lower.itemDamage);
            assertEquals(-1, bounds.upper.itemDamage);
        }

        /**
         * PERCENT_50 with a damaged item should select items that have less than 50% durability, which should translate
         * to a damage range of [maxDmg, 0.50*maxDmg).
         */
        @Test
        void test50PercentWithDamagedItem() {
            DamageBounds bounds = new DamageBounds(damagedStack, FuzzyMode.PERCENT_50);
            assertEquals(stack.getMaxDamage(), bounds.lower.itemDamage);
            assertEquals((int) (0.50 * stack.getMaxDamage()), bounds.upper.itemDamage);
        }

        /**
         * PERCENT_25 with an undamaged item should select items that have 25% or more durability, which should
         * translate to a damage range of [0.75*maxDmg, -1).
         */
        @Test
        void test25PercentWithUndamagedItem() {
            DamageBounds bounds = new DamageBounds(stack, FuzzyMode.PERCENT_25);
            assertEquals((int) (0.75 * stack.getMaxDamage()), bounds.lower.itemDamage);
            assertEquals(-1, bounds.upper.itemDamage);
        }

        /**
         * PERCENT_25 with a damaged item should select items that have less than 25% durability, which should translate
         * to a damage range of [maxDmg, 0.75*maxDmg).
         */
        @Test
        void test25PercentWithDamagedItem() {
            DamageBounds bounds = new DamageBounds(damagedStack, FuzzyMode.PERCENT_25);
            assertEquals(stack.getMaxDamage(), bounds.lower.itemDamage);
            assertEquals((int) (0.75 * stack.getMaxDamage()), bounds.upper.itemDamage);
        }

    }

    private static class DamageBounds {
        final FuzzyItemVariantList.ItemDamageBound lower;
        final FuzzyItemVariantList.ItemDamageBound upper;

        public DamageBounds(ItemStack stack, FuzzyMode mode) {
            lower = FuzzyItemVariantList.makeLowerBound(stack, mode);
            upper = FuzzyItemVariantList.makeUpperBound(stack, mode);

            // This may be counter intuitive, but the map is sorted in descending order of item damage
            assertThat(lower.itemDamage).isGreaterThan(upper.itemDamage);
        }
    }

}
