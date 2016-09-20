package appeng.client.render.cablebus;


import java.util.EnumSet;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;


/**
 * Captures the state required to render a facade properly.
 */
public class FacadeRenderState
{

	// The block state to use for rendering this facade
	private final IBlockState sourceBlock;

	// Which faces of the cube should be rendered for this particular facade
	private final EnumSet<EnumFacing> openFaces;

	public FacadeRenderState( IBlockState sourceBlock, EnumSet<EnumFacing> openFaces )
	{
		this.sourceBlock = sourceBlock;
		this.openFaces = openFaces;
	}

	public IBlockState getSourceBlock()
	{
		return sourceBlock;
	}

	public EnumSet<EnumFacing> getOpenFaces()
	{
		return openFaces;
	}

}
