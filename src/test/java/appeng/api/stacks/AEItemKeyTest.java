package appeng.api.stacks;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;

import appeng.api.config.FuzzyMode;
import appeng.util.BootstrapMinecraft;

@BootstrapMinecraft
class AEItemKeyTest {
    @Test
    void testFuzzySearchValues() {
        var undamaged = AEItemKey.of(Items.DIAMOND_PICKAXE);
        var damagedStack = undamaged.toStack();
        damagedStack.setDamageValue(undamaged.getItem().getMaxDamage());
        var damaged = AEItemKey.of(damagedStack);

        assertEquals(damaged.getFuzzySearchMaxValue(), Items.DIAMOND_PICKAXE.getMaxDamage());
        assertEquals(damaged.getFuzzySearchValue(), Items.DIAMOND_PICKAXE.getMaxDamage());
        assertEquals(undamaged.getFuzzySearchValue(), 0);
    }

    @ParameterizedTest
    @CsvSource({
            "100,100,IGNORE_ALL,true",
            "100,100,PERCENT_99,true",
            "100,100,PERCENT_75,true",
            "100,100,PERCENT_50,true",
            "100,100,PERCENT_25,true",
            "100,99,PERCENT_99,false",
            "99,0,PERCENT_99,true",
            "100,99,PERCENT_99,false",
            "99,0,PERCENT_99,true",
            "100,75,PERCENT_75,true",
            "100,74,PERCENT_75,false",
            "75,74,PERCENT_75,false",
            "74,0,PERCENT_75,true",
            "100,50,PERCENT_50,true",
            "100,49,PERCENT_50,false",
            "50,49,PERCENT_50,false",
            "49,0,PERCENT_50,true",
            "100,25,PERCENT_25,true",
            "100,24,PERCENT_25,false",
            "25,24,PERCENT_25,false",
            "24,0,PERCENT_25,true",
    })
    void testFuzzyEquals(int durabilityPercentA, int durabilityPercentB, FuzzyMode mode, boolean equals) {
        var stackA = new ItemStack(Items.DIAMOND_PICKAXE);
        setDamageFromPercentage(stackA, durabilityPercentA);
        var keyA = AEItemKey.of(stackA);
        var stackB = new ItemStack(Items.DIAMOND_PICKAXE);
        setDamageFromPercentage(stackB, durabilityPercentB);
        var keyB = AEItemKey.of(stackB);

        assertEquals(equals, keyA.fuzzyEquals(keyB, mode));
        assertEquals(equals, keyB.fuzzyEquals(keyA, mode));
    }

    /**
     * This tests that the fuzzy search works the same for {@link FuzzySearch} and {@link AEKey#fuzzyEquals}.
     */
    @ParameterizedTest
    @EnumSource(value = FuzzyMode.class, mode = EnumSource.Mode.EXCLUDE, names = "IGNORE_ALL")
    void testConsistencyWithFuzzySearch(FuzzyMode mode) {
        var keys = new KeyCounter();
        for (var i = 0; i <= Items.IRON_PICKAXE.getMaxDamage(); i++) {
            var stack = new ItemStack(Items.IRON_PICKAXE);
            stack.setDamageValue(i);
            keys.set(AEItemKey.of(stack), 1);
        }

        var undamaged = new ItemStack(Items.IRON_PICKAXE);
        var fullyDamaged = new ItemStack(Items.IRON_PICKAXE);
        fullyDamaged.setDamageValue(fullyDamaged.getMaxDamage());

        // Get the upper partition
        var upperKeys = keys.findFuzzy(AEItemKey.of(undamaged), mode)
                .stream().map(Map.Entry::getKey).toList();
        var lowerKeys = keys.findFuzzy(AEItemKey.of(fullyDamaged), mode)
                .stream().map(Map.Entry::getKey).toList();

        // Given AEKey.fuzzyEquals, all upperKeys should be equal to each other, and unequal to all lower keys
        for (int i = 0; i < upperKeys.size(); i++) {
            var key = upperKeys.get(i);
            var unequals = upperKeys.stream().filter(ok -> !ok.fuzzyEquals(key, mode)).toList();
            assertThat(unequals).isEmpty();

            var equals = lowerKeys.stream().filter(ok -> ok.fuzzyEquals(key, mode)).toList();
            assertThat(equals).isEmpty();
        }
        for (int i = 0; i < lowerKeys.size(); i++) {
            var key = lowerKeys.get(i);
            var unequals = lowerKeys.stream().filter(ok -> !ok.fuzzyEquals(key, mode)).toList();
            assertThat(unequals).isEmpty();

            var equals = upperKeys.stream().filter(ok -> ok.fuzzyEquals(key, mode)).toList();
            assertThat(equals).isEmpty();
        }
    }

