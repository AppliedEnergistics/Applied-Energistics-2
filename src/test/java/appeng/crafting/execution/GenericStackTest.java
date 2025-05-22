package appeng.crafting.execution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.items.misc.WrappedGenericStack;
import appeng.util.BootstrapMinecraft;

@BootstrapMinecraft
class GenericStackTest {
    @Nested
    class Wrapping {
        @Test
        void wrapWater() {
            var water = new GenericStack(AEFluidKey.of(Fluids.WATER), Long.MAX_VALUE);

            ItemStack wrapped = GenericStack.wrapInItemStack(water);
            assertValidWrapped(wrapped);

            assertEquals(water, GenericStack.unwrapItemStack(wrapped));
        }

        @Test
        void wrapItemAmountOfZero() {
            var zeroCobble = new GenericStack(AEItemKey.of(Items.COBBLESTONE), 0);

            ItemStack wrapped = GenericStack.wrapInItemStack(zeroCobble);
            assertValidWrapped(wrapped);

            assertEquals(zeroCobble, GenericStack.unwrapItemStack(wrapped));
        }

        private void assertValidWrapped(ItemStack wrapped) {
            assertFalse(wrapped.isEmpty());
            assertInstanceOf(WrappedGenericStack.class, wrapped.getItem());
            assertEquals(1, wrapped.getCount());
        }
    }
}
