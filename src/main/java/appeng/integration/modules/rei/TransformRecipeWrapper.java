package appeng.integration.modules.rei;

import java.util.List;
import java.util.Optional;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;

import appeng.recipes.transform.TransformCircumstance;
import appeng.recipes.transform.TransformRecipe;

public class TransformRecipeWrapper implements Display {

    private final RecipeHolder<TransformRecipe> holder;
    private final List<EntryIngredient> inputs;
    private final List<EntryIngredient> outputs;

    public TransformRecipeWrapper(RecipeHolder<TransformRecipe> holder) {
        this.holder = holder;
        this.inputs = EntryIngredients.ofIngredients(holder.value().getIngredients());
        this.outputs = List.of(EntryIngredients.of(holder.value().getResultItem()));
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
        return Optional.of(holder.id());
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return TransformCategory.ID;
    }

    public TransformCircumstance getTransformCircumstance() {
        return holder.value().circumstance;
    }
}
