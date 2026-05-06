package appeng.datagen.providers.recipes;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;

import appeng.api.ids.AETags;
import appeng.core.ConventionTags;
import appeng.core.definitions.AEParts;
import appeng.recipes.quartzcutting.QuartzCuttingRecipe;

import java.util.List;

public class QuartzCuttingRecipesProvider extends AE2RecipeProvider {

    public QuartzCuttingRecipesProvider(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
    }

    @Override
    protected void buildRecipes() {
        output.accept(
                makeKey("network/parts/cable_anchor"),
                new QuartzCuttingRecipe(
                        new Recipe.CommonInfo(false),
                        new CraftingRecipe.CraftingBookInfo(CraftingBookCategory.MISC, ""),
                        AEParts.CABLE_ANCHOR.template(4),
                        List.of(Ingredient.of(items.getOrThrow(ConventionTags.QUARTZ_KNIFE)),
                                Ingredient.of(items.getOrThrow(AETags.METAL_INGOTS)))),
                null);
    }
}
