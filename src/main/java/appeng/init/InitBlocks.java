package appeng.init;

import net.minecraft.block.Block;
import net.minecraftforge.registries.IForgeRegistry;

import appeng.core.api.definitions.ApiBlocks;
import appeng.core.features.BlockDefinition;

public final class InitBlocks {

    private InitBlocks() {
    }

    public static void init(IForgeRegistry<Block> registry) {
        for (BlockDefinition definition : ApiBlocks.getBlocks()) {
            registry.register(definition.block());
        }
    }

}
