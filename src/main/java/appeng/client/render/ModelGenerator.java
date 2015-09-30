package appeng.client.render;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import javax.vecmath.Vector3f;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.renderer.block.model.BlockPartFace;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.ModelRotation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.IColoredBakedQuad;

import appeng.block.AEBaseBlock;
import appeng.client.texture.BaseIcon;
import appeng.client.texture.IAESprite;
import appeng.client.texture.MissingIcon;
import appeng.items.AEBaseItem;
import appeng.items.parts.ItemMultiPart;

public class ModelGenerator
{

	private static final class CachedModel implements IBakedModel
	{
		List<BakedQuad>[] faces = new List[6];
		List<BakedQuad> general;

		public CachedModel()
		{
			this.general = new ArrayList<BakedQuad>();
			for ( final EnumFacing f : EnumFacing.VALUES )
				this.faces[f.ordinal()] = new ArrayList<BakedQuad>();
		}
		
		@Override
		public boolean isGui3d()
		{
			return true;
		}

		@Override
		public boolean isBuiltInRenderer()
		{
			return false;
		}

		@Override
		public boolean isAmbientOcclusion()
		{
			return true;
		}

		@Override
		public TextureAtlasSprite getTexture()
		{
			return null;
		}

		@Override
		public ItemCameraTransforms getItemCameraTransforms()
		{
			return ItemCameraTransforms.DEFAULT;
		}

		@Override
		public List getGeneralQuads()
		{
			return this.general;
		}

		@Override
		public List getFaceQuads(
				final EnumFacing p_177551_1_ )
		{
			return this.faces[p_177551_1_.ordinal()];
		}
	}

	public int uvRotateBottom;
	public int uvRotateEast;
	public int uvRotateNorth;
	public int uvRotateSouth;
	public int uvRotateTop;
	public int uvRotateWest;
	public IAESprite overrideBlockTexture;
	public boolean renderAllFaces;

	public double renderMinX;
	public double renderMaxX;
	
	public double renderMinY;
	public double renderMaxY;
	
	public double renderMinZ;
	public double renderMaxZ;

	public IBlockAccess blockAccess;
	
	CachedModel generatedModel = new CachedModel();

	// used to create faces...
	final FaceBakery faceBakery = new FaceBakery();

	float tx=0,ty=0,tz=0;
	final float[] defUVs = { 0, 0, 1, 1 };

	public void setRenderBoundsFromBlock(
			final Block block )
	{
		if ( block == null ) return;

		this.renderMinX = block.getBlockBoundsMinX();
		this.renderMinY = block.getBlockBoundsMinY();
		this.renderMinZ = block.getBlockBoundsMinZ();
		this.renderMaxX = block.getBlockBoundsMaxX();
		this.renderMaxY = block.getBlockBoundsMaxY();
		this.renderMaxZ = block.getBlockBoundsMaxZ();
	}

	public void setRenderBounds(
			final double d,
			final double e,
			final double f,
			final double g,
			final double h,
			final double i )
	{
		this.renderMinX = d;
		this.renderMinY = e;
		this.renderMinZ = f;
		this.renderMaxX = g;
		this.renderMaxY = h;
		this.renderMaxZ = i;
	}

	int color = -1;
	
	public void setBrightness(
			final int i )
	{
		this.brightness =i;
	}

	public void setColorRGBA_F(
			final int r,
			final int g,
			final int b,
			final float a )
	{
		final int alpha = ( int ) ( a * 0xff );
		this.color = alpha << 24 |
				r << 16 |
				b << 8 |
				b;
	}

	public void setColorOpaque_I(
			final int whiteVariant )
	{
		final int alpha = 0xff;
		this.color = //alpha << 24 |
				whiteVariant;
	}
	public void setColorOpaque(
			final int r,
			final int g,
			final int b )
	{
		final int alpha = 0xff;
		this.color =// alpha << 24 |
				r << 16 |
				g << 8 |
				b;
	}
	
