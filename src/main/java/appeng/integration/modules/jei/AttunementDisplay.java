package appeng.integration.modules.jei;

import java.util.List;

import net.minecraft.network.chat.Component;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;

public class AttunementDisplay extends BasicDisplay {

    private final Component description;

    public AttunementDisplay(List<EntryIngredient> inputs, List<EntryIngredient> outputs, Component description) {
        super(inputs, outputs);
        this.description = description;
    }

    public Component getDescription() {
        return description;
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return AttunementCategory.ID;
    }
}
