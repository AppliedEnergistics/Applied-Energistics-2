
package appeng.block.paint;

import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.bootstrap.BlockRenderingCustomizer;
import appeng.bootstrap.IBlockRendering;
import appeng.bootstrap.IItemRendering;

public class PaintRendering extends BlockRenderingCustomizer {

    @Override
    @OnlyIn(Dist.CLIENT)
    public void customize(IBlockRendering rendering, IItemRendering itemRendering) {
        rendering.renderType(RenderType.getCutout());
        // Disable auto rotation
        rendering.modelCustomizer((location, model) -> model);
    }
}
