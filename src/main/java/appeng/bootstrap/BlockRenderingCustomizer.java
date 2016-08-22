package appeng.bootstrap;


import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


/**
 * A callback that allows the rendering of a block to be customized. Sadly this class is required and no lambdas can be used
 * due to them not being able to be annotated with @SideOnly(CLIENT).
 */
public abstract class BlockRenderingCustomizer
{

	@SideOnly( Side.CLIENT )
	public abstract void customize( IBlockRendering rendering, IItemRendering itemRendering );

}
