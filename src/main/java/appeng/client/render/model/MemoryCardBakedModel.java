
package appeng.client.render.model;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.model.TRSRTransformation;

import appeng.api.implementations.items.IMemoryCard;
import appeng.api.util.AEColor;
import appeng.client.render.cablebus.CubeBuilder;
import appeng.core.AELog;


class MemoryCardBakedModel implements IBakedModel
{

	private final VertexFormat format;

	private final IBakedModel baseModel;

	private final TextureAtlasSprite texture;

	private final long hash;

	private final Cache<Long, MemoryCardBakedModel> modelCache;

	private final ImmutableList<BakedQuad> generalQuads;

	MemoryCardBakedModel( VertexFormat format, IBakedModel baseModel, TextureAtlasSprite texture )
	{
		this( format, baseModel, texture, 0, createCache() );
	}

	private MemoryCardBakedModel( VertexFormat format, IBakedModel baseModel, TextureAtlasSprite texture, long hash, Cache<Long, MemoryCardBakedModel> modelCache )
	{
		this.format = format;
		this.baseModel = baseModel;
		this.texture = texture;
		this.hash = hash;
		this.generalQuads = ImmutableList.copyOf( this.buildGeneralQuads() );
		this.modelCache = modelCache;
	}

	private static Cache<Long, MemoryCardBakedModel> createCache()
	{
		return CacheBuilder.newBuilder()
				.maximumSize( 100 )
				.build();
	}

	@Override
	public List<BakedQuad> getQuads( @Nullable IBlockState state, @Nullable EnumFacing side, long rand )
	{

		List<BakedQuad> quads = this.baseModel.getQuads( state, side, rand );

		if( side != null )
		{
			return quads;
		}

		List<BakedQuad> result = new ArrayList<>( quads.size() + this.generalQuads.size() );
		result.addAll( quads );
		result.addAll( this.generalQuads );
		return result;
	}

	private List<BakedQuad> buildGeneralQuads()
	{
		CubeBuilder builder = new CubeBuilder( this.format );

		builder.setTexture( this.texture );
		System.out.println( Long.toHexString( this.hash ) );

		for( int x = 0; x < 4; x++ )
		{
			for( int y = 0; y < 2; y++ )
			{
				final long color = this.hash >>> ( x + y * 4 ) * 4 & 0xF;
				final AEColor col = AEColor.values()[(int) color];

				builder.setColorRGB( col.mediumVariant );
				builder.addCube( 7 + x, 8 + y, 7.5f, 7 + x + 1, 8 + y + 1, 8.5f );
			}
		}
		return builder.getOutput();
	}

	@Override
	public boolean isAmbientOcclusion()
	{
		return this.baseModel.isAmbientOcclusion();
	}

	@Override
	public boolean isGui3d()
	{
		return this.baseModel.isGui3d();
	}

	@Override
	public boolean isBuiltInRenderer()
	{
		return this.baseModel.isBuiltInRenderer();
	}

	@Override
	public TextureAtlasSprite getParticleTexture()
	{
		return this.baseModel.getParticleTexture();
	}

	@Override
	public ItemCameraTransforms getItemCameraTransforms()
	{
		return this.baseModel.getItemCameraTransforms();
	}

	@Override
	public ItemOverrideList getOverrides()
	{
		return new ItemOverrideList( Collections.emptyList() )
		{
			@Override
			public IBakedModel handleItemState( IBakedModel originalModel, ItemStack stack, World world, EntityLivingBase entity )
			{
				final long hash;
				if( stack.getItem() instanceof IMemoryCard )
				{
					final IMemoryCard memoryCard = (IMemoryCard) stack.getItem();
					hash = memoryCard.getHash( stack );
				}
				else
				{
					hash = 0x77777777;
				}

				try
				{
					return MemoryCardBakedModel.this.modelCache.get( hash,
							() -> new MemoryCardBakedModel( MemoryCardBakedModel.this.format, MemoryCardBakedModel.this.baseModel, MemoryCardBakedModel.this.texture, hash, MemoryCardBakedModel.this.modelCache ) );
				}
				catch( ExecutionException e )
				{
					AELog.error( e );
					return MemoryCardBakedModel.this;
				}
			}
		};
	}

	@Override
	public Pair<? extends IBakedModel, Matrix4f> handlePerspective( ItemCameraTransforms.TransformType type )
	{
		// Delegate to the base model if possible
		if( this.baseModel instanceof IBakedModel )
		{
			IBakedModel pam = this.baseModel;
			Pair<? extends IBakedModel, Matrix4f> pair = pam.handlePerspective( type );
			return Pair.of( this, pair.getValue() );
		}
		return Pair.of( this, TRSRTransformation.identity().getMatrix() );
	}
}
