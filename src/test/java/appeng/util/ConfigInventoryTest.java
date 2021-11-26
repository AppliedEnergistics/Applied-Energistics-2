package appeng.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;

import appeng.api.storage.GenericStack;
import appeng.api.storage.data.AEFluidKey;
import appeng.api.storage.data.AEItemKey;
import appeng.helpers.iface.GenericStackInv;

@BootstrapMinecraft
public class ConfigInventoryTest {
    public static final AEItemKey STICK_KEY = AEItemKey.of(Items.STICK);
    public static final GenericStack ONE_STICK = new GenericStack(STICK_KEY, 1);
    public static final GenericStack ZERO_STICK = new GenericStack(STICK_KEY, 0);

    /**
     * The underlying inventory can be deserialized from NBT, and in general this can lead to keys from other channels
     * being deserialized. The getters need to account for this and filter out the respective keys and stacks.
     */
    @Nested
    class ChannelFiltering {
        ConfigInventory inv = ConfigInventory.configStacks(AEItemKey.filter(), 2, null);

        @BeforeEach
        void loadMixedStacks() {
            var mixedInv = new GenericStackInv(null, 2);
            mixedInv.setStack(0, ONE_STICK);
            mixedInv.setStack(1, new GenericStack(AEFluidKey.of(Fluids.WATER), 1));
            inv.readFromTag(mixedInv.writeToTag());
        }

        @Test
        void testGetStackOnlyReturnsItems() {
            assertEquals(ONE_STICK, inv.getStack(0));
            assertNull(inv.getStack(1));
        }

        @Test
        void getGetKeyOnlyReturnsItems() {
            assertEquals(STICK_KEY, inv.getKey(0));
            assertNull(inv.getKey(1));
        }
    }

    @Nested
    class TypesMode {
        ConfigInventory inv = ConfigInventory.configTypes(AEItemKey.filter(), 1, null);

        @Test
        void amountZeroIsAllowed() {
            inv.setStack(0, ZERO_STICK);
            assertEquals(ZERO_STICK, inv.getStack(0));
        }

        @Test
        void otherAmountsAreSetToZero() {
            inv.setStack(0, ONE_STICK);
            assertEquals(ZERO_STICK, inv.getStack(0));
        }
    }

    @Nested
    class StacksMode {
        ConfigInventory inv = ConfigInventory.configStacks(AEItemKey.filter(), 1, null);

        @Test
        void stacksWithAmountZeroAreDropped() {
            inv.setStack(0, ZERO_STICK);
            assertNull(inv.getStack(0));
        }

        @Test
        void stacksWithNegativeAmountsAreDropped() {
            inv.setStack(0, new GenericStack(STICK_KEY, -1000));
            assertNull(inv.getStack(0));
        }

        @Test
        void amountZeroDropsExistingStack() {
            inv.setStack(0, ONE_STICK);
            assertEquals(ONE_STICK, inv.getStack(0));
            inv.setStack(0, ZERO_STICK);
            assertNull(inv.getStack(0));
        }

        @Test
        void positiveAmountsAreKept() {
            GenericStack stack = new GenericStack(STICK_KEY, 1000);
            inv.setStack(0, stack);
            assertEquals(stack, inv.getStack(0));
            assertEquals(1000, inv.getAmount(0));
        }
    }
}
