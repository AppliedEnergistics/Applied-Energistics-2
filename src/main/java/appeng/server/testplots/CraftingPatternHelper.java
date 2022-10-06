package appeng.server.testplots;

import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import appeng.api.crafting.PatternDetailsHelper;
import appeng.menu.AutoCraftingMenu;

public class CraftingPatternHelper {
    public static ItemStack encodeShapelessCraftingRecipe(Level level, ItemStack... inputs) {
        var container = new CraftingContainer(new AutoCraftingMenu(), 3, 3);
        for (int i = 0; i < inputs.length; i++) {
            container.setItem(i, inputs[i].copy());
        }

        var recipe = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, container, level)
                .orElseThrow(() -> new RuntimeException("Couldn't get a shapeless recipe for the provided input."));

        var actualInputs = new ItemStack[9];
        for (int i = 0; i < actualInputs.length; i++) {
            actualInputs[i] = i < inputs.length ? inputs[i] : ItemStack.EMPTY;
        }

        return PatternDetailsHelper.encodeCraftingPattern(
                recipe,
                actualInputs,
                recipe.getResultItem(),
                false,
                false);
    }

}
