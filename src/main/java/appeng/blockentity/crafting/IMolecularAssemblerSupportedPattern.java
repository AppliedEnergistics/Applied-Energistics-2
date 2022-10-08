package appeng.blockentity.crafting;

import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.KeyCounter;

/**
 * Implement this on pattern details that support being assembled in the {@link MolecularAssemblerBlockEntity}.
 */
public interface IMolecularAssemblerSupportedPattern extends IPatternDetails {
    ItemStack assemble(Container container, Level level);

    boolean isItemValid(int slot, AEItemKey key, Level level);

    void fillCraftingGrid(KeyCounter[] table, CraftingGridAccessor gridAccessor);

    @FunctionalInterface
    interface CraftingGridAccessor {
        void set(int slot, ItemStack stack);
    }

    NonNullList<ItemStack> getRemainingItems(CraftingContainer container);
}
