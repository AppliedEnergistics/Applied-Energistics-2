package appeng.server.testplots;

import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import appeng.api.crafting.PatternDetailsHelper;
import appeng.menu.AutoCraftingMenu;

import java.util.Arrays;
import java.util.Collections;

public class CraftingPatternHelper {
    public static ItemStack encodeShapelessCraftingRecipe(Level level, ItemStack... inputs) {
        // Pad out the list to 3x3
        var items = NonNullList.withSize(3 * 3, ItemStack.EMPTY);
        Collections.addAll(items, inputs);
        var recipeInput = CraftingInput.of(3, 3, items);

        var recipe = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, recipeInput, level)
                .orElseThrow(() -> new RuntimeException("Couldn't get a shapeless recipe for the provided input."));

        var actualInputs = new ItemStack[9];
        for (int i = 0; i < actualInputs.length; i++) {
            actualInputs[i] = i < inputs.length ? inputs[i] : ItemStack.EMPTY;
        }

        return PatternDetailsHelper.encodeCraftingPattern(
                recipe,
                actualInputs,
                recipe.value().getResultItem(level.registryAccess()),
                false,
                false);
    }

}
