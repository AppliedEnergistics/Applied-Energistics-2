package appeng.helpers;

import javax.annotation.Nullable;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import appeng.api.inventories.InternalInventory;
import appeng.api.networking.security.IActionHost;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.ITerminalHost;
import appeng.parts.encoding.EncodingMode;
import appeng.util.inv.InternalInventoryHost;

public interface IPatternTerminalHost extends ITerminalHost, IActionHost, InternalInventoryHost {

    /**
     * Identifies the sub-inventory used by the pattern terminal to encode the inputs of crafting or processing
     * patterns.
     */
    ResourceLocation INV_CRAFTING = new ResourceLocation(
            "ae2:pattern_encoding_terminal_crafting");

    /**
     * Identifies the sub-inventory used by the pattern terminal to encode the outputs of crafting or processing
     * patterns.
     */
    ResourceLocation INV_OUTPUT = new ResourceLocation(
            "ae2:pattern_encoding_terminal_output");

    @Nullable
    InternalInventory getSubInventory(ResourceLocation id);

    EncodingMode getMode();

    void setMode(EncodingMode mode);

    boolean isSubstitution();

    void setSubstitution(boolean canSubstitute);

    boolean isFluidSubstitution();

    void setFluidSubstitution(boolean canSubstitute);

    default void fixCraftingRecipes() {
        if (getMode() == EncodingMode.CRAFTING) {
            var craftingGrid = this.getSubInventory(INV_CRAFTING);
            for (int slot = 0; slot < craftingGrid.size(); slot++) {
                // Clamp item count to 1 for crafting recipes
                var is = craftingGrid.getStackInSlot(slot);
                if (!is.isEmpty()) {
                    is.setCount(1);
                }

                // Wrapped stacks are not allowed in crafting recipes. Attempt unwrapping items, but
                // clear any other form of generic stack.
                var stack = GenericStack.unwrapItemStack(is);
                if (stack != null) {
                    if (stack.what() instanceof AEItemKey itemKey) {
                        craftingGrid.setItemDirect(slot, itemKey.toStack());
                    } else {
                        craftingGrid.setItemDirect(slot, ItemStack.EMPTY);
                    }
                }
            }
        }
    }
}
