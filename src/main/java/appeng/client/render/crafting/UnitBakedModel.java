package appeng.client.render.crafting;


import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;

import appeng.client.render.cablebus.CubeBuilder;


/**
 * A simple crafting unit model that uses an un-lit texture for the inner block.
 */
class UnitBakedModel extends CraftingCubeBakedModel
{

	private final TextureAtlasSprite unitTexture;

	UnitBakedModel( VertexFormat format, TextureAtlasSprite ringCorner, TextureAtlasSprite ringHor, TextureAtlasSprite ringVer, TextureAtlasSprite unitTexture )
	{
		super( format, ringCorner, ringHor, ringVer );
		this.unitTexture = unitTexture;
	}

	@Override
	protected void addInnerCube( EnumFacing facing, IBlockState state, CubeBuilder builder, float x1, float y1, float z1, float x2, float y2, float z2 )
	{
		builder.setTexture( unitTexture );
		builder.addCube( x1, y1, z1, x2, y2, z2 );
	}
}
