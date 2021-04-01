package appeng.block.misc;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import appeng.bootstrap.TileEntityRendering;
import appeng.bootstrap.TileEntityRenderingCustomizer;
import appeng.client.render.tesr.InscriberTESR;
import appeng.tile.misc.InscriberTileEntity;

public class InscriberRendering implements TileEntityRenderingCustomizer<InscriberTileEntity> {

    @Environment(EnvType.CLIENT)
    @Override
    public void customize(TileEntityRendering<InscriberTileEntity> rendering) {
        rendering.tileEntityRenderer(InscriberTESR::new);
    }

}
