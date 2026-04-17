package appeng.recipes;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.level.Level;

/**
 * Base class for recipes that are recipes used to
 * make game mechanics data-driven without being normal crafting
 * recipes. This interface just default-implements a few of the
 * recipe methods that would only be relevant for normal crafting.
 * <p>
 * They're:
 * <ul>
 *     <li>Not placeable</li>
 *     <li>Don't show in any recipe books</li>
 *     <li>Don't show discovery notifications</li>
 *     <li>They never match anything</li>
 *     <li>Their assembly method returns an empty stack</li>
 * </ul>
 */
public abstract class MechanicsRecipe<T extends RecipeInput> implements Recipe<T> {
    @Override
    public final boolean isSpecial() {
        return true;
    }

    @Override
    public final boolean showNotification() {
        return false;
    }

    @Override
    public final String group() {
        return "";
    }

    @Override
    public final PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public final RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    @Override
    public final boolean matches(RecipeInput input, Level level) {
        return false;
    }

    @Override
    public final ItemStack assemble(RecipeInput input) {
        return ItemStack.EMPTY;
    }
}
