
package appeng.block.misc;


import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.bootstrap.BlockRenderingCustomizer;
import appeng.bootstrap.IBlockRendering;
import appeng.bootstrap.IItemRendering;
import appeng.client.render.tesr.InscriberTESR;


public class InscriberRendering extends BlockRenderingCustomizer
{

	@OnlyIn( Dist.CLIENT )
	@Override
	public void customize( IBlockRendering rendering, IItemRendering itemRendering )
	{
		rendering.tesr( new InscriberTESR() );
	}

}
