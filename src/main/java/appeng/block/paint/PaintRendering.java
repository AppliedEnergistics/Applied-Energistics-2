
package appeng.block.paint;


import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appeng.bootstrap.BlockRenderingCustomizer;
import appeng.bootstrap.IBlockRendering;
import appeng.bootstrap.IItemRendering;


public class PaintRendering extends BlockRenderingCustomizer
{

	@Override
	@SideOnly( Side.CLIENT )
	public void customize( IBlockRendering rendering, IItemRendering itemRendering )
	{
		rendering.builtInModel( "models/block/paint", new PaintModel() );
		// Disable auto rotation
		rendering.modelCustomizer( ( location, model ) -> model );
	}
}
