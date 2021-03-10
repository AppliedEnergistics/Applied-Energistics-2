package appeng.core;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import appeng.api.definitions.IBlocks;
import appeng.api.definitions.IDefinitions;
import appeng.api.definitions.IItemDefinition;

public class AEItemGroup extends ItemGroup {

    private final List<IItemDefinition> itemDefs = new ArrayList<>();

    public AEItemGroup(String label) {
        super(label);
    }

    @Override
    public ItemStack makeIcon() {
        final IDefinitions definitions = Api.instance().definitions();
        final IBlocks blocks = definitions.blocks();
        return blocks.controller().stack(1);
    }

    public void add(IItemDefinition itemDef) {
        this.itemDefs.add(itemDef);
    }

    @Override
    public void fillItemList(NonNullList<ItemStack> items) {
        for (IItemDefinition itemDef : this.itemDefs) {
            itemDef.item().fillItemCategory(this, items);
        }
    }

}
