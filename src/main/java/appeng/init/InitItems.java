package appeng.init;

import net.minecraft.item.Item;
import net.minecraftforge.registries.IForgeRegistry;

import appeng.core.api.definitions.ApiBlocks;
import appeng.core.api.definitions.ApiItems;
import appeng.core.features.BlockDefinition;
import appeng.core.features.ItemDefinition;

public final class InitItems {

    private InitItems() {
    }

    public static void init(IForgeRegistry<Item> registry) {
        for (BlockDefinition definition : ApiBlocks.getBlocks()) {
            registry.register(definition.blockItem());
        }
        for (ItemDefinition definition : ApiItems.getItems()) {
            registry.register(definition.item());
        }
    }

}
