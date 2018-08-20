
package appeng.client.render.cablebus;


import java.util.EnumSet;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;


/**
 * Captures the state required to render a facade properly.
 */
public class FacadeRenderState
{

	// The block state to use for rendering this facade
	private final IBlockState sourceBlock;

	// Which faces of the cube should be rendered for this particular facade
    @Deprecated // This can be removed?
	private final EnumSet<EnumFacing> openFaces;

	// For resolving the tint indices of a facade
	private final ItemStack textureItem;

	public FacadeRenderState( IBlockState sourceBlock, EnumSet<EnumFacing> openFaces, ItemStack textureItem )
	{
		this.sourceBlock = sourceBlock;
		this.openFaces = openFaces;
		this.textureItem = textureItem;
	}

	public IBlockState getSourceBlock()
	{
		return this.sourceBlock;
	}

    @Deprecated
	public EnumSet<EnumFacing> getOpenFaces()
	{
		return this.openFaces;
	}

	public int resolveTintColor( int tintIndex )
	{
		return Minecraft.getMinecraft().getItemColors().colorMultiplier( this.textureItem, tintIndex );
	}

}
