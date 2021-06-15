package appeng.init;

import net.minecraft.block.Block;
import net.minecraftforge.registries.IForgeRegistry;

import appeng.api.definitions.IBlockDefinition;
import appeng.core.api.definitions.ApiBlocks;

public final class InitBlocks {

    private InitBlocks() {
    }

    public static void init(IForgeRegistry<Block> registry) {
        for (IBlockDefinition definition : ApiBlocks.getBlocks()) {
            registry.register(definition.block());
        }
    }

}
