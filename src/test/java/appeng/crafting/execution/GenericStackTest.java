package appeng.crafting.execution;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.items.misc.WrappedGenericStack;
import appeng.util.BootstrapMinecraft;
import appeng.util.CodecTestUtil;
import com.mojang.serialization.JsonOps;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@BootstrapMinecraft
class GenericStackTest {
    @Test
    void testItemJsonRoundtrip() {
        var expected = GsonHelper.parse("{\"id\":\"minecraft:diamond\",\"#c\":\"ae2:i\",\"amount\":9223372036854775807}");

        var ik = AEItemKey.of(Items.DIAMOND);
        var gs = new GenericStack(ik, Long.MAX_VALUE);
        CodecTestUtil.testRoundtrip(GenericStack.CODEC, gs, JsonOps.INSTANCE, expected);
    }

    @Test
    void testNullableListRoundtrip() {
        var expected = GsonHelper.parseArray("[{},{\"id\":\"minecraft:diamond\",\"#c\":\"ae2:i\",\"amount\":1000}]");

        var list = new ArrayList<GenericStack>();
        list.add(null);
        list.add(new GenericStack(AEItemKey.of(Items.DIAMOND), 1000));
        CodecTestUtil.testRoundtrip(GenericStack.NULLABLE_LIST_CODEC, list, JsonOps.INSTANCE, expected);
    }

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
