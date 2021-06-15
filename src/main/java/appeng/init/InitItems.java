package appeng.init;

import net.minecraft.item.Item;
import net.minecraftforge.registries.IForgeRegistry;

import appeng.api.definitions.IBlockDefinition;
import appeng.api.definitions.IItemDefinition;
import appeng.core.api.definitions.ApiBlocks;
import appeng.core.api.definitions.ApiItems;

public final class InitItems {

    private InitItems() {
    }

    public static void init(IForgeRegistry<Item> registry) {
        for (IBlockDefinition definition : ApiBlocks.getBlocks()) {
            registry.register(definition.blockItem());
        }
        for (IItemDefinition definition : ApiItems.getItems()) {
            registry.register(definition.item());
        }
    }

}
