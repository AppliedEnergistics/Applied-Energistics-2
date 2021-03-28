package appeng.util.item;

import appeng.api.config.FuzzyMode;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AESharedItemStackTest {

    @Nested
    class Bounds {
        final ItemStack vanillaStack = new ItemStack(Items.DIAMOND_SWORD);
        final AESharedItemStack stack = new AESharedItemStack(vanillaStack);

        @Test
        void testIgnoreAll() {
            AESharedItemStack.Bounds bounds = stack.getBounds(FuzzyMode.IGNORE_ALL);
            assertEquals(vanillaStack.getMaxDamage(), bounds.upper().getItemDamage());
            assertEquals(-1, bounds.lower().getItemDamage());
        }

        @Test
        void test99PercentDurabilityOrLess() {
            AESharedItemStack.Bounds bounds = stack.getBounds(FuzzyMode.PERCENT_99);
            assertEquals((int)(0.99 * vanillaStack.getMaxDamage()), bounds.upper().getItemDamage());
            assertEquals(-1, bounds.lower().getItemDamage());
        }

        @Test
        void test75PercentDurabilityOrLess() {
            AESharedItemStack.Bounds bounds = stack.getBounds(FuzzyMode.PERCENT_75);
            assertEquals((int)(0.75 * vanillaStack.getMaxDamage()), bounds.upper().getItemDamage());
            assertEquals(-1, bounds.lower().getItemDamage());
        }

        @Test
        void test50PercentDurabilityOrLess() {
            AESharedItemStack.Bounds bounds = stack.getBounds(FuzzyMode.PERCENT_50);
            assertEquals((int)(0.50 * vanillaStack.getMaxDamage()), bounds.upper().getItemDamage());
            assertEquals(-1, bounds.lower().getItemDamage());
        }

        @Test
        void test25PercentDurabilityOrLess() {
            AESharedItemStack.Bounds bounds = stack.getBounds(FuzzyMode.PERCENT_25);
            assertEquals((int)(0.25 * vanillaStack.getMaxDamage()), bounds.upper().getItemDamage());
            assertEquals(-1, bounds.lower().getItemDamage());
        }
    }

}