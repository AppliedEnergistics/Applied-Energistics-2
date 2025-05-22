package appeng.integration.modules.rei;

import java.util.List;
import java.util.Optional;

import net.minecraft.resources.ResourceLocation;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;

import appeng.recipes.transform.TransformCircumstance;
import appeng.recipes.transform.TransformRecipe;

public class TransformRecipeWrapper implements Display {

    private final TransformRecipe recipe;
    private final List<EntryIngredient> inputs;
    private final List<EntryIngredient> outputs;

    public TransformRecipeWrapper(TransformRecipe recipe) {
        this.recipe = recipe;
        this.inputs = EntryIngredients.ofIngredients(recipe.getIngredients());
        this.outputs = List.of(EntryIngredients.of(recipe.getResultItem()));
    }

    @Override
    public List<EntryIngredient> getInputEntries() {
        return inputs;
    }

    @Override
    public List<EntryIngredient> getOutputEntries() {
        return outputs;
    }

    @Override
    public Optional<ResourceLocation> getDisplayLocation() {
        return Optional.of(recipe.getId());
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return TransformCategory.ID;
    }

    public TransformCircumstance getTransformCircumstance() {
        return recipe.circumstance;
    }
}
