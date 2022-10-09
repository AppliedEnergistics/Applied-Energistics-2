package appeng.integration.modules.jei.transfer;

import net.minecraft.world.item.crafting.Recipe;

import mezz.jei.api.gui.ingredient.IRecipeSlotsView;

public abstract class AbstractTransferHandler {
    protected static final int CRAFTING_GRID_WIDTH = 3;
    protected static final int CRAFTING_GRID_HEIGHT = 3;

    protected final boolean fitsIn3x3Grid(Recipe<?> recipe, IRecipeSlotsView display) {
        if (recipe != null) {
            return recipe.canCraftInDimensions(CRAFTING_GRID_WIDTH, CRAFTING_GRID_HEIGHT);
        } else {
            return true;
        }
    }
}