	public void setColorOpaque_F(
			final int r,
			final int g,
			final int b )
	{
		final int alpha = 0xff;
		this.color = //alpha << 24 |
				Math.min( 0xff, Math.max( 0, r ) ) << 16 |
				Math.min( 0xff, Math.max( 0, g ) ) << 8 |
				Math.min( 0xff, Math.max( 0, b ) );
	}

	public void setColorOpaque_F(
			final float rf,
			final float bf,
			final float gf )
	{
		final int r = (int)( rf * 0xff );
		final int g = (int)( gf * 0xff );
		final int b = (int)( bf * 0xff );
		final int alpha = 0xff;
		this.color = //alpha << 24 |
				Math.min( 0xff, Math.max( 0, r ) ) << 16 |
				Math.min( 0xff, Math.max( 0, g ) ) << 8 |
				Math.min( 0xff, Math.max( 0, b ) );
	}

	public IAESprite getIcon(
			final ItemStack is )
	{
		final Item it = is.getItem();
		
		if ( it instanceof ItemMultiPart )
			return ( (ItemMultiPart) it).getIcon( is);
		
		final Block blk = Block.getBlockFromItem( it );
		
		if ( blk != null )
			return this.getIcon(blk.getStateFromMeta( is.getMetadata() ))[0];

		if ( it instanceof AEBaseItem )
		{
			final IAESprite ico = ( (AEBaseItem)it ).getIcon(is);
			if ( ico != null ) return ico;
		}
		
		return new MissingIcon( is  );
	}


	public IAESprite[] getIcon(
			final IBlockState state )
	{
		final IAESprite[] out = new IAESprite[6];

		final Block blk = state.getBlock();
		if ( blk instanceof AEBaseBlock )
		{
			final AEBaseBlock base = (AEBaseBlock)blk;
			for ( final EnumFacing face : EnumFacing.VALUES )
				out[face.ordinal()] = base.getIcon( face, state );
		}
		else
		{
			final TextureAtlasSprite spite = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getTexture( state );
			if ( spite == null )
			{
				out[0] =  new MissingIcon( blk );
				out[1] =  new MissingIcon( blk );
				out[2] =  new MissingIcon( blk );
				out[3] =  new MissingIcon( blk );
				out[4] =  new MissingIcon( blk );
				out[5] =  new MissingIcon( blk );
			}
			else
			{
				final IAESprite mySpite = new BaseIcon( spite );
				out[0] =  mySpite;
				out[1] =  mySpite;
				out[2] =  mySpite;
				out[3] =  mySpite;
				out[4] =  mySpite;
				out[5] =  mySpite;
			}
		}
		
		return out;
	}

	public IAESprite[] getIcon( final IBlockAccess world, final BlockPos pos)
	{
		final IBlockState state = world.getBlockState( pos );
		final Block blk = state.getBlock();
		
		if ( blk instanceof AEBaseBlock )
		{
			final IAESprite[] out = new IAESprite[6];

			final AEBaseBlock base = (AEBaseBlock)blk;
			for ( final EnumFacing face : EnumFacing.VALUES )
				out[face.ordinal()] = base.getIcon( world, pos, face );
			
			return out;
		}
		
		return this.getIcon( state );
	}

	int point =0;
	int brightness = -1;
	float[][] points = new float[4][];
	
