
package appeng.block.misc;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import appeng.bootstrap.TileEntityRendering;
import appeng.bootstrap.TileEntityRenderingCustomizer;
import appeng.client.render.tesr.InscriberTESR;
import appeng.tile.misc.InscriberBlockEntity;

public class InscriberRendering implements TileEntityRenderingCustomizer<InscriberBlockEntity> {

    @Environment(EnvType.CLIENT)
    @Override
    public void customize(TileEntityRendering<InscriberBlockEntity> rendering) {
        rendering.tileEntityRenderer(InscriberTESR::new);
    }

}
