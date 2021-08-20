package appeng.integration.modules.jei;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.world.item.ItemStack;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;

import appeng.api.config.CondenserOutput;
import appeng.api.implementations.items.IStorageComponent;
import appeng.blockentity.misc.CondenserBlockEntity;
import appeng.core.definitions.AEItems;

public class CondenserOutputDisplay implements Display {

    private final CondenserOutput type;

    private final List<EntryIngredient> output;

    private final List<EntryStack<ItemStack>> viableStorageComponents;

    public CondenserOutputDisplay(CondenserOutput output) {
        this.type = output;
        this.output = Collections.singletonList(EntryIngredients.of(getOutput(type)));
        this.viableStorageComponents = getViableStorageComponents(output);
    }

    @Override
    public List<EntryIngredient> getInputEntries() {
        return Collections.emptyList();
    }

    @Override
    public List<EntryIngredient> getOutputEntries() {
        return output;
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return CondenserCategory.ID;
    }

    public CondenserOutput getType() {
        return type;
    }

    private static ItemStack getOutput(CondenserOutput recipe) {
        return switch (recipe) {
            case MATTER_BALLS -> AEItems.MATTER_BALL.stack();
            case SINGULARITY -> AEItems.SINGULARITY.stack();
            default -> ItemStack.EMPTY;
        };
    }

    private List<EntryStack<ItemStack>> getViableStorageComponents(CondenserOutput condenserOutput) {
        List<EntryStack<ItemStack>> viableComponents = new ArrayList<>();
        this.addViableComponent(condenserOutput, viableComponents, AEItems.ITEM_1K_CELL_COMPONENT.stack());
        this.addViableComponent(condenserOutput, viableComponents, AEItems.ITEM_4K_CELL_COMPONENT.stack());
        this.addViableComponent(condenserOutput, viableComponents, AEItems.ITEM_16K_CELL_COMPONENT.stack());
        this.addViableComponent(condenserOutput, viableComponents, AEItems.ITEM_64K_CELL_COMPONENT.stack());
        return viableComponents;
    }

    private void addViableComponent(CondenserOutput condenserOutput, List<EntryStack<ItemStack>> viableComponents,
            ItemStack itemStack) {
        IStorageComponent comp = (IStorageComponent) itemStack.getItem();
        int storage = comp.getBytes(itemStack) * CondenserBlockEntity.BYTE_MULTIPLIER;
        if (storage >= condenserOutput.requiredPower) {
            viableComponents.add(EntryStacks.of(itemStack));
        }
    }

    public List<EntryStack<ItemStack>> getViableStorageComponents() {
        return viableStorageComponents;
    }
}