	public void addVertexWithUV(
			final EnumFacing face,
			final double x,
			final double y,
			final double z,
			final double u,
			final double v )
	{
		this.points[this.point++] = new float[]{ (float)x+ this.tx, (float)y+ this.ty, (float)z+ this.tz, (float)u, (float)v };
		
		if ( this.point == 4 )
		{
			this.brightness = -1;
			final int[] vertData = {
				Float.floatToRawIntBits( this.points[0][0] ),
				Float.floatToRawIntBits( this.points[0][1] ),
				Float.floatToRawIntBits( this.points[0][2] ),
				this.brightness,
				Float.floatToRawIntBits( this.points[0][3] ),
				Float.floatToRawIntBits( this.points[0][4] ),
				0,

				Float.floatToRawIntBits( this.points[1][0] ),
				Float.floatToRawIntBits( this.points[1][1] ),
				Float.floatToRawIntBits( this.points[1][2] ),
				this.brightness,
				Float.floatToRawIntBits( this.points[1][3] ),
				Float.floatToRawIntBits( this.points[1][4] ),
				0,
				
				Float.floatToRawIntBits( this.points[2][0] ),
				Float.floatToRawIntBits( this.points[2][1] ),
				Float.floatToRawIntBits( this.points[2][2] ),
				this.brightness,
				Float.floatToRawIntBits( this.points[2][3] ),
				Float.floatToRawIntBits( this.points[2][4] ),
				0,			
				
				Float.floatToRawIntBits( this.points[3][0] ),
				Float.floatToRawIntBits( this.points[3][1] ),
				Float.floatToRawIntBits( this.points[3][2] ),
				this.brightness,
				Float.floatToRawIntBits( this.points[3][3] ),
				Float.floatToRawIntBits( this.points[3][4] ),
				0,
			};

			this.generatedModel.general.add( new IColoredBakedQuad.ColoredBakedQuad( vertData, this.color, face ));

			this.point =0;
		}
	}

	public boolean renderStandardBlock(
			final Block block,
			final BlockPos pos )
	{
		//setRenderBoundsFromBlock( block );

		final IAESprite[] textures = this.getIcon( this.blockAccess,pos );
		this.setColorOpaque_I( 0xffffff );

		this.renderFaceXNeg( block, pos, textures[EnumFacing.WEST.ordinal()] );
		this.renderFaceXPos( block, pos, textures[EnumFacing.EAST.ordinal()] );
		this.renderFaceYNeg( block, pos, textures[EnumFacing.DOWN.ordinal()] );
		this.renderFaceYPos( block, pos, textures[EnumFacing.UP.ordinal()] );
		this.renderFaceZNeg( block, pos, textures[EnumFacing.NORTH.ordinal()] );
		this.renderFaceZPos( block, pos, textures[EnumFacing.SOUTH.ordinal()] );
		
		return false;
	}

	public void setTranslation(
			final int x,
			final int y,
			final int z )
	{
		this.tx =x;
		this.ty =y;
		this.tz =z;
	}

	public boolean isAlphaPass()
	{
		return MinecraftForgeClient.getRenderLayer() == EnumWorldBlockLayer.TRANSLUCENT;
	}

	final float[] quadsUV = {
			0,
			0,
			1,
			1,
			0,
			0,
			1,
			1
	};
	public EnumSet<EnumFacing> renderFaces = EnumSet.allOf(EnumFacing.class);
	public boolean flipTexture=false;
	private List<SMFace> faces = new ArrayList();
	
