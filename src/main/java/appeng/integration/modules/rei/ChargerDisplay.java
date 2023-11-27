package appeng.integration.modules.rei;

import java.util.List;
import java.util.Optional;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;

import appeng.core.AppEng;
import appeng.recipes.handlers.ChargerRecipe;

public record ChargerDisplay(RecipeHolder<ChargerRecipe> holder) implements Display {
    public static CategoryIdentifier<ChargerDisplay> ID = CategoryIdentifier.of(AppEng.makeId("charger"));

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
        return ID;
    }

    @Override
    public Optional<ResourceLocation> getDisplayLocation() {
        return Optional.of(holder.id());
    }
}
