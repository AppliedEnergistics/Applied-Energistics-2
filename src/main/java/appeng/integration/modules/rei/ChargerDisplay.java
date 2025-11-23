package appeng.integration.modules.rei;

import java.util.List;
import java.util.Optional;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeHolder;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;

import appeng.recipes.handlers.ChargerRecipe;

public record ChargerDisplay(RecipeHolder<ChargerRecipe> holder) implements Display {

    @Override
    public List<EntryIngredient> getInputEntries() {
        return List.of(EntryIngredients.ofIngredient(holder.value().getIngredient()));
    }

    @Override
    public List<EntryIngredient> getOutputEntries() {
        return List.of(EntryIngredients.of(holder.value().getResultItem()));
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return CategoryIds.CHARGER;
    }

    @Override
    public Optional<Identifier> getDisplayLocation() {
        return Optional.of(holder.id().identifier());
    }
}