	private float[] getFaceUvs(
			final EnumFacing face,
			final Vector3f to_16,
			final Vector3f from_16		 )
	{
		float from_a = 0;
		float from_b = 0;
		float to_a = 0;
		float to_b = 0;

		switch ( face )
		{
			case UP:
				from_a = from_16.x / 16.0f;
				from_b = from_16.z / 16.0f;
				to_a = to_16.x / 16.0f;
				to_b = to_16.z / 16.0f;
				break;
			case DOWN:
				from_a = from_16.x / 16.0f;
				from_b = from_16.z / 16.0f;
				to_a = to_16.x / 16.0f;
				to_b = to_16.z / 16.0f;
				break;
			case SOUTH:
				from_a = from_16.x / 16.0f;
				from_b = from_16.y / 16.0f;
				to_a = to_16.x / 16.0f;
				to_b = to_16.y / 16.0f;
				break;
			case NORTH:
				from_a = from_16.x / 16.0f;
				from_b = from_16.y / 16.0f;
				to_a = to_16.x / 16.0f;
				to_b = to_16.y / 16.0f;
				break;
			case EAST:
				from_a = from_16.y / 16.0f;
				from_b = from_16.z / 16.0f;
				to_a = to_16.y / 16.0f;
				to_b = to_16.z / 16.0f;
				break;
			case WEST:
				from_a = from_16.y / 16.0f;
				from_b = from_16.z / 16.0f;
				to_a = to_16.y / 16.0f;
				to_b = to_16.z / 16.0f;
				break;
			default:
		}

		from_a = 1.0f - from_a;
		from_b = 1.0f - from_b;
		to_a = 1.0f - to_a;
		to_b = 1.0f - to_b;

		final float[] afloat = {// :P
		16.0f * ( this.quadsUV[0] + this.quadsUV[2] * from_a + this.quadsUV[4] * from_b ), // 0
		16.0f * ( this.quadsUV[1] + this.quadsUV[3] * from_a + this.quadsUV[5] * from_b ), // 1

		16.0f * ( this.quadsUV[0] + this.quadsUV[2] * to_a + this.quadsUV[4] * from_b ), // 2
		16.0f * ( this.quadsUV[1] + this.quadsUV[3] * to_a + this.quadsUV[5] * from_b ), // 3

		16.0f * ( this.quadsUV[0] + this.quadsUV[2] * to_a + this.quadsUV[4] * to_b ), // 2
		16.0f * ( this.quadsUV[1] + this.quadsUV[3] * to_a + this.quadsUV[5] * to_b ), // 3

		16.0f * ( this.quadsUV[0] + this.quadsUV[2] * from_a + this.quadsUV[4] * to_b ), // 0
		16.0f * ( this.quadsUV[1] + this.quadsUV[3] * from_a + this.quadsUV[5] * to_b ), // 1
		};

		return afloat;
	}
	
	public void renderFaceXNeg(
			final Block blk,
			final BlockPos pos,
			final IAESprite lights )
	{		
		final boolean isEdge = this.renderMinX < 0.0001;
		final Vector3f to = new Vector3f( (float) this.renderMinX * 16.0f, (float) this.renderMinY * 16.0f, (float) this.renderMinZ * 16.0f);
		final Vector3f from = new Vector3f( (float) this.renderMinX * 16.0f, (float) this.renderMaxY * 16.0f, (float) this.renderMaxZ * 16.0f);

		final EnumFacing myFace = EnumFacing.WEST;
		this.addFace(myFace, isEdge,to,from, this.defUVs,lights );
	}
	
	public void renderFaceYNeg(
			final Block blk,
			final BlockPos pos,
			final IAESprite lights )
	{		
		final boolean isEdge = this.renderMinY < 0.0001;
		final Vector3f to = new Vector3f( (float) this.renderMinX * 16.0f, (float) this.renderMinY * 16.0f, (float) this.renderMinZ * 16.0f );
		final Vector3f from = new Vector3f( (float) this.renderMaxX * 16.0f, (float) this.renderMinY * 16.0f, (float) this.renderMaxZ * 16.0f );

		final EnumFacing myFace = EnumFacing.DOWN;
		this.addFace(myFace, isEdge,to,from, this.defUVs, lights );
	}

	public void renderFaceZNeg(
			final Block blk,
			final BlockPos pos,
			final IAESprite lights )
	{		
		final boolean isEdge = this.renderMinZ < 0.0001;
		final Vector3f to = new Vector3f( (float) this.renderMinX * 16.0f, (float) this.renderMinY * 16.0f, (float) this.renderMinZ * 16.0f );
		final Vector3f from = new Vector3f( (float) this.renderMaxX * 16.0f, (float) this.renderMaxY * 16.0f, (float) this.renderMinZ * 16.0f );

		final EnumFacing myFace = EnumFacing.NORTH;
		this.addFace(myFace, isEdge,to,from, this.defUVs, lights );
	}

	public void renderFaceYPos(
			final Block blk,
			final BlockPos pos,
			final IAESprite lights )
	{		
		final boolean isEdge = this.renderMaxY > 0.9999;
		final Vector3f to = new Vector3f( (float) this.renderMinX * 16.0f, (float) this.renderMaxY * 16.0f, (float) this.renderMinZ * 16.0f );
		final Vector3f from = new Vector3f( (float) this.renderMaxX * 16.0f, (float) this.renderMaxY * 16.0f, (float) this.renderMaxZ * 16.0f);

		final EnumFacing myFace = EnumFacing.UP;
		this.addFace(myFace, isEdge,to,from, this.defUVs,lights );
	}

