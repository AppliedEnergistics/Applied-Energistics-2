package appeng.crafting.pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.storage.GenericStack;
import appeng.api.storage.data.AEItemKey;
import appeng.core.definitions.AEItems;
import appeng.util.BootstrapMinecraft;

@BootstrapMinecraft
class ProcessingPatternItemTest {
    @Test
    void testDecodeWithEmptyTag() {
        assertNull(decode(new CompoundTag()));
    }

    /**
     * Sanity check that an encoded pattern that contains item-ids that are now invalid (i.e. mod removed, item removed
     * from mod, etc.) do not crash when being decoded.
     */
    @Test
    void testDecodeWithRemovedIngredientItemIds() {
        var encoded = PatternDetailsHelper.encodeProcessingPattern(
                new GenericStack[] {
                        GenericStack.fromItemStack(new ItemStack(Items.TORCH)),
                        GenericStack.fromItemStack(new ItemStack(Items.DIAMOND))
                },
                new GenericStack[] {
                        GenericStack.fromItemStack(new ItemStack(Items.STICK))
                });
        var encodedTag = encoded.getTag();

        var inputTag = encodedTag.getList("in", Tag.TAG_COMPOUND).getCompound(0);
        assertEquals("minecraft:torch", inputTag.getString("id"));
        inputTag.putString("id", "minecraft:unknown_item_id");

        var reDecoded = decode(encodedTag);
        assertNull(reDecoded);
    }

    /**
     * Sanity check that an encoded pattern that contains item-ids that are now invalid (i.e. mod removed, item removed
     * from mod, etc.) does not crash when being decoded.
     */
    @Test
    void testDecodeWithRemovedResultItemIds() {
        var encoded = PatternDetailsHelper.encodeProcessingPattern(
                new GenericStack[] {
                        GenericStack.fromItemStack(new ItemStack(Items.TORCH)),
                        GenericStack.fromItemStack(new ItemStack(Items.DIAMOND))
                },
                new GenericStack[] {
                        GenericStack.fromItemStack(new ItemStack(Items.STICK))
                });
        var encodedTag = encoded.getTag();

        var inputTag = encodedTag.getList("out", Tag.TAG_COMPOUND).getCompound(0);
        assertEquals("minecraft:stick", inputTag.getString("id"));
        inputTag.putString("id", "minecraft:unknown_item_id");

        assertNull(decode(encodedTag));
    }

    /**
     * Sanity check that an encoded pattern that contains stacks that reference missing storage channels.
     */
    @Test
    void testDecodeWithRemovedStorageChannels() {
        var encoded = PatternDetailsHelper.encodeProcessingPattern(
                new GenericStack[] {
                        GenericStack.fromItemStack(new ItemStack(Items.TORCH)),
                        GenericStack.fromItemStack(new ItemStack(Items.DIAMOND))
                },
                new GenericStack[] {
                        GenericStack.fromItemStack(new ItemStack(Items.STICK))
                });
        var encodedTag = encoded.getTag();

        var inputTag = encodedTag.getList("in", Tag.TAG_COMPOUND).getCompound(0);
        assertEquals("ae2:i", inputTag.getString("#c"));
        inputTag.putString("#c", "some_mod:missing_chan");

        assertNull(decode(encodedTag));
    }

    private AEProcessingPattern decode(CompoundTag tag) {
        return AEItems.PROCESSING_PATTERN.asItem().decode(AEItemKey.of(AEItems.PROCESSING_PATTERN, tag), null);
    }
}
