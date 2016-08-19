
package appeng.client.render.model;


import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import com.google.common.base.Objects;
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
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.client.model.pipeline.IVertexConsumer;
import net.minecraftforge.client.model.pipeline.QuadGatheringTransformer;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;

import appeng.block.AEBaseTileBlock;
import appeng.client.render.FacingToRotation;


public class CachingRotatingBakedModel implements IBakedModel
{

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
				final EnumFacing forward = key.getLeft().getValue( AEBaseTileBlock.AE_BLOCK_FORWARD );
				final EnumFacing up = key.getLeft().getValue( AEBaseTileBlock.AE_BLOCK_UP );
				final FacingToRotation f2r = FacingToRotation.get( forward, up );

				List<BakedQuad> original = CachingRotatingBakedModel.this.parent.getQuads( key.getLeft(), f2r.resultingRotate( key.getRight() ), 0 );
				List<BakedQuad> rotated = new ArrayList<>();
				for( BakedQuad quad : original )
				{
					VertexFormat format = quad.getFormat();
					UnpackedBakedQuad.Builder builder = new UnpackedBakedQuad.Builder( format );
					VertexRotator rot = new VertexRotator( f2r, quad.getFace() );
					rot.setParent( builder );
					quad.pipe( rot );
					builder.setQuadOrientation( f2r.rotate( quad.getFace() ) );
					BakedQuad q = builder.build();
					rotated.add( q );
				}
				return rotated;
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

	public class VertexRotator extends QuadGatheringTransformer
	{
		private final FacingToRotation f2r;
		private final EnumFacing face;

		public VertexRotator( FacingToRotation f2r, EnumFacing face )
		{
			this.f2r = f2r;
			this.face = face;
		}

		@Override
		public void setParent( IVertexConsumer parent )
		{
			super.setParent( parent );
			if( Objects.equal( getVertexFormat(), parent.getVertexFormat() ) )
			{
				return;
			}
			setVertexFormat( parent.getVertexFormat() );
		}

		@Override
		protected void processQuad()
		{
			VertexFormat format = parent.getVertexFormat();
			int count = format.getElementCount();

			for( int v = 0; v < 4; v++ )
			{
				for( int e = 0; e < count; e++ )
				{
					VertexFormatElement element = format.getElement( e );
					if( element.getUsage() == VertexFormatElement.EnumUsage.POSITION )
					{
						parent.put( e, transform( quadData[e][v] ) );
					}
					else if( element.getUsage() == VertexFormatElement.EnumUsage.NORMAL )
					{
						parent.put( e, transformNormal( quadData[e][v] ) );
					}
					else
					{
						parent.put( e, quadData[e][v] );
					}
				}
			}
		}

		private float[] transform( float[] fs )
		{
			switch( fs.length )
			{
				case 3:
					Vector3f vec = new Vector3f( fs[0], fs[1], fs[2] );
					vec.x -= 0.5f;
					vec.y -= 0.5f;
					vec.z -= 0.5f;
					f2r.getMat().transform( vec );
					vec.x += 0.5f;
					vec.y += 0.5f;
					vec.z += 0.5f;
					return new float[] { vec.x, vec.y, vec.z };
				case 4:
					Vector4f vecc = new Vector4f( fs[0], fs[1], fs[2], fs[3] );
					vecc.x -= 0.5f;
					vecc.y -= 0.5f;
					vecc.z -= 0.5f;
					f2r.getMat().transform( vecc );
					vecc.x += 0.5f;
					vecc.y += 0.5f;
					vecc.z += 0.5f;
					return new float[] { vecc.x, vecc.y, vecc.z, vecc.w };

				default:
					return fs;
			}
		}

		private float[] transformNormal( float[] fs )
		{
			switch( fs.length )
			{
				case 3:
					Vec3i vec = f2r.rotate( face ).getDirectionVec();
					return new float[] { vec.getX(), vec.getY(), vec.getZ() };
				case 4:
					Vector4f veccc = new Vector4f( fs[0], fs[1], fs[2], fs[3] );
					Vec3i vecc = f2r.rotate( face ).getDirectionVec();
					return new float[] { vecc.getX(), vecc.getY(), vecc.getZ(), veccc.w };

				default:
					return fs;
			}
		}

		public void setQuadTint( int tint )
		{
			parent.setQuadTint( tint );
		}

		@Override
		public void setQuadOrientation( EnumFacing orientation )
		{

		}

		@Override
		public void setApplyDiffuseLighting( boolean diffuse )
		{
			parent.setApplyDiffuseLighting( diffuse );
		}

		@Override
		public void setTexture( TextureAtlasSprite texture )
		{
			parent.setTexture( texture );
		}

	}

}
