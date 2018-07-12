
package appeng.client.render.cablebus;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;

import appeng.api.parts.IPartBakedModel;
import appeng.api.util.AEColor;
import appeng.util.Platform;


public class P2PTunnelFrequencyBakedModel implements IBakedModel, IPartBakedModel
{
	private final VertexFormat format;
	private final TextureAtlasSprite texture;

	private static final int[][] QUAD_OFFSETS = new int[][] {
			{ 4, 10, 2 },
			{ 10, 10, 2 },
			{ 4, 4, 2 },
			{ 10, 4, 2 }
	};

	public P2PTunnelFrequencyBakedModel( final VertexFormat format, final TextureAtlasSprite texture )
	{
		this.format = format;
		this.texture = texture;
	}

	@Override
	public List<BakedQuad> getPartQuads( Long partFlags, long rand )
	{
		short frequency = 0;
		boolean active = false;
		if( partFlags != null )
		{
			frequency = (short) ( partFlags.longValue() & 0xffffL );
			active = ( partFlags.longValue() & 0x10000L ) != 0;
		}
		return getQuadsForFrequency( frequency, active );
	}

	@Override
	public List<BakedQuad> getQuads( IBlockState state, EnumFacing side, long rand )
	{
		if( side != null )
		{
			return Collections.emptyList();
		}
		return getPartQuads( null, rand );
	}

	private List<BakedQuad> getQuadsForFrequency( final short frequency, final boolean active )
	{
		final List<BakedQuad> out = new ArrayList<>();
		final AEColor[] colors = Platform.p2p().toColors( frequency );
		for( int i = 0; i < 4; ++i )
		{
			final int[] offs = QUAD_OFFSETS[i];
			final CubeBuilder cb = new CubeBuilder( this.format );

			cb.setTexture( this.texture );
			cb.useStandardUV();
			if( active )
			{
				cb.setRenderFullBright( true );
				cb.setColorRGB( colors[i].dye.getColorValue() );
			}
			else
			{
				final float cv[] = colors[i].dye.getColorComponentValues();
				cb.setColorRGB( cv[0] * 0.5f, cv[1] * 0.5f, cv[2] * 0.5f );
			}
			cb.addCube( offs[0], offs[1], offs[2], offs[0] + 2, offs[1] + 2, offs[2] + 1 );
			out.addAll( cb.getOutput() );
		}
		return out;
	}

	@Override
	public boolean isAmbientOcclusion()
	{
		return false;
	}

	@Override
	public boolean isGui3d()
	{
		return false;
	}

	@Override
	public boolean isBuiltInRenderer()
	{
		return true;
	}

	@Override
	public TextureAtlasSprite getParticleTexture()
	{
		return this.texture;
	}

	@Override
	public ItemOverrideList getOverrides()
	{
		return ItemOverrideList.NONE;
	}
}
