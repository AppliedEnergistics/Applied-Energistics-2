package appeng.block.misc;

import appeng.tile.misc.InscriberTileEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import appeng.bootstrap.TileEntityRendering;
import appeng.bootstrap.TileEntityRenderingCustomizer;
import appeng.client.render.tesr.InscriberTESR;

public class InscriberRendering implements TileEntityRenderingCustomizer<InscriberTileEntity> {

    @Environment(EnvType.CLIENT)
    @Override
    public void customize(TileEntityRendering<InscriberTileEntity> rendering) {
        rendering.tileEntityRenderer(InscriberTESR::new);
    }

}
