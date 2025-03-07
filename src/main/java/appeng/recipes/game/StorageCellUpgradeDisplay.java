package appeng.recipes.game;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;

public record StorageCellUpgradeDisplay(
        SlotDisplay inputCell,
        SlotDisplay inputComponent,
        SlotDisplay result,
        SlotDisplay resultComponent
) implements RecipeDisplay {

    @Override
    public SlotDisplay result() {
        return result;
    }

    @Override
    public SlotDisplay craftingStation() {
        return new SlotDisplay.ItemSlotDisplay(Items.CRAFTING_TABLE);
    }

    @Override
    public Type<? extends RecipeDisplay> type() {
        return TYPE;
    }
}
