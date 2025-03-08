package appeng.server.testplots;

import java.util.Arrays;

import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;

import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.stacks.AEItemKey;

public class CraftingPatternHelper {

    public static ItemStack encodeCraftingPattern(ServerLevel level,
            Object[] ingredients,
            boolean allowSubstitutions,
            boolean allowFluidSubstitutions) {

        // Allow a mixed input of items or item stacks as ingredients
        var stacks = Arrays.stream(ingredients)
                .map(in -> {
                    if (in instanceof ItemLike itemLike) {
                        return new ItemStack(itemLike);
                    } else if (in instanceof ItemStack itemStack) {
                        return itemStack;
                    } else if (in == null) {
                        return ItemStack.EMPTY;
                    } else {
                        throw new IllegalArgumentException("Unsupported argument: " + in);
                    }
                })
                .toArray(ItemStack[]::new);

        var c = NonNullList.withSize(9, ItemStack.EMPTY);
        for (int i = 0; i < stacks.length; i++) {
            c.set(i, stacks[i]);
        }
        var recipeInput = CraftingInput.of(3, 3, c);

        var recipe = level.recipeAccess().getRecipeFor(RecipeType.CRAFTING, recipeInput, level).orElseThrow();

        var result = recipe.value().assemble(recipeInput, level.registryAccess());

        return PatternDetailsHelper.encodeCraftingPattern(
                recipe,
                stacks,
                result,
                allowSubstitutions,
                allowFluidSubstitutions);
    }

    public static ItemStack encodeShapelessCraftingRecipe(ServerLevel level, ItemStack... inputs) {
        // Pad out the list to 3x3
        var items = NonNullList.withSize(3 * 3, ItemStack.EMPTY);
        for (int i = 0; i < inputs.length; i++) {
            items.set(i, inputs[i]);
        }
        var recipeInput = CraftingInput.of(3, 3, items);

        var recipe = level.recipeAccess().getRecipeFor(RecipeType.CRAFTING, recipeInput, level)
                .orElseThrow(() -> new RuntimeException("Couldn't get a shapeless recipe for the provided input."));

        var actualInputs = new ItemStack[9];
        for (int i = 0; i < actualInputs.length; i++) {
            actualInputs[i] = i < inputs.length ? inputs[i] : ItemStack.EMPTY;
        }

        var result = recipe.value().assemble(recipeInput, level.registryAccess());

        return PatternDetailsHelper.encodeCraftingPattern(
                recipe,
                actualInputs,
                result,
                false,
                false);
    }

    public static ItemStack encodeStoneCutterPattern(ServerLevel level, ItemLike inputItem, ItemLike outputItem,
            boolean allowSubstitutes) {

        var input = new SingleRecipeInput(new ItemStack(inputItem));

        RecipeHolder<StonecutterRecipe> foundRecipe = null;
        var it = level.recipeAccess().recipeMap().getRecipesFor(RecipeType.STONECUTTING, input, level).iterator();
        while (it.hasNext()) {
            var holder = it.next();
            StonecutterRecipe recipe = holder.value();
            if (recipe.assemble(input, level.registryAccess()).is(outputItem.asItem())) {
                foundRecipe = holder;
                break;
            }
        }

        if (foundRecipe == null) {
            throw new RuntimeException(
                    "No stonecutter recipe found for input=" + inputItem + " and output=" + outputItem);
        }

        return PatternDetailsHelper.encodeStonecuttingPattern(
                foundRecipe,
                AEItemKey.of(inputItem),
                AEItemKey.of(outputItem),
                allowSubstitutes);
    }

    public static ItemStack encodeSmithingPattern(ServerLevel level, ItemLike template, ItemLike base, ItemLike addition,
            boolean allowSubstitutes) {

        var input = new SmithingRecipeInput(new ItemStack(template), new ItemStack(base), new ItemStack(addition));

        var foundRecipe = level.recipeAccess().getRecipeFor(RecipeType.SMITHING, input, level).orElse(null);
        if (foundRecipe == null) {
            throw new RuntimeException(
                    "No stonecutter recipe found for template=" + template + " and base=" + base + " and addition="
                            + addition);
        }

        var result = foundRecipe.value().assemble(input, level.registryAccess());

        return PatternDetailsHelper.encodeSmithingTablePattern(
                foundRecipe,
                AEItemKey.of(template),
                AEItemKey.of(base),
                AEItemKey.of(addition),
                AEItemKey.of(result),
                allowSubstitutes);
    }

}
