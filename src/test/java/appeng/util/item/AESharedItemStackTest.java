package appeng.util.item;

import appeng.api.config.FuzzyMode;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.registry.Bootstrap;
import net.minecraft.util.text.StringTextComponent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AESharedItemStackTest {

    @BeforeAll
    static void bootstrap() {
        Bootstrap.register();
    }

    private final TestItemWithCaps TEST_ITEM = new TestItemWithCaps();

    /**
     * Creates a bunch of item stacks that should all be considered not-equal, and then performs a sanity check
     * on compareTo and equals.
     */
    @Test
    void testCompareTo() {
        // Test stack -> Name for debuggin the tests
        Map<AESharedItemStack, String> stacks = new IdentityHashMap<>();

        ItemStack nameTag1 = new ItemStack(TEST_ITEM);
        stacks.put(new AESharedItemStack(nameTag1), "no-nbt");

        // NBT
        ItemStack nameTag2 = new ItemStack(TEST_ITEM);
        nameTag2.setDisplayName(new StringTextComponent("Hello World"));
        stacks.put(new AESharedItemStack(nameTag2), "nbt1");

        // Different NBT
        ItemStack nameTag3 = new ItemStack(TEST_ITEM);
        nameTag3.setDisplayName(new StringTextComponent("ABCDEFGH"));
        stacks.put(new AESharedItemStack(nameTag3), "nbt2");

        // NBT + Cap
        CompoundNBT capNbt = new CompoundNBT();
        capNbt.putInt("Parent", 1);
        ItemStack nameTag4 = new ItemStack(TEST_ITEM, 1, capNbt);
        nameTag4.setDisplayName(new StringTextComponent("Hello World"));
        stacks.put(new AESharedItemStack(nameTag4), "nbt1+cap1");

        // NBT + Different Cap
        CompoundNBT capNbt2 = new CompoundNBT();
        capNbt2.putInt("Parent", 123);
        ItemStack nameTag5 = new ItemStack(TEST_ITEM, 1, capNbt2);
        nameTag5.setDisplayName(new StringTextComponent("Hello World"));
        stacks.put(new AESharedItemStack(nameTag5), "nbt1+cap2");

        // Start by sanity checking compareTo & equals
        for (AESharedItemStack stack : stacks.keySet()) {
            for (AESharedItemStack otherStack : stacks.keySet()) {
                String stackName = stacks.get(stack);
                String otherStackName = stacks.get(otherStack);

                if (stack == otherStack) {
                    assertThat(stack).as("%s.compareTo(%s) == 0", stackName, stackName)
                            .isEqualByComparingTo(otherStack);
                    assertThat(stack).as("%s.equals(%s)", stackName, stackName)
                            .isEqualTo(otherStack);
                } else {
                    assertThat(stack)
                            .as("%s.compareTo(%s) != 0", stackName, otherStackName)
                            .isNotEqualByComparingTo(otherStack);
                    assertThat(stack)
                            .as("!%s.equals(%s)", stackName, otherStackName)
                            .isNotEqualTo(otherStack);
                }
            }
        }
    }

    @Test
    void testCompareToForDamagedItems() {
        // Diamond Sword @ 100% durability
        ItemStack undamagedSword = new ItemStack(Items.DIAMOND_SWORD);
        AESharedItemStack undamagedStack = new AESharedItemStack(undamagedSword);

        // Unenchanted Diamond Sword @ 0% durability
        ItemStack damagedSword = new ItemStack(Items.DIAMOND_SWORD);
        damagedSword.setDamage(damagedSword.getMaxDamage());
        AESharedItemStack damagedStack = new AESharedItemStack(damagedSword);

        // Create a list of stacks and sort by their natural order
        AESharedItemStack[] stacks = new AESharedItemStack[]{
                damagedStack, undamagedStack
        };
        Arrays.sort(stacks);
        assertThat(stacks).containsExactly(damagedStack, undamagedStack);
    }

    @Test
    void testCompareToWithBounds() {

    }

    private AESharedItemStack diamondSword(int damage, String displayName) {
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        sword.setDamage(damage);
        if (displayName != null) {
            sword.setDisplayName(new StringTextComponent(displayName));
        }
        return new AESharedItemStack(sword);
    }

    @Nested
    class Bounds {
        final ItemStack vanillaStack = new ItemStack(Items.DIAMOND_SWORD);
        final AESharedItemStack stack = new AESharedItemStack(vanillaStack);
        final AESharedItemStack damagedStack;
        {
            ItemStack damagedVanillaStack = vanillaStack.copy();
            damagedVanillaStack.setDamage(damagedVanillaStack.getMaxDamage());
            damagedStack = new AESharedItemStack(damagedVanillaStack);
        }

        @Test
        void testIgnoreAll() {
            AESharedItemStack.Bounds bounds = stack.getBounds(FuzzyMode.IGNORE_ALL);
            assertBoundsCompareTo(bounds);
            assertEquals(vanillaStack.getMaxDamage(), bounds.lower().getItemDamage());
            assertEquals(-1, bounds.upper().getItemDamage());
        }

        /**
         * PERCENT_99 with an undamaged item should select only undamaged items,
         * which translates to a damage range of [0, -1).
         */
        @Test
        void test99PercentDurabilityWithUndamagedItem() {
            AESharedItemStack.Bounds bounds = stack.getBounds(FuzzyMode.PERCENT_99);
            assertBoundsCompareTo(bounds);
            assertEquals(0, bounds.lower().getItemDamage());
            assertEquals(-1, bounds.upper().getItemDamage());
        }

        /**
         * PERCENT_99 with a damaged item should select only damaged items,
         * which translates to a damage range of [maxDmg, 0).
         */
        @Test
        void test99PercentDurabilityWithDamagedItem() {
            AESharedItemStack.Bounds bounds = damagedStack.getBounds(FuzzyMode.PERCENT_99);
            assertBoundsCompareTo(bounds);
            assertEquals(vanillaStack.getMaxDamage(), bounds.lower().getItemDamage());
            assertEquals(0, bounds.upper().getItemDamage());
        }

        /**
         * PERCENT_75 with an undamaged item should select items that have 75% or more
         * durability, which should translate to a damage range of [0.25*maxDmg, -1).
         */
        @Test
        void test75PercentWithUndamagedItem() {
            AESharedItemStack.Bounds bounds = stack.getBounds(FuzzyMode.PERCENT_75);
            assertBoundsCompareTo(bounds);
            assertEquals((int)(0.25 * vanillaStack.getMaxDamage()), bounds.lower().getItemDamage());
            assertEquals(-1, bounds.upper().getItemDamage());
        }

        /**
         * PERCENT_75 with a damaged item should select items that have less than 75%
         * durability, which should translate to a damage range of [maxDmg, 0.25*maxDmg).
         */
        @Test
        void test75PercentWithDamagedItem() {
            AESharedItemStack.Bounds bounds = damagedStack.getBounds(FuzzyMode.PERCENT_75);
            assertBoundsCompareTo(bounds);
            assertEquals(vanillaStack.getMaxDamage(), bounds.lower().getItemDamage());
            assertEquals((int)(0.25 * vanillaStack.getMaxDamage()), bounds.upper().getItemDamage());
        }

        /**
         * PERCENT_50 with an undamaged item should select items that have 50% or more
         * durability, which should translate to a damage range of [0.50*maxDmg, -1).
         */
        @Test
        void test50PercentWithUndamagedItem() {
            AESharedItemStack.Bounds bounds = stack.getBounds(FuzzyMode.PERCENT_50);
            assertBoundsCompareTo(bounds);
            assertEquals((int)(0.50 * vanillaStack.getMaxDamage()), bounds.lower().getItemDamage());
            assertEquals(-1, bounds.upper().getItemDamage());
        }

        /**
         * PERCENT_50 with a damaged item should select items that have less than 50%
         * durability, which should translate to a damage range of [maxDmg, 0.50*maxDmg).
         */
        @Test
        void test50PercentWithDamagedItem() {
            AESharedItemStack.Bounds bounds = damagedStack.getBounds(FuzzyMode.PERCENT_50);
            assertBoundsCompareTo(bounds);
            assertEquals(vanillaStack.getMaxDamage(), bounds.lower().getItemDamage());
            assertEquals((int)(0.50 * vanillaStack.getMaxDamage()), bounds.upper().getItemDamage());
        }
        
        /**
         * PERCENT_25 with an undamaged item should select items that have 25% or more
         * durability, which should translate to a damage range of [0.75*maxDmg, -1).
         */
        @Test
        void test25PercentWithUndamagedItem() {
            AESharedItemStack.Bounds bounds = stack.getBounds(FuzzyMode.PERCENT_25);
            assertBoundsCompareTo(bounds);
            assertEquals((int)(0.75 * vanillaStack.getMaxDamage()), bounds.lower().getItemDamage());
            assertEquals(-1, bounds.upper().getItemDamage());
        }

        /**
         * PERCENT_25 with a damaged item should select items that have less than 25%
         * durability, which should translate to a damage range of [maxDmg, 0.75*maxDmg).
         */
        @Test
        void test25PercentWithDamagedItem() {
            AESharedItemStack.Bounds bounds = damagedStack.getBounds(FuzzyMode.PERCENT_25);
            assertBoundsCompareTo(bounds);
            assertEquals(vanillaStack.getMaxDamage(), bounds.lower().getItemDamage());
            assertEquals((int)(0.75 * vanillaStack.getMaxDamage()), bounds.upper().getItemDamage());
        }

        private void assertBoundsCompareTo(AESharedItemStack.Bounds bounds) {
            assertTrue(bounds.lower().compareTo(bounds.upper()) < 0); // lower should be less than upper
            assertTrue(bounds.upper().compareTo(bounds.lower()) > 0); // upper should be greater than lower
        }
    }

}