package appeng.init.client;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.core.api.definitions.ApiBlocks;
import appeng.core.features.BlockDefinition;

/**
 * Initializes which layers specific blocks render in.
 */
@OnlyIn(Dist.CLIENT)
public final class InitRenderTypes {

    /**
     * List of blocks that should render in the cutout layer.
     */
    private static final BlockDefinition[] CUTOUT_BLOCKS = {
            ApiBlocks.CRAFTING_MONITOR,
            ApiBlocks.SECURITY_STATION,
            ApiBlocks.CONTROLLER,
            ApiBlocks.MOLECULAR_ASSEMBLER,
            ApiBlocks.QUARTZ_ORE_CHARGED,
            ApiBlocks.QUARTZ_GLASS,
            ApiBlocks.QUARTZ_VIBRANT_GLASS,
            ApiBlocks.QUARTZ_FIXTURE,
            ApiBlocks.LIGHT_DETECTOR,
            ApiBlocks.WIRELESS_ACCESS_POINT,
            ApiBlocks.PAINT,
            ApiBlocks.QUANTUM_RING,
            ApiBlocks.QUANTUM_LINK,
            ApiBlocks.CHEST,
            ApiBlocks.DRIVE,
            ApiBlocks.CRAFTING_UNIT,
            ApiBlocks.CRAFTING_ACCELERATOR,
            ApiBlocks.CRAFTING_STORAGE_1K,
            ApiBlocks.CRAFTING_STORAGE_4K,
            ApiBlocks.CRAFTING_STORAGE_16K,
            ApiBlocks.CRAFTING_STORAGE_64K,
            ApiBlocks.SPATIAL_PYLON,
    };

    private InitRenderTypes() {
    }

    public static void init() {
        for (BlockDefinition definition : CUTOUT_BLOCKS) {
            RenderTypeLookup.setRenderLayer(definition.block(), RenderType.getCutout());
        }

        // Cable bus multiblock renders in all layers
        RenderTypeLookup.setRenderLayer(ApiBlocks.MULTI_PART.block(), rt -> true);
    }

}
