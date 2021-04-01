package appeng.block.qnb;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.RenderType;
import appeng.bootstrap.BlockRenderingCustomizer;
import appeng.bootstrap.IBlockRendering;
import appeng.bootstrap.IItemRendering;

public class QuantumBridgeRendering extends BlockRenderingCustomizer {

    @Override
    @Environment(EnvType.CLIENT)
    public void customize(IBlockRendering rendering, IItemRendering itemRendering) {
        rendering.renderType(RenderType.getCutout());
        // Disable auto rotation
        rendering.modelCustomizer((location, model) -> model);
    }
}
