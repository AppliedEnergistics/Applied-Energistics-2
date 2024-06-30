package appeng.crafting.execution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.ArrayList;

import com.mojang.serialization.JsonOps;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.core.definitions.AEItems;
import appeng.items.misc.WrappedGenericStack;
import appeng.util.BootstrapMinecraft;
import appeng.util.CodecTestUtil;
import appeng.util.RecursiveTagReplace;

@BootstrapMinecraft
class GenericStackTest {
    @Test
    void testItemJsonRoundtrip() {
        var expected = GsonHelper.parse("{\"id\":\"minecraft:diamond\",\"#t\":\"ae2:i\",\"#\":9223372036854775807}");

        var ik = AEItemKey.of(Items.DIAMOND);
        var gs = new GenericStack(ik, Long.MAX_VALUE);
        CodecTestUtil.testRoundtrip(GenericStack.CODEC, gs, JsonOps.INSTANCE, expected);
    }

    @Test
    void testNullableListRoundtrip() {
        var expected = GsonHelper.parseArray("[{},{\"id\":\"minecraft:diamond\",\"#t\":\"ae2:i\",\"#\":1000}]");

        var list = new ArrayList<GenericStack>();
        list.add(null);
        list.add(new GenericStack(AEItemKey.of(Items.DIAMOND), 1000));
        CodecTestUtil.testRoundtrip(GenericStack.FAULT_TOLERANT_NULLABLE_LIST_CODEC, list, JsonOps.INSTANCE, expected);
    }

    @Nested
    class MissingContent {
        GenericStack baseStack = new GenericStack(AEItemKey.of(Items.STICK), 1000);

        CompoundTag serialized = (CompoundTag) GenericStack.CODEC.encodeStart(NbtOps.INSTANCE, baseStack).getOrThrow();

        @Test
        void testMissingContentForUnknownChannel() {
            assertEquals(1, RecursiveTagReplace.replace(serialized, "ae2:i", "invalid_channel_id"));
            var stack = deserialize();
            assertSame(AEItems.MISSING_CONTENT.asItem(), assertInstanceOf(AEItemKey.class, stack.what()).getItem());
            assertEquals(baseStack.amount(), stack.amount()); // don't lose the amount
        }

        @Test
        void testMissingContentForUnknownId() {
            assertEquals(1, RecursiveTagReplace.replace(serialized, "minecraft:stick", "invalid_item_id"));
            var stack = deserialize();
            assertSame(AEItems.MISSING_CONTENT.asItem(), assertInstanceOf(AEItemKey.class, stack.what()).getItem());
            assertEquals(baseStack.amount(), stack.amount()); // don't lose the amount
        }

        private GenericStack deserialize() {
            return GenericStack.CODEC.decode(NbtOps.INSTANCE, serialized).getOrThrow().getFirst();
        }

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
