package appeng.client.gui;

import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

/**
 * Allows a widget to expose an ingredient for use with the JEI integration. This is used to allow players to hover
 * non-item slots and press R or U to show related recipes.
 */
public interface IIngredientSupplier {

    /**
     * @return If this widget contains an item, return it for the purposes of JEI integration.
     */
    @Nullable
    default ItemStack getItemIngredient() {
        return null;
    }

    /**
     * @return If this widget contains a fluid, return it for the purposes of JEI integration.
     */
    @Nullable
    default FluidStack getFluidIngredient() {
        return null;
    }

}
