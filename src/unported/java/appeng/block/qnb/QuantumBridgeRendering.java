
package appeng.block.qnb;

import net.minecraft.client.render.RenderLayer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import appeng.bootstrap.BlockRenderingCustomizer;
import appeng.bootstrap.IBlockRendering;
import appeng.bootstrap.IItemRendering;

public class QuantumBridgeRendering extends BlockRenderingCustomizer {

    @Override
    @Environment(EnvType.CLIENT)
    public void customize(IBlockRendering rendering, IItemRendering itemRendering) {
        rendering.renderType(RenderLayer.getCutout());
        // Disable auto rotation
        rendering.modelCustomizer((location, model) -> model);
    }
}
