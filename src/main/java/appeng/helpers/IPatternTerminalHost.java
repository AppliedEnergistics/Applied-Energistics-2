package appeng.helpers;

import javax.annotation.Nullable;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import appeng.api.inventories.InternalInventory;
import appeng.api.networking.security.IActionHost;
import appeng.api.storage.ITerminalHost;
import appeng.util.inv.InternalInventoryHost;

public interface IPatternTerminalHost extends ITerminalHost, IActionHost, InternalInventoryHost {

    /**
     * Identifies the sub-inventory used by the pattern terminal to encode the inputs of crafting or processing
     * patterns.
     */
    ResourceLocation INV_CRAFTING = new ResourceLocation(
            "ae2:pattern_terminal_crafting");

    /**
     * Identifies the sub-inventory used by the pattern terminal to encode the outputs of crafting or processing
     * patterns.
     */
    ResourceLocation INV_OUTPUT = new ResourceLocation(
            "ae2:pattern_terminal_output");

    @Nullable
    InternalInventory getSubInventory(ResourceLocation id);

    boolean isCraftingRecipe();

    void setCraftingRecipe(final boolean craftingMode);

    boolean isSubstitution();

    void setSubstitution(final boolean canSubstitute);

    boolean isFluidSubstitution();

    void setFluidSubstitution(boolean canSubstitute);

    default void fixCraftingRecipes() {
        if (this.isCraftingRecipe()) {
            for (int x = 0; x < this.getSubInventory(INV_CRAFTING).size(); x++) {
                final ItemStack is = this.getSubInventory(INV_CRAFTING).getStackInSlot(x);
                if (!is.isEmpty()) {
                    is.setCount(1);
                }
            }
        }
    }
}
