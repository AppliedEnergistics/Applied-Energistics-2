package appeng.integration.modules.rei;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.item.ItemStack;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryStacks;

import appeng.recipes.entropy.EntropyRecipe;

public class EntropyManipulatorDisplay implements Display {
    private final EntropyRecipe recipe;
    private final List<EntryIngredient> inputs = new ArrayList<>();
    private final List<EntryIngredient> outputs = new ArrayList<>();

    public EntropyManipulatorDisplay(EntropyRecipe recipe) {
        this.recipe = recipe;
        if (recipe.getInputFluid() != null) {
            inputs.add(EntryIngredient.of(EntryStacks.of(recipe.getInputFluid())));
        }
        if (recipe.getInputBlock() != null) {
            inputs.add(EntryIngredient.of(EntryStacks.of(recipe.getInputBlock())));
        }

        if (recipe.getOutputFluid() != null) {
            outputs.add(EntryIngredient.of(EntryStacks.of(recipe.getOutputFluid())));
        }
        if (recipe.getOutputBlock() != null) {
            outputs.add(EntryIngredient.of(EntryStacks.of(recipe.getOutputBlock())));
        }
        for (ItemStack drop : recipe.getDrops()) {
            outputs.add(EntryIngredient.of(EntryStacks.of(drop)));
        }
    }

    public EntropyRecipe getRecipe() {
        return recipe;
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
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return EntropyManipulatorCategory.ID;
    }
}
