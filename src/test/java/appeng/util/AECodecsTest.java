package appeng.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Comparator;
import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.CustomData;

import appeng.api.ids.AEComponents;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.core.definitions.AEItems;

class AECodecsTest {
    private static final Comparator<ItemStack> STACK_COMPARATOR = Comparator.comparing(stack -> {
        return ItemStack.CODEC.encodeStart(NbtOps.INSTANCE, stack).getOrThrow().toString();
    });

    @Test
    void testMissingContentReplacement() {

        // Encodes a stick
        var stick = Items.STICK.getDefaultInstance();
        var encodedStick = AECodecs.FAULT_TOLERANT_SIMPLE_ITEM_CODEC.encodeStart(NbtOps.INSTANCE, stick).getOrThrow();
        var decodedStick = AECodecs.FAULT_TOLERANT_SIMPLE_ITEM_CODEC.decode(NbtOps.INSTANCE, encodedStick).getOrThrow()
                .getFirst();
        assertTrue(ItemStack.isSameItemSameComponents(stick, decodedStick));

        // Break the encoded itemstack by replacing its ID with an unknown item
        assertEquals(1, RecursiveTagReplace.replace(encodedStick, "minecraft:stick", "unknown_mod:stick"));

        // Try decoding it again. This must not error, since otherwise in lists, the rest would be dropped.
        var decodedMissingContent = AECodecs.FAULT_TOLERANT_SIMPLE_ITEM_CODEC.decode(NbtOps.INSTANCE, encodedStick)
                .getOrThrow().getFirst();
        assertEquals(AEItems.MISSING_CONTENT.asItem(), decodedMissingContent.getItem());
        assertTrue(decodedMissingContent.has(AEComponents.MISSING_CONTENT_ERROR));
        assertTrue(decodedMissingContent.has(AEComponents.MISSING_CONTENT_ITEMSTACK_DATA));

        // When we re-serialize the missing content, it should result in the same broken NBT as it was before
        var reEncodedMissingContent = AECodecs.FAULT_TOLERANT_SIMPLE_ITEM_CODEC
                .encodeStart(NbtOps.INSTANCE, decodedMissingContent).getOrThrow();
        assertEquals(encodedStick, reEncodedMissingContent);
    }

    /**
     * Validates our assumptions about serializing lists. The standard ItemStack codec will cause items in lists to be
     * skipped if they come after a now missing item. Our fault-tolerant codec will instead substitute the item for a
     * missing content item and keep going.
     */
    @Nested
    public class FaultTolerantListCodec {
        Codec<List<ItemStack>> originalCodec = ItemStack.OPTIONAL_CODEC.listOf();
        Codec<List<ItemStack>> faultTolerantCodec = AECodecs.FAULT_TOLERANT_ITEMSTACK_CODEC.listOf();

        // Encode a list of itemstacks
        List<ItemStack> stacks = List.of(
                new ItemStack(Items.STICK),
                new ItemStack(Items.DIAMOND),
                new ItemStack(Items.OAK_PLANKS));

        ListTag encodedUsingOriginalCodec = (ListTag) originalCodec.encodeStart(NbtOps.INSTANCE, stacks).getOrThrow();

        // Same as encodedUsingOriginalCodec, but with diamond replaced by unknown/broken item
        ListTag encodedWithBrokenItem;

        @BeforeEach
        void setUp() {
            encodedWithBrokenItem = encodedUsingOriginalCodec.copy();
            // Now break it by replacing diamonds with unknown_item
            assertEquals(1, RecursiveTagReplace.replace(encodedWithBrokenItem, "minecraft:diamond",
                    "unknown_mod:does_not_exist"));
        }

        @Test
        void testFaultTolerantEncodesToSameResultAsOriginal() {
            var encodedUsingLenientCodec = faultTolerantCodec.encodeStart(NbtOps.INSTANCE, stacks).getOrThrow();
            assertEquals(encodedUsingOriginalCodec, encodedUsingLenientCodec);
        }

        @Test
        void testFaultTolerantCanDecodeOriginalCodecOutput() {
            // Decode unchanged
            var decodedUsingFaultTolerantCodec = faultTolerantCodec.decode(NbtOps.INSTANCE, encodedUsingOriginalCodec)
                    .getOrThrow().getFirst();
            assertThat(decodedUsingFaultTolerantCodec).usingElementComparator(STACK_COMPARATOR).isEqualTo(stacks);
        }

        @Test
        void testMissingContentReplacementInList() {
            // Set up the missing content stack we'd expect
            var brokenItemTag = (CompoundTag) encodedWithBrokenItem.get(1);
            var errorMessage = ItemStack.CODEC.decode(NbtOps.INSTANCE, brokenItemTag).error()
                    .map(DataResult.Error::message).get();
            var expectedMissingContent = AEItems.MISSING_CONTENT.toStack();
            expectedMissingContent.set(AEComponents.MISSING_CONTENT_ITEMSTACK_DATA, CustomData.of(brokenItemTag));
            expectedMissingContent.set(AEComponents.MISSING_CONTENT_ERROR, errorMessage);

            // Using the fault-tolerant codec will give us the full list, but with diamond replaced
            var decodedBrokenUsingFaultTolerantCodec = faultTolerantCodec.decode(NbtOps.INSTANCE, encodedWithBrokenItem)
                    .getPartialOrThrow()
                    .getFirst();
            assertThat(decodedBrokenUsingFaultTolerantCodec).usingElementComparator(STACK_COMPARATOR).containsExactly(
                    new ItemStack(Items.STICK),
                    expectedMissingContent,
                    new ItemStack(Items.OAK_PLANKS));
        }

