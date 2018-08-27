package appeng.client.render.cablebus;


import net.minecraft.block.state.IBlockState;


/**
 * Captures the state required to render a facade properly.
 */
public class FacadeRenderState
{

	// The block state to use for rendering this facade
	private final IBlockState sourceBlock;

	private final boolean transparent;

	public FacadeRenderState( IBlockState sourceBlock, boolean transparent )
	{
		this.sourceBlock = sourceBlock;
		this.transparent = transparent;
	}

	public IBlockState getSourceBlock()
	{
		return this.sourceBlock;
	}

	public boolean isTransparent()
	{
		return transparent;
	}
}
