package appeng.datagen.providers.recipes;

import appeng.api.ids.AETags;
import appeng.core.definitions.AEParts;
import appeng.datagen.providers.tags.ConventionTags;
import appeng.recipes.quartzcutting.QuartzCuttingRecipe;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.crafting.Ingredient;

public class QuartzCuttingRecipesProvider extends AE2RecipeProvider {

    public QuartzCuttingRecipesProvider(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
    }

    @Override
    protected void buildRecipes() {
        output.accept(
                makeKey("network/parts/cable_anchor"),
                new QuartzCuttingRecipe(
                        AEParts.CABLE_ANCHOR.stack(4),
                        NonNullList.of(Ingredient.of(items.getOrThrow(ConventionTags.QUARTZ_KNIFE)),
                                Ingredient.of(items.getOrThrow(AETags.METAL_INGOTS)))),
                null);
    }
}