        /**
         * When a missing content item is encoded using our fault-tolerant codec, it will result in the original, broken
         * data. This helps if a player uninstalls a mod *temporarily*, and then re-installs it.
         */
        @Test
        void testReserializationResultsInOriginalBrokenData() {
            // Using the fault-tolerant codec will give us the full list, but with diamond replaced
            var decoded = faultTolerantCodec.decode(NbtOps.INSTANCE, encodedWithBrokenItem).getPartialOrThrow()
                    .getFirst();
            var reencoded = faultTolerantCodec.encodeStart(NbtOps.INSTANCE, decoded).result().get();
            assertEquals(encodedWithBrokenItem, reencoded);
        }
    }

    @Nested
    class FaultTolerantGenericStackCodec {
        @Test
        void testFaultTolerantSerialization() {
            // Construct a stack that will fail Serialization due to exceeding the maximum stack size
            var stack = new ItemStack(Items.STICK);
            stack.set(DataComponents.BUNDLE_CONTENTS, new BundleContents(List.of(
                    new ItemStack(Items.STICK, 999))));

            // Validate our assumption that this throws
            assertThrows(Exception.class, () -> stack.save(RegistryAccess.EMPTY));

            // Build a generic stack out of this item
            var listInput = List.of(
                    GenericStack.fromItemStack(AEItems.FORMATION_CORE.toStack()),
                    GenericStack.fromItemStack(stack),
                    GenericStack.fromItemStack(AEItems.ANNIHILATION_CORE.toStack()));
            var encodedList = GenericStack.FAULT_TOLERANT_LIST_CODEC.encodeStart(NbtOps.INSTANCE, listInput)
                    .getOrThrow();

            // Now decode again and assert the broken stick was replaced by a missing content item
            var stackList = GenericStack.FAULT_TOLERANT_LIST_CODEC.decode(NbtOps.INSTANCE, encodedList)
                    .getOrThrow().getFirst();

            // No item should be missing
            assertThat(stackList).hasSize(3);

            // The two unaffected items should be untouched
            assertThat(stackList.get(0)).isEqualTo(GenericStack.fromItemStack(AEItems.FORMATION_CORE.toStack()));
            assertThat(stackList.get(2)).isEqualTo(GenericStack.fromItemStack(AEItems.ANNIHILATION_CORE.toStack()));

            // Assert that the missing content item is as we expect
            var brokenStack = stackList.get(1);
            assertNotNull(brokenStack);
            assertEquals(1, brokenStack.amount());
            var resultItemKey = assertInstanceOf(AEItemKey.class, brokenStack.what());
            assertSame(AEItems.MISSING_CONTENT.asItem(), resultItemKey.getItem());
            assertEquals("Value must be within range [1;99]: 999",
                    resultItemKey.get(AEComponents.MISSING_CONTENT_ERROR));
        }

        @Test
        void testFaultTolerantDeserialization() {
            // Use a known broken NBT to deserialize a generic stack list from by injecting an unknown item id
            // Build a generic stack out of this item
            var listInput = List.of(
                    GenericStack.fromItemStack(AEItems.FORMATION_CORE.toStack()),
                    GenericStack.fromItemStack(new ItemStack(Items.DIAMOND)),
                    GenericStack.fromItemStack(AEItems.ANNIHILATION_CORE.toStack()));
            var encodedList = GenericStack.FAULT_TOLERANT_LIST_CODEC.encodeStart(NbtOps.INSTANCE, listInput)
                    .getOrThrow();

            // Now break it by replacing diamonds with unknown_item
            assertEquals(1, RecursiveTagReplace.replace(encodedList, "minecraft:diamond",
                    "unknown_mod:does_not_exist"));

            // Now decode again and assert the broken stick was replaced by a missing content item
            var stackList = GenericStack.FAULT_TOLERANT_LIST_CODEC.decode(NbtOps.INSTANCE, encodedList)
                    .getOrThrow().getFirst();

            // No item should be missing
            assertThat(stackList).hasSize(3);

            // The two unaffected items should be untouched
            assertThat(stackList.get(0)).isEqualTo(GenericStack.fromItemStack(AEItems.FORMATION_CORE.toStack()));
            assertThat(stackList.get(2)).isEqualTo(GenericStack.fromItemStack(AEItems.ANNIHILATION_CORE.toStack()));

            // Assert that the missing content item is as we expect
            var brokenStack = stackList.get(1);
            assertNotNull(brokenStack);
            assertEquals(1, brokenStack.amount());
            var resultItemKey = assertInstanceOf(AEItemKey.class, brokenStack.what());
            assertSame(AEItems.MISSING_CONTENT.asItem(), resultItemKey.getItem());
            assertEquals(
                    "Unknown registry key in ResourceKey[minecraft:root / minecraft:item]: unknown_mod:does_not_exist",
                    resultItemKey.get(AEComponents.MISSING_CONTENT_ERROR));
        }
    }
}
