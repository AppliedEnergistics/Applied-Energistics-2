
package appeng.block.misc;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.bootstrap.TileEntityRendering;
import appeng.bootstrap.TileEntityRenderingCustomizer;
import appeng.client.render.tesr.InscriberTESR;
import appeng.tile.misc.InscriberTileEntity;

public class InscriberRendering extends TileEntityRenderingCustomizer<InscriberTileEntity> {

    @OnlyIn(Dist.CLIENT)
    @Override
    public void customize(TileEntityRendering<InscriberTileEntity> rendering) {
        rendering.tileEntityRenderer(InscriberTESR::new);
    }

}
