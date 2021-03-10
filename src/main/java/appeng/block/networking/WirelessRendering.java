
package appeng.block.networking;

import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.api.util.AEColor;
import appeng.bootstrap.BlockRenderingCustomizer;
import appeng.bootstrap.IBlockRendering;
import appeng.bootstrap.IItemRendering;
import appeng.client.render.StaticBlockColor;

public class WirelessRendering extends BlockRenderingCustomizer {
    @Override
    @OnlyIn(Dist.CLIENT)
    public void customize(IBlockRendering rendering, IItemRendering itemRendering) {
        rendering.renderType(RenderType.cutout());
        rendering.blockColor(new StaticBlockColor(AEColor.TRANSPARENT));
    }
}
