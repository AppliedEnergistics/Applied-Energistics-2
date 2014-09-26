package mods.railcraft.api.crafting;

import java.util.List;
import net.minecraft.item.ItemStack;

/**
 *
 * @author CovertJaguar <http://www.railcraft.info>
 */
public interface IBlastFurnaceCraftingManager {

    /**
     * Adds a new Blast Furnace Recipe.
     *
     * @param input the input, if null the function will silently abort
     * @param matchDamage if true, it will compare item damage, if false, just
     * the item ID
     * @param matchNBT if true, it will compare nbt
     * @param cookTime the time it takes to cook the recipe
     * @param output the output
     */
    void addRecipe(ItemStack input, boolean matchDamage, boolean matchNBT, int cookTime, ItemStack output);

    List<ItemStack> getFuels();

    IBlastFurnaceRecipe getRecipe(ItemStack stack);

    List<? extends IBlastFurnaceRecipe> getRecipes();
}
