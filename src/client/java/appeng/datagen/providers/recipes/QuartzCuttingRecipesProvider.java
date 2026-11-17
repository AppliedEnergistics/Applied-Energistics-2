package appeng.datagen.providers.recipes;

import java.util.List;

import net.minecraft.advancements.Advancement;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;

import appeng.api.ids.AETags;
import appeng.core.ConventionTags;
import appeng.core.definitions.AEParts;
import appeng.recipes.quartzcutting.QuartzCuttingRecipe;

public class QuartzCuttingRecipesProvider extends AE2RecipeProvider {

    public QuartzCuttingRecipesProvider(BootstrapContext<Recipe<?>> recipeOutput,
            BootstrapContext<Advancement> advancementOutput) {
        super(recipeOutput, advancementOutput);
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
