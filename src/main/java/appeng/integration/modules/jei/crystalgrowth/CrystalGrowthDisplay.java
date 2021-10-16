package appeng.integration.modules.jei.crystalgrowth;

import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;

public class CrystalGrowthDisplay extends BasicDisplay {
    public static final String TAG_SUPPORTS_ACCELERATORS = "supportsAccelerators";
    private final boolean supportsAccelerators;

    public CrystalGrowthDisplay(List<Ingredient> inputs, ItemStack result, boolean supportsAccelerators) {
        super(
                EntryIngredients.ofIngredients(inputs),
                List.of(EntryIngredients.of(result)));
        this.supportsAccelerators = supportsAccelerators;
    }

    private CrystalGrowthDisplay(List<EntryIngredient> inputs,
            List<EntryIngredient> outputs,
            CompoundTag data) {
        super(inputs, outputs);
        this.supportsAccelerators = data.getBoolean(TAG_SUPPORTS_ACCELERATORS);
    }

    public boolean isSupportsAccelerators() {
        return supportsAccelerators;
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return CrystalGrowthCategory.ID;
    }

    public static BasicDisplay.Serializer<CrystalGrowthDisplay> serializer() {
        return BasicDisplay.Serializer.ofRecipeLess(CrystalGrowthDisplay::new, (display, tag) -> {
            tag.putBoolean(TAG_SUPPORTS_ACCELERATORS, display.isSupportsAccelerators());
        });
    }
}
