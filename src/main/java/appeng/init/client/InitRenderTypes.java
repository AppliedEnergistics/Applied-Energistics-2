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
            ApiBlocks.craftingMonitor,
            ApiBlocks.securityStation,
            ApiBlocks.controller,
            ApiBlocks.molecularAssembler,
            ApiBlocks.quartzOreCharged,
            ApiBlocks.quartzGlass,
            ApiBlocks.quartzVibrantGlass,
            ApiBlocks.quartzFixture,
            ApiBlocks.lightDetector,
            ApiBlocks.wirelessAccessPoint,
            ApiBlocks.paint,
            ApiBlocks.quantumRing,
            ApiBlocks.quantumLink,
            ApiBlocks.chest,
            ApiBlocks.drive,
            ApiBlocks.craftingUnit,
            ApiBlocks.craftingAccelerator,
            ApiBlocks.craftingStorage1k,
            ApiBlocks.craftingStorage4k,
            ApiBlocks.craftingStorage16k,
            ApiBlocks.craftingStorage64k,
            ApiBlocks.spatialPylon,
    };

    private InitRenderTypes() {
    }

    public static void init() {
        for (BlockDefinition definition : CUTOUT_BLOCKS) {
            RenderTypeLookup.setRenderLayer(definition.block(), RenderType.getCutout());
        }

        // Cable bus multiblock renders in all layers
        RenderTypeLookup.setRenderLayer(ApiBlocks.multiPart.block(), rt -> true);
    }

}
