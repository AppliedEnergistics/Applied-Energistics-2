
package appeng.client.render.cablebus;


import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.property.IExtendedBlockState;

import appeng.api.util.AEColor;
import appeng.block.networking.BlockCableBus;
import appeng.util.Platform;


public class P2PTunnelFrequencyBakedModel implements IBakedModel
{
	private final VertexFormat format;
	private final TextureAtlasSprite texture;

	private static final EnumMap<AEColor, List<BakedQuad>> color1Cache = new EnumMap<>( AEColor.class );
	private static final EnumMap<AEColor, List<BakedQuad>> color2Cache = new EnumMap<>( AEColor.class );
	private static final EnumMap<AEColor, List<BakedQuad>> color3Cache = new EnumMap<>( AEColor.class );
	private static final EnumMap<AEColor, List<BakedQuad>> color4Cache = new EnumMap<>( AEColor.class );

	private static final int[][] QUAD_OFFSETS = new int[][] {
			{ 4, 10, 3 },
			{ 10, 10, 3 },
			{ 10, 4, 3 },
			{ 4, 4, 3 }
	};

	public P2PTunnelFrequencyBakedModel( final VertexFormat format, final TextureAtlasSprite texture )
	{
		this.format = format;
		this.texture = texture;
	}

	@Override
	public List<BakedQuad> getQuads( IBlockState state, EnumFacing side, long rand )
	{
		if( side == null )
		{
			return Collections.emptyList();
		}

		final CableBusRenderState rs = getRenderingState( state );
		short frequency = 0;

		if( rs != null && rs.getPartFlags().get( side ) != null )
		{
			frequency = rs.getPartFlags().get( side ).shortValue();
		}

		return getQuadsForFrequency( frequency );
	}

	private static CableBusRenderState getRenderingState( IBlockState state )
	{
		if( state == null || !( state instanceof IExtendedBlockState ) )
		{
			return null;
		}

		IExtendedBlockState extendedBlockState = (IExtendedBlockState) state;
		return extendedBlockState.getValue( BlockCableBus.RENDER_STATE_PROPERTY );
	}

	private static Map<AEColor, List<BakedQuad>> getCache( int pos )
	{
		switch( pos )
		{
			case 0:
				return color1Cache;
			case 1:
				return color2Cache;
			case 2:
				return color3Cache;
			case 3:
				return color4Cache;
			default:
				throw new RuntimeException( "invalid cache index" );
		}
	}

	private List<BakedQuad> getQuadsForFrequency( final short frequency )
	{
		final List<BakedQuad> out = new ArrayList<>();
		final AEColor[] colors = Platform.p2p().toColors( frequency );
		for( int i = 0; i < 4; ++i )
		{
			final int[] offs = QUAD_OFFSETS[i];
			out.addAll( getCache( i ).computeIfAbsent( colors[i], c ->
			{
				final CubeBuilder cb = new CubeBuilder( this.format );

				cb.setTexture( this.texture );
				cb.setRenderFullBright( true );
				cb.setColorRGB( c.dye.getColorValue() );
				cb.addCube( offs[0], offs[1], offs[2],
						offs[0] + 2, offs[1] + 2, offs[2] + 0.5f );

				return cb.getOutput();
			} ) );
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
