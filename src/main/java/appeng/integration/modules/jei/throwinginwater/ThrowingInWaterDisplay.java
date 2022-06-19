package appeng.integration.modules.jei.throwinginwater;

import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;

public class ThrowingInWaterDisplay extends BasicDisplay {
    public ThrowingInWaterDisplay(List<Ingredient> inputs, ItemStack result) {
        super(
                EntryIngredients.ofIngredients(inputs),
                List.of(EntryIngredients.of(result)));
    }

    private ThrowingInWaterDisplay(List<EntryIngredient> inputs,
            List<EntryIngredient> outputs,
            CompoundTag data) {
        super(inputs, outputs);
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return ThrowingInWaterCategory.ID;
    }

    public static BasicDisplay.Serializer<ThrowingInWaterDisplay> serializer() {
        return BasicDisplay.Serializer.ofRecipeLess(ThrowingInWaterDisplay::new);
    }
}
