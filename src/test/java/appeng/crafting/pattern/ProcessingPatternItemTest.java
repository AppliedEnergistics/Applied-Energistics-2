package appeng.crafting.pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
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
                new IAEStack[] {
                        IAEItemStack.of(new ItemStack(Items.TORCH)),
                        IAEItemStack.of(new ItemStack(Items.DIAMOND))
                },
                new IAEStack[] {
                        IAEItemStack.of(new ItemStack(Items.STICK))
                });
        var encodedTag = encoded.getTag();

        var inputTag = encodedTag.getList("in", Tag.TAG_COMPOUND).getCompound(0).getCompound("is");
        assertEquals("minecraft:torch", inputTag.getString("id"));
        inputTag.putString("id", "minecraft:unknown_item_id");

        var reDecoded = decode(encodedTag);
        assertNotNull(reDecoded);
        // The missing input should be gone
        assertEquals(1, reDecoded.getInputs().length);
    }

    /**
     * Sanity check that an encoded pattern that contains item-ids that are now invalid (i.e. mod removed, item removed
     * from mod, etc.) do not crash when being decoded.
     */
    @Test
    void testDecodeWithRemovedResultItemIds() {
        var encoded = PatternDetailsHelper.encodeProcessingPattern(
                new IAEStack[] {
                        IAEItemStack.of(new ItemStack(Items.TORCH)),
                        IAEItemStack.of(new ItemStack(Items.DIAMOND))
                },
                new IAEStack[] {
                        IAEItemStack.of(new ItemStack(Items.STICK))
                });
        var encodedTag = encoded.getTag();

        var inputTag = encodedTag.getList("out", Tag.TAG_COMPOUND).getCompound(0).getCompound("is");
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
                new IAEStack[] {
                        IAEItemStack.of(new ItemStack(Items.TORCH)),
                        IAEItemStack.of(new ItemStack(Items.DIAMOND))
                },
                new IAEStack[] {
                        IAEItemStack.of(new ItemStack(Items.STICK))
                });
        var encodedTag = encoded.getTag();

        var inputTag = encodedTag.getList("in", Tag.TAG_COMPOUND).getCompound(0);
        assertEquals("appliedenergistics2:item", inputTag.getString("chan"));
        inputTag.putString("chan", "some_mod:missing_chan");

        assertNull(decode(encodedTag));
    }

    private AEProcessingPattern decode(CompoundTag tag) {
        return AEItems.PROCESSING_PATTERN.asItem().decode(
                tag, null, false);
    }
}
