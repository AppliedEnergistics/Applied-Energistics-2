
package appeng.client.render.model;


import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Matrix4f;
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
import net.minecraftforge.client.model.pipeline.IVertexConsumer;
import net.minecraftforge.client.model.pipeline.QuadGatheringTransformer;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;
import net.minecraftforge.common.model.TRSRTransformation;

import appeng.block.AEBaseTileBlock;


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
				final Matrix4f mat = FacingToRotation.get( forward, up ).getMat();

				List<BakedQuad> original = CachingRotatingBakedModel.this.parent.getQuads( key.getLeft(), key.getRight(), 0 );
				List<BakedQuad> rotated = new ArrayList<>();
				for( BakedQuad quad : original )
				{
					VertexFormat format = quad.getFormat();
					UnpackedBakedQuad.Builder builder = new UnpackedBakedQuad.Builder( format );
					VertexRotator rot = new VertexRotator( mat );
					rot.setParent( builder );
					quad.pipe( rot );
					rotated.add( builder.build() );
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

	public enum FacingToRotation
	{

		// DUNSWE
		//@formatter:off
		DOWN_DOWN	( new Vector3f(	0,		0,		0	) ), //NOOP
		DOWN_UP		( new Vector3f(	0,		0,		0	) ), //NOOP
		DOWN_NORTH	( new Vector3f(	-90,	0,		0	) ),
		DOWN_SOUTH	( new Vector3f(	-90,	0,		180	) ),
		DOWN_WEST	( new Vector3f(	-90,	0,		90	) ),
		DOWN_EAST	( new Vector3f(	-90,	0,		-90	) ),
		UP_DOWN		( new Vector3f(	0,		0,		0	) ), //NOOP
		UP_UP		( new Vector3f(	0,		0,		0	) ), //NOOP
		UP_NORTH	( new Vector3f(	90,		0,		180	) ),
		UP_SOUTH	( new Vector3f(	90,		0,		0	) ),
		UP_WEST		( new Vector3f(	90,		0,		90	) ),
		UP_EAST		( new Vector3f(	90,		0,		-90	) ),
		NORTH_DOWN	( new Vector3f(	0,		0,		180	) ),
		NORTH_UP	( new Vector3f(	0,		0,		0	) ),
		NORTH_NORTH	( new Vector3f(	0,		0,		0	) ), //NOOP
		NORTH_SOUTH	( new Vector3f(	0,		0,		0	) ), //NOOP
		NORTH_WEST	( new Vector3f(	0,		0,		90	) ),
		NORTH_EAST	( new Vector3f(	0,		0,		-90	) ),
		SOUTH_DOWN	( new Vector3f(	0,		180,	180	) ),
		SOUTH_UP	( new Vector3f(	0,		180,	0	) ),
		SOUTH_NORTH	( new Vector3f(	0,		0,		0	) ), //NOOP
		SOUTH_SOUTH	( new Vector3f(	0,		0,		0	) ), //NOOP
		SOUTH_WEST	( new Vector3f(	0,		180,	-90	) ),
		SOUTH_EAST	( new Vector3f(	0,		180,	90	) ),
		WEST_DOWN	( new Vector3f(	0,		90,		180	) ),
		WEST_UP		( new Vector3f(	0,		90,		0	) ),
		WEST_NORTH	( new Vector3f(	0,		90,		-90	) ),
		WEST_SOUTH	( new Vector3f(	0,		90,		90	) ),
		WEST_WEST	( new Vector3f(	0,		0,		0	) ), //NOOP
		WEST_EAST	( new Vector3f(	0,		0,		0	) ), //NOOP
		EAST_DOWN	( new Vector3f(	0,		-90,	180	) ),
		EAST_UP		( new Vector3f(	0,		-90,	0	) ),
		EAST_NORTH	( new Vector3f(	0,		-90,	90	) ),
		EAST_SOUTH	( new Vector3f(	0,		-90,	-90	) ),
		EAST_WEST	( new Vector3f(	0,		0,		0	) ), //NOOP
		EAST_EAST	( new Vector3f(	0,		0,		0	) ); //NOOP
		//@formatter:on

		private final Matrix4f mat;

		private FacingToRotation( Vector3f rot )
		{
			this.mat = TRSRTransformation.toVecmath( new org.lwjgl.util.vector.Matrix4f().rotate( (float) Math.toRadians( rot.x ), new org.lwjgl.util.vector.Vector3f( 1, 0, 0 ) ).rotate( (float) Math.toRadians( rot.y ), new org.lwjgl.util.vector.Vector3f( 0, 1, 0 ) ).rotate( (float) Math.toRadians( rot.z ), new org.lwjgl.util.vector.Vector3f( 0, 0, 1 ) ) );
		}

		public Matrix4f getMat()
		{
			return new Matrix4f( this.mat );
		}

		public static FacingToRotation get( EnumFacing forward, EnumFacing up )
		{
			return values()[forward.ordinal() * 6 + up.ordinal()];
		}

	}

	public class VertexRotator extends QuadGatheringTransformer
	{
		private final Matrix4f mat;

		public VertexRotator( Matrix4f mat )
		{
			this.mat = mat;
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
					mat.transform( vec );
					vec.x += 0.5f;
					vec.y += 0.5f;
					vec.z += 0.5f;
					return new float[] { vec.x, vec.y, vec.z };
				case 4:
					Vector4f vecc = new Vector4f( fs[0], fs[1], fs[2], fs[3] );
					vecc.x -= 0.5f;
					vecc.y -= 0.5f;
					vecc.z -= 0.5f;
					mat.transform( vecc );
					vecc.x += 0.5f;
					vecc.y += 0.5f;
					vecc.z += 0.5f;
					return new float[] { vecc.x, vecc.y, vecc.z, vecc.w };

				default:
					return fs;
			}
		}

		public void setQuadTint( int tint )
		{

		}

		@Override
		public void setQuadOrientation( EnumFacing orientation )
		{

		}

		@Override
		public void setApplyDiffuseLighting( boolean diffuse )
		{

		}

	}

}
