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
import com.mojang.authlib.GameProfile;

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
import net.minecraftforge.client.model.IPerspectiveAwareModel;
import net.minecraftforge.common.model.TRSRTransformation;

import appeng.api.implementations.items.IBiometricCard;
import appeng.api.util.AEColor;
import appeng.client.render.cablebus.CubeBuilder;
import appeng.core.AELog;


class BiometricCardBakedModel implements IPerspectiveAwareModel
{

	private final VertexFormat format;

	private final IBakedModel baseModel;

	private final TextureAtlasSprite texture;

	private final int hash;

	private final Cache<Integer, BiometricCardBakedModel> modelCache;

	private final ImmutableList<BakedQuad> generalQuads;

	BiometricCardBakedModel( VertexFormat format, IBakedModel baseModel, TextureAtlasSprite texture )
	{
		this( format, baseModel, texture, 0, createCache() );
	}

	private BiometricCardBakedModel( VertexFormat format, IBakedModel baseModel, TextureAtlasSprite texture, int hash, Cache<Integer, BiometricCardBakedModel> modelCache )
	{
		this.format = format;
		this.baseModel = baseModel;
		this.texture = texture;
		this.hash = hash;
		this.generalQuads = ImmutableList.copyOf( buildGeneralQuads() );
		this.modelCache = modelCache;
	}

	private static Cache<Integer, BiometricCardBakedModel> createCache() {
		return CacheBuilder.newBuilder()
				.maximumSize( 100 )
				.build();
	}

	@Override
	public List<BakedQuad> getQuads( @Nullable IBlockState state, @Nullable EnumFacing side, long rand )
	{

		List<BakedQuad> quads = baseModel.getQuads( state, side, rand );

		if( side != null )
		{
			return quads;
		}

		List<BakedQuad> result = new ArrayList<>( quads.size() + generalQuads.size() );
		result.addAll( quads );
		result.addAll( generalQuads );
		return result;
	}

	private List<BakedQuad> buildGeneralQuads()
	{
		CubeBuilder builder = new CubeBuilder( this.format );

		builder.setTexture( texture );

		AEColor col = AEColor.values()[Math.abs( 3 + hash ) % AEColor.values().length];
		if( hash == 0 )
		{
			col = AEColor.BLACK;
		}

		for( int x = 0; x < 8; x++ )
		{
			for( int y = 0; y < 6; y++ )
			{
				final boolean isLit;

				// This makes the border always use the darker color
				if( x == 0 || y == 0 || x == 7 || y == 5 )
				{
					isLit = false;
				}
				else
				{
					isLit = ( hash & ( 1 << x ) ) != 0 || ( hash & ( 1 << y ) ) != 0;
				}

				if( isLit )
				{
					builder.setColorRGB( col.mediumVariant );
				}
				else
				{
					final float scale = 0.3f / 255.0f;
					builder.setColorRGB( ( ( col.blackVariant >> 16 ) & 0xff ) * scale, ( ( col.blackVariant >> 8 ) & 0xff ) * scale, ( col.blackVariant & 0xff ) * scale );
				}

				builder.addCube( 4 + x, 6 + y, 7.5f, 4 + x + 1, 6 + y + 1, 8.5f );
			}
		}
		return builder.getOutput();
	}

	@Override
	public boolean isAmbientOcclusion()
	{
		return baseModel.isAmbientOcclusion();
	}

	@Override
	public boolean isGui3d()
	{
		return baseModel.isGui3d();
	}

	@Override
	public boolean isBuiltInRenderer()
	{
		return baseModel.isBuiltInRenderer();
	}

	@Override
	public TextureAtlasSprite getParticleTexture()
	{
		return baseModel.getParticleTexture();
	}

	@Override
	public ItemCameraTransforms getItemCameraTransforms()
	{
		return baseModel.getItemCameraTransforms();
	}

	@Override
	public ItemOverrideList getOverrides()
	{
		return new ItemOverrideList( Collections.emptyList() )
		{
			@Override
			public IBakedModel handleItemState( IBakedModel originalModel, ItemStack stack, World world, EntityLivingBase entity )
			{
				String username = "";
				if( stack.getItem() instanceof IBiometricCard )
				{
					final GameProfile gp = ( (IBiometricCard) stack.getItem() ).getProfile( stack );
					if( gp != null )
					{
						if( gp.getId() != null )
						{
							username = gp.getId().toString();
						}
						else
						{
							username = gp.getName();
						}
					}
				}
				final int hash = !username.isEmpty() ? username.hashCode() : 0;

				// Get hash
				if( hash == 0 )
				{
					return BiometricCardBakedModel.this;
				}

				try
				{
					return modelCache.get( hash, () -> new BiometricCardBakedModel( format, baseModel, texture, hash, modelCache ) );
				}
				catch( ExecutionException e )
				{
					AELog.error( e );
					return BiometricCardBakedModel.this;
				}
			}
		};
	}

	@Override
	public Pair<? extends IBakedModel, Matrix4f> handlePerspective( ItemCameraTransforms.TransformType type )
	{
		// Delegate to the base model if possible
		if( baseModel instanceof IPerspectiveAwareModel )
		{
			IPerspectiveAwareModel pam = (IPerspectiveAwareModel) baseModel;
			Pair<? extends IBakedModel, Matrix4f> pair = pam.handlePerspective( type );
			return Pair.of( this, pair.getValue() );
		}
		return Pair.of( this, TRSRTransformation.identity().getMatrix() );
	}
}
