
package appeng.block.networking;

import net.minecraft.client.render.RenderLayer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import appeng.api.util.AEColor;
import appeng.bootstrap.BlockRenderingCustomizer;
import appeng.bootstrap.IBlockRendering;
import appeng.bootstrap.IItemRendering;
import appeng.client.render.StaticBlockColor;

public class WirelessRendering extends BlockRenderingCustomizer {
    @Override
    @Environment(EnvType.CLIENT)
    public void customize(IBlockRendering rendering, IItemRendering itemRendering) {
        rendering.renderType(RenderLayer.getCutout());
        rendering.blockColor(new StaticBlockColor(AEColor.TRANSPARENT));
    }
}
