
package appeng.block.misc;


import appeng.bootstrap.*;
import appeng.tile.misc.TileInscriber;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.client.render.tesr.InscriberTESR;


public class InscriberRendering extends TileEntityRenderingCustomizer<TileInscriber>
{

	@OnlyIn( Dist.CLIENT )
	@Override
	public void customize(TileEntityRendering<TileInscriber> rendering) {
		rendering.tileEntityRenderer( InscriberTESR::new );
	}

}