    private void setDamageFromPercentage(ItemStack stack, int percentage) {
        stack.setDamageValue((int) ((1.0f - (percentage / 100.0f)) * (float) stack.getMaxDamage()));
    }

    @Test
    void testFuzzyEqualsDifferentItems() {
        // Different items are not fuzzy equals
        assertFalse(AEItemKey.of(Items.DIAMOND).fuzzyEquals(AEItemKey.of(Items.DIAMOND_PICKAXE), FuzzyMode.IGNORE_ALL));
    }

    @Test
    void testFuzzyEqualsDifferentNbt() {
        var pick1 = new ItemStack(Items.DIAMOND_PICKAXE);
        var pick2 = new ItemStack(Items.DIAMOND_PICKAXE);
        pick2.enchant(Enchantments.BLOCK_FORTUNE, 2);
        assertNotEquals(pick1.getTag(), pick2.getTag());

        assertTrue(AEItemKey.of(pick1).fuzzyEquals(AEItemKey.of(pick2), FuzzyMode.IGNORE_ALL));
    }

    @Nested
    class GenericTagSerialization {
        @Test
        void deserializeFromTagWithoutChannel() {
            assertNull(AEKey.fromTagGeneric(new CompoundTag()));
        }

        @Test
        void deserializeFromTagWithUnknownChannelId() {
            var tag = new CompoundTag();
            tag.putString("#c", "modid:doesnt_exist");

            assertNull(AEKey.fromTagGeneric(tag));
        }

        @Test
        void deserializeFromTagWithMalformedChannelId() {
            var tag = new CompoundTag();
            tag.putString("#c", "modid!!!!!doesnt_exist");

            assertNull(AEKey.fromTagGeneric(tag));
        }
    }

    @Test
    void testIsDamaged() {
        assertFalse(AEItemKey.of(Blocks.BEDROCK).isDamaged());
        assertFalse(AEItemKey.of(Items.DIAMOND_PICKAXE).isDamaged());

        var stack = new ItemStack(Items.DIAMOND_PICKAXE);
        stack.setDamageValue(0);
        assertFalse(AEItemKey.of(stack).isDamaged());

        stack.setDamageValue(1);
        assertTrue(AEItemKey.of(stack).isDamaged());
    }

    /**
     * Regression test for {@link FuzzySearch#COMPARATOR} wrongly using AEKey identity comparison as a last resort.
     */
    @Test
    void testDifferentInstances() {
        int testCount = 100;
        while (testCount-- > 0) {

            final int COUNT = 5;
            AEKey[] keys = new AEKey[COUNT];
            AEKey[] keyCopies = new AEKey[COUNT];

            for (int i = 0; i < COUNT; i++) {
                var stack = new ItemStack(Items.DIAMOND_SWORD);
                stack.enchant(Enchantments.SHARPNESS, i + 1);
                keys[i] = AEItemKey.of(stack);
                keyCopies[i] = AEItemKey.of(stack);

                // If we ever intern AEKeys, remember to update this test...
                assertThat(keys[i]).isNotSameAs(keyCopies[i]);
            }

            var counter = new KeyCounter();
            for (int i = 0; i < COUNT; i++) {
                counter.set(keys[i], 1);
            }

            for (int i = 0; i < COUNT; i++) {
                assertThat(counter.get(keyCopies[i])).isEqualTo(1);
            }

        }
    }
}
