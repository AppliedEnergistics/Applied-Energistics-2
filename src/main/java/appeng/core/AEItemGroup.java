package appeng.core;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import appeng.api.AEApi;
import appeng.api.definitions.IBlocks;
import appeng.api.definitions.IDefinitions;
import appeng.api.definitions.IItemDefinition;

public class AEItemGroup extends ItemGroup {

    private final List<IItemDefinition> itemDefs = new ArrayList<>();

    public AEItemGroup(String label) {
        super(label);
    }

    @Override
    public ItemStack createIcon() {
        final IDefinitions definitions = AEApi.instance().definitions();
        final IBlocks blocks = definitions.blocks();
        return blocks.controller().stack(1);
    }

    public void add(IItemDefinition itemDef) {
        this.itemDefs.add(itemDef);
    }

    @Override
    public void fill(NonNullList<ItemStack> items) {
        for (IItemDefinition itemDef : this.itemDefs) {
            itemDef.item().fillItemGroup(this, items);
        }
    }

}