	public void renderFaceZPos(
			final Block blk,
			final BlockPos pos,
			final IAESprite lights )
	{
		final boolean isEdge = this.renderMaxZ > 0.9999;
		final Vector3f to = new Vector3f( (float) this.renderMinX * 16.0f, (float) this.renderMinY * 16.0f, (float) this.renderMaxZ * 16.0f );
		final Vector3f from = new Vector3f( (float) this.renderMaxX * 16.0f, (float) this.renderMaxY * 16.0f, (float) this.renderMaxZ * 16.0f );

		final EnumFacing myFace = EnumFacing.SOUTH;
		this.addFace(myFace, isEdge,to,from, this.defUVs,lights );
	}

	public void renderFaceXPos(
			final Block blk,
			final BlockPos pos,
			final IAESprite lights )
	{
		final boolean isEdge = this.renderMaxX > 0.9999;
		final Vector3f to = new Vector3f( (float) this.renderMaxX * 16.0f, (float) this.renderMinY * 16.0f, (float) this.renderMinZ * 16.0f );
		final Vector3f from = new Vector3f( (float) this.renderMaxX * 16.0f, (float) this.renderMaxY * 16.0f, (float) this.renderMaxZ * 16.0f );

		final EnumFacing myFace = EnumFacing.EAST;
		this.addFace(myFace, isEdge,to,from, this.defUVs, lights );
	}

	private void addFace(
			final EnumFacing face , final boolean isEdge,
			final Vector3f to,
			final Vector3f from,
			final float[] defUVs2,
			IAESprite texture )
	{
		if ( this.overrideBlockTexture != null )
			texture = this.overrideBlockTexture;

		this.faces.add( new SMFace( face, isEdge, this.color, to, from, defUVs2, new IconUnwrapper( texture ) ) );
	}

	EnumFacing currentFace = EnumFacing.UP;
	
	public void setNormal(
			final float x,
			final float y,
			final float z )
	{
		if ( x > 0.5 )
			this.currentFace = EnumFacing.EAST;
		if ( x < -0.5 )
			this.currentFace = EnumFacing.WEST;
		if ( y > 0.5 )
			this.currentFace = EnumFacing.UP;
		if ( y < -0.5 )
			this.currentFace = EnumFacing.DOWN;
		if ( z > 0.5 )
			this.currentFace = EnumFacing.SOUTH;
		if ( z < -0.5 )
			this.currentFace = EnumFacing.NORTH;
	}

	public void setOverrideBlockTexture(
			final IAESprite object )
	{
		this.overrideBlockTexture = object;
	}

	public void finalizeModel( final boolean Flip )
	{
		ModelRotation mr = ModelRotation.X0_Y0;
		
		if ( Flip )
			  mr = ModelRotation.X0_Y180;
		
		for ( final SMFace face : this.faces )
		{
			final EnumFacing myFace = face.face;
			final float[] uvs = this.getFaceUvs( myFace, face.from, face.to );
			
			final BlockFaceUV uv = new BlockFaceUV( uvs, 0 );
			final BlockPartFace bpf = new BlockPartFace( myFace, face.color, "", uv );
	
			BakedQuad bf = this.faceBakery.makeBakedQuad( face.to, face.from, bpf, face.spite, myFace, mr, null, true, true );
			bf = new IColoredBakedQuad.ColoredBakedQuad( bf.getVertexData(), face.color, bf.getFace() );
			
			if ( face.isEdge )
				this.generatedModel.getFaceQuads( myFace ).add( bf );
			else
				this.generatedModel.getGeneralQuads().add( bf );
		}
	}
	
	public IBakedModel getOutput()
	{
		return this.generatedModel;
	}

}
