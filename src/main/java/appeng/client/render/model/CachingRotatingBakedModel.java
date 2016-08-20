
package appeng.client.render.model;


import java.util.List;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;

import appeng.api.client.BakingPipeline;
import appeng.client.render.model.pipeline.DoubleFacingQuadRotator;
import appeng.client.render.model.pipeline.ParentQuads;
import appeng.client.render.model.pipeline.TypeTransformer;


public class CachingRotatingBakedModel implements IBakedModel
{

	private static final BakingPipeline<Void, BakedQuad> pipeline = new BakingPipeline<>( new ParentQuads(), TypeTransformer.quads2vecs, new DoubleFacingQuadRotator(), TypeTransformer.vecs2quads );
	private final IBakedModel parent;
	private final LoadingCache<Pair<IBlockState, EnumFacing>, List<BakedQuad>> quadCache;

	public CachingRotatingBakedModel( IBakedModel parent )
	{
		this.parent = parent;
		// 6 (DUNSWE) * 6 (DUNSWE) * 7 (DUNSWE + null) = 252
		this.quadCache = CacheBuilder.newBuilder().maximumSize( 252 ).build( new CacheLoader<Pair<IBlockState, EnumFacing>, List<BakedQuad>>(){

			@Override
			public List<BakedQuad> load( Pair<IBlockState, EnumFacing> key ) throws Exception
			{
				return pipeline.pipe( null, parent, key.getLeft(), key.getRight(), 0 );
			}

		} );
	}

	@Override
	public boolean isAmbientOcclusion()
	{
		return parent.isAmbientOcclusion();
	}

	@Override
	public boolean isGui3d()
	{
		return parent.isGui3d();
	}

	@Override
	public boolean isBuiltInRenderer()
	{
		return parent.isBuiltInRenderer();
	}

	@Override
	public TextureAtlasSprite getParticleTexture()
	{
		return parent.getParticleTexture();
	}

	@Override
	public ItemCameraTransforms getItemCameraTransforms()
	{
		return parent.getItemCameraTransforms();
	}

	@Override
	public ItemOverrideList getOverrides()
	{
		return parent.getOverrides();
	}

	@Override
	public List<BakedQuad> getQuads( IBlockState state, EnumFacing side, long rand )
	{
		if( state == null )
		{
			return parent.getQuads( state, side, rand );
		}
		return quadCache.getUnchecked( new ImmutablePair<IBlockState, EnumFacing>( state, side ) );
	}

}
