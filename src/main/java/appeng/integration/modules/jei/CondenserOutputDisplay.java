package appeng.integration.modules.jei;

import appeng.api.AEApi;
import appeng.api.config.CondenserOutput;
import appeng.api.definitions.IMaterials;
import appeng.api.implementations.items.IStorageComponent;
import appeng.core.Api;
import appeng.tile.misc.CondenserBlockEntity;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.RecipeDisplay;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CondenserOutputDisplay implements RecipeDisplay {

    private final CondenserOutput type;

    private final List<EntryStack> output;

    private final List<EntryStack> viableStorageComponents;

    public CondenserOutputDisplay(CondenserOutput output) {
        this.type = output;
        this.output = Collections.singletonList(
                EntryStack.create(getOutput(type))
        );
        this.viableStorageComponents = getViableStorageComponents(output);
    }

    @Override
    public List<List<EntryStack>> getInputEntries() {
        return Collections.emptyList();
    }

    @Override
    public List<EntryStack> getOutputEntries() {
        return output;
    }

    @Override
    public Identifier getRecipeCategory() {
        return CondenserCategory.UID;
    }

    public CondenserOutput getType() {
        return type;
    }

    private static ItemStack getOutput(CondenserOutput recipe) {
        switch (recipe) {
            case MATTER_BALLS:
                return Api.INSTANCE.definitions().materials().matterBall().stack(1);
            case SINGULARITY:
                return Api.INSTANCE.definitions().materials().singularity().stack(1);
            default:
                return ItemStack.EMPTY;
        }
    }

    private List<EntryStack> getViableStorageComponents(CondenserOutput condenserOutput) {
        IMaterials materials = AEApi.instance().definitions().materials();
        List<EntryStack> viableComponents = new ArrayList<>();
        this.addViableComponent(condenserOutput, viableComponents, materials.cell1kPart().stack(1));
        this.addViableComponent(condenserOutput, viableComponents, materials.cell4kPart().stack(1));
        this.addViableComponent(condenserOutput, viableComponents, materials.cell16kPart().stack(1));
        this.addViableComponent(condenserOutput, viableComponents, materials.cell64kPart().stack(1));
        return viableComponents;
    }

    private void addViableComponent(CondenserOutput condenserOutput, List<EntryStack> viableComponents,
                                    ItemStack itemStack) {
        IStorageComponent comp = (IStorageComponent) itemStack.getItem();
        int storage = comp.getBytes(itemStack) * CondenserBlockEntity.BYTE_MULTIPLIER;
        if (storage >= condenserOutput.requiredPower) {
            viableComponents.add(EntryStack.create(itemStack));
        }
    }

    public List<EntryStack> getViableStorageComponents() {
        return viableStorageComponents;
    }
}
