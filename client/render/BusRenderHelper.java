package appeng.client.render;

import java.util.EnumSet;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.AEApi;
import appeng.api.parts.IBoxProvider;
import appeng.api.parts.IPartCollsionHelper;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.parts.ISimplifiedBundle;
import appeng.block.AEBaseBlock;
import appeng.core.AEConfig;
import appeng.core.features.AEFeature;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class BusRenderHelper implements IPartRenderHelper
{

	final public static BusRenderHelper instance = new BusRenderHelper();

	double minX = 0;
	double minY = 0;
	double minZ = 0;
	double maxX = 16;
	double maxY = 16;
	double maxZ = 16;

	AEBaseBlock blk = (AEBaseBlock) AEApi.instance().blocks().blockMultiPart.block();
	BaseBlockRender bbr = new BaseBlockRender();

	private ForgeDirection ax = ForgeDirection.EAST;
	private ForgeDirection ay = ForgeDirection.UP;
	private ForgeDirection az = ForgeDirection.SOUTH;

	int color = 0xffffff;

	class BoundBoxCalculator implements IPartCollsionHelper
	{

		public boolean started = false;

		float minX;
		float minY;
		float minZ;

		float maxX;
		float maxY;
		float maxZ;

		@Override
		public void addBox(double minX, double minY, double minZ, double maxX, double maxY, double maxZ)
		{
			if ( started )
			{
				this.minX = Math.min( this.minX, (float) minX );
				this.minY = Math.min( this.minY, (float) minY );
				this.minZ = Math.min( this.minZ, (float) minZ );
				this.maxX = Math.max( this.maxX, (float) maxX );
				this.maxY = Math.max( this.maxY, (float) maxY );
				this.maxZ = Math.max( this.maxZ, (float) maxZ );
			}
			else
			{
				started = true;
				this.minX = (float) minX;
				this.minY = (float) minY;
				this.minZ = (float) minZ;
				this.maxX = (float) maxX;
				this.maxY = (float) maxY;
				this.maxZ = (float) maxZ;
			}
		}

		@Override
		public ForgeDirection getWorldX()
		{
			return ax;
		}

		@Override
		public ForgeDirection getWorldY()
		{
			return ay;
		}

		@Override
		public ForgeDirection getWorldZ()
		{
			return az;
		}

		@Override
		public boolean isBBCollision()
		{
			return false;
		}

	};

	BoundBoxCalculator bbc = new BoundBoxCalculator();

	int renderingForPass = 0;
	int currentPass = 0;
	int itemsRendered = 0;
	boolean noAlphaPass = AEConfig.instance.isFeatureEnabled( AEFeature.AlphaPass ) == false;

	public int getItemsRendered()
	{
		return itemsRendered;
	}

	public void setPass(int pass)
	{
		renderingForPass = 0;
		currentPass = pass;
		itemsRendered = 0;
	}

	@Override
	public void renderForPass(int pass)
	{
		renderingForPass = pass;
	}

	public boolean renderThis()
	{
		if ( renderingForPass == currentPass || noAlphaPass )
		{
			itemsRendered++;
			return true;
		}
		return false;
	}

	@Override
	public void normalRendering()
	{
		RenderBlocksWorkaround rbw = BusRenderer.instance.renderer;
		rbw.calculations = true;
		rbw.useTextures = true;
		rbw.enableAO = false;
	}

	@Override
	public ISimplifiedBundle useSimpliedRendering(int x, int y, int z, IBoxProvider p, ISimplifiedBundle sim)
	{
		RenderBlocksWorkaround rbw = BusRenderer.instance.renderer;

		if ( sim != null && rbw.similarLighting( blk, rbw.blockAccess, x, y, z, sim ) )
		{
			rbw.populate( sim );
			rbw.faces = EnumSet.allOf( ForgeDirection.class );
			rbw.calculations = false;
			rbw.useTextures = false;

			return sim;
		}
		else
		{
			rbw.calculations = true;
			rbw.faces.clear();

			bbc.started = false;
			if ( p == null )
			{
				bbc.minX = bbc.minY = bbc.minZ = 0;
				bbc.maxX = bbc.maxY = bbc.maxZ = 16;
			}
			else
			{
				p.getBoxes( bbc );

				if ( bbc.minX < 1 )
					bbc.minX = 1;
				if ( bbc.minY < 1 )
					bbc.minY = 1;
				if ( bbc.minZ < 1 )
					bbc.minZ = 1;

				if ( bbc.maxX > 15 )
					bbc.maxX = 15;
				if ( bbc.maxY > 15 )
					bbc.maxY = 15;
				if ( bbc.maxZ > 15 )
					bbc.maxZ = 15;
			}

			setBounds( bbc.minX, bbc.minY, bbc.minZ, bbc.maxX, bbc.maxY, bbc.maxZ );

			bbr.renderBlockBounds( rbw, minX, minY, minZ, maxX, maxY, maxZ, ax, ay, az );
			rbw.renderStandardBlock( blk, x, y, z );

			rbw.faces = EnumSet.allOf( ForgeDirection.class );
			rbw.calculations = false;
			rbw.useTextures = false;

			return rbw.getLightingCache();
		}
	}

	@Override
	public void setBounds(float minx, float miny, float minz, float maxx, float maxy, float maxz)
	{
		minX = minx;
		minY = miny;
		minZ = minz;
		maxX = maxx;
		maxY = maxy;
		maxZ = maxz;
	}

	public double getBound(ForgeDirection side)
	{
		switch (side)
		{
		default:
		case UNKNOWN:
			return 0.5;
		case DOWN:
			return minY;
		case EAST:
			return maxX;
		case NORTH:
			return minZ;
		case SOUTH:
			return maxZ;
		case UP:
			return maxY;
		case WEST:
			return minX;

		}
	}

	@Override
	public void setInvColor(int newColor)
	{
		color = newColor;
	}

	@Override
	public void setTexture(IIcon ico)
	{
		blk.getRendererInstance().setTemporaryRenderIcon( ico );
	}

	@Override
	public void setTexture(IIcon Down, IIcon Up, IIcon North, IIcon South, IIcon West, IIcon East)
	{
		IIcon list[] = new IIcon[6];

		list[0] = Down;
		list[1] = Up;
		list[2] = North;
		list[3] = South;
		list[4] = West;
		list[5] = East;

		blk.getRendererInstance().setTemporaryRenderIcons( list[mapRotation( ForgeDirection.UP ).ordinal()],
				list[mapRotation( ForgeDirection.DOWN ).ordinal()], list[mapRotation( ForgeDirection.SOUTH ).ordinal()],
				list[mapRotation( ForgeDirection.NORTH ).ordinal()], list[mapRotation( ForgeDirection.EAST ).ordinal()],
				list[mapRotation( ForgeDirection.WEST ).ordinal()] );
	}

	public ForgeDirection mapRotation(ForgeDirection dir)
	{
		ForgeDirection forward = az;
		ForgeDirection up = ay;
		ForgeDirection west = ForgeDirection.UNKNOWN;

		if ( forward == null || up == null )
			return dir;

		int west_x = forward.offsetY * up.offsetZ - forward.offsetZ * up.offsetY;
		int west_y = forward.offsetZ * up.offsetX - forward.offsetX * up.offsetZ;
		int west_z = forward.offsetX * up.offsetY - forward.offsetY * up.offsetX;

		for (ForgeDirection dx : ForgeDirection.VALID_DIRECTIONS)
			if ( dx.offsetX == west_x && dx.offsetY == west_y && dx.offsetZ == west_z )
				west = dx;

		if ( dir.equals( forward ) )
			return ForgeDirection.SOUTH;
		if ( dir.equals( forward.getOpposite() ) )
			return ForgeDirection.NORTH;

		if ( dir.equals( up ) )
			return ForgeDirection.UP;
		if ( dir.equals( up.getOpposite() ) )
			return ForgeDirection.DOWN;

		if ( dir.equals( west ) )
			return ForgeDirection.WEST;
		if ( dir.equals( west.getOpposite() ) )
			return ForgeDirection.EAST;

		return ForgeDirection.UNKNOWN;
	}

	@Override
	public void renderInventoryBox(RenderBlocks renderer)
	{
		renderer.setRenderBounds( minX / 16.0, minY / 16.0, minZ / 16.0, maxX / 16.0, maxY / 16.0, maxZ / 16.0 );
		bbr.renderInvBlock( EnumSet.allOf( ForgeDirection.class ), blk, null, Tessellator.instance, color, renderer );
	}

	@Override
	public void renderInventoryFace(IIcon IIcon, ForgeDirection face, RenderBlocks renderer)
	{
		renderer.setRenderBounds( minX / 16.0, minY / 16.0, minZ / 16.0, maxX / 16.0, maxY / 16.0, maxZ / 16.0 );
		setTexture( IIcon );
		bbr.renderInvBlock( EnumSet.of( face ), blk, null, Tessellator.instance, color, renderer );
	}

	@Override
	public void renderBlock(int x, int y, int z, RenderBlocks renderer)
	{
		if ( !renderThis() )
			return;

		AEBaseBlock blk = (AEBaseBlock) AEApi.instance().blocks().blockMultiPart.block();
		BlockRenderInfo info = blk.getRendererInstance();
		ForgeDirection forward = BusRenderHelper.instance.az;
		ForgeDirection up = BusRenderHelper.instance.ay;

		renderer.uvRotateBottom = info.getTexture( ForgeDirection.DOWN ).setFlip( BaseBlockRender.getOrientation( ForgeDirection.DOWN, forward, up ) );
		renderer.uvRotateTop = info.getTexture( ForgeDirection.UP ).setFlip( BaseBlockRender.getOrientation( ForgeDirection.UP, forward, up ) );

		renderer.uvRotateEast = info.getTexture( ForgeDirection.EAST ).setFlip( BaseBlockRender.getOrientation( ForgeDirection.EAST, forward, up ) );
		renderer.uvRotateWest = info.getTexture( ForgeDirection.WEST ).setFlip( BaseBlockRender.getOrientation( ForgeDirection.WEST, forward, up ) );

		renderer.uvRotateNorth = info.getTexture( ForgeDirection.NORTH ).setFlip( BaseBlockRender.getOrientation( ForgeDirection.NORTH, forward, up ) );
		renderer.uvRotateSouth = info.getTexture( ForgeDirection.SOUTH ).setFlip( BaseBlockRender.getOrientation( ForgeDirection.SOUTH, forward, up ) );

		bbr.renderBlockBounds( renderer, minX, minY, minZ, maxX, maxY, maxZ, ax, ay, az );

		renderer.renderStandardBlock( blk, x, y, z );
	}

	@Override
	public Block getBlock()
	{
		return AEApi.instance().blocks().blockMultiPart.block();
	}

	public void prepareBounds(RenderBlocks renderer)
	{
		bbr.renderBlockBounds( renderer, minX, minY, minZ, maxX, maxY, maxZ, ax, ay, az );
	}

	@Override
	public void setFacesToRender(EnumSet<ForgeDirection> faces)
	{
		BusRenderer.instance.renderer.renderFaces = faces;
	}

	public void renderBlockCurrentBounds(int x, int y, int z, RenderBlocks renderer)
	{
		if ( !renderThis() )
			return;

		renderer.renderStandardBlock( blk, x, y, z );
	}

	@Override
	public void renderFaceCutout(int x, int y, int z, IIcon ico, ForgeDirection face, float edgeThickness, RenderBlocks renderer)
	{
		if ( !renderThis() )
			return;

		switch (face)
		{
		case DOWN:
			face = ay.getOpposite();
			break;
		case EAST:
			face = ax;
			break;
		case NORTH:
			face = az.getOpposite();
			break;
		case SOUTH:
			face = az;
			break;
		case UP:
			face = ay;
			break;
		case WEST:
			face = ax.getOpposite();
			break;
		case UNKNOWN:
			break;
		default:
			break;
		}

		bbr.renderCutoutFace( blk, ico, x, y, z, renderer, face, edgeThickness );
	}

	@Override
	public void renderFace(int x, int y, int z, IIcon ico, ForgeDirection face, RenderBlocks renderer)
	{
		if ( !renderThis() )
			return;

		prepareBounds( renderer );
		switch (face)
		{
		case DOWN:
			face = ay.getOpposite();
			break;
		case EAST:
			face = ax;
			break;
		case NORTH:
			face = az.getOpposite();
			break;
		case SOUTH:
			face = az;
			break;
		case UP:
			face = ay;
			break;
		case WEST:
			face = ax.getOpposite();
			break;
		case UNKNOWN:
			break;
		default:
			break;
		}

		bbr.renderFace( x, y, z, blk, ico, renderer, face );
	}

	@Override
	public ForgeDirection getWorldX()
	{
		return ax;
	}

	@Override
	public ForgeDirection getWorldY()
	{
		return ay;
	}

	@Override
	public ForgeDirection getWorldZ()
	{
		return az;
	}

	public void setOrientation(ForgeDirection dx, ForgeDirection dy, ForgeDirection dz)
	{
		ax = dx == null ? ForgeDirection.EAST : dx;
		ay = dy == null ? ForgeDirection.UP : dy;
		az = dz == null ? ForgeDirection.SOUTH : dz;
	}

	public double[] getBounds()
	{
		return new double[] { minX, minY, minZ, maxX, maxY, maxZ };
	}

	public void setBounds(double[] bounds)
	{
		if ( bounds == null || bounds.length != 6 )
			return;

		minX = bounds[0];
		minY = bounds[1];
		minZ = bounds[2];
		maxX = bounds[3];
		maxY = bounds[4];
		maxZ = bounds[5];
	}

}
