package appeng.datagen.providers.recipes;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;

import appeng.api.ids.AETags;
import appeng.core.ConventionTags;
import appeng.core.definitions.AEParts;
import appeng.recipes.quartzcutting.QuartzCuttingRecipe;
import net.minecraft.world.item.crafting.Recipe;

public class QuartzCuttingRecipesProvider extends AE2RecipeProvider {

    public QuartzCuttingRecipesProvider(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
    }

    @Override
    protected void buildRecipes() {
        output.accept(
                makeKey("network/parts/cable_anchor"),
                new QuartzCuttingRecipe(
                        new Recipe.CommonInfo(false), // TODO 26.1 - Mithi83 - check for proper values
                        new CraftingRecipe.CraftingBookInfo(CraftingBookCategory.MISC, ""), // TODO 26.1 - Mithi83 - check for proper values
                        AEParts.CABLE_ANCHOR.template(4),
                        NonNullList.of(Ingredient.of(items.getOrThrow(ConventionTags.QUARTZ_KNIFE)),
                                Ingredient.of(items.getOrThrow(AETags.METAL_INGOTS)))),
                null);
    }
}
