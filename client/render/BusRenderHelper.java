package appeng.client.render;

import java.util.EnumSet;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.Icon;
import net.minecraftforge.common.ForgeDirection;
import appeng.api.AEApi;
import appeng.api.parts.IPartRenderHelper;
import appeng.block.AEBaseBlock;

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

	public ForgeDirection ax;
	public ForgeDirection ay;
	public ForgeDirection az;

	int color = 0xffffff;

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

	@Override
	public void setInvColor(int newColor)
	{
		color = newColor;
	}

	@Override
	public void setTexture(Icon ico)
	{
		blk.getRendererInstance().setTemporaryRenderIcon( ico );
	}

	@Override
	public void setTexture(Icon Down, Icon Up, Icon North, Icon South, Icon West, Icon East)
	{
		Icon list[] = new Icon[6];

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
		bbr.renderInvBlock( EnumSet.allOf( ForgeDirection.class ), blk, Tessellator.instance, color, renderer );
	}

	@Override
	public void renderInventoryFace(Icon icon, ForgeDirection face, RenderBlocks renderer)
	{
		renderer.setRenderBounds( minX / 16.0, minY / 16.0, minZ / 16.0, maxX / 16.0, maxY / 16.0, maxZ / 16.0 );
		setTexture( icon );
		bbr.renderInvBlock( EnumSet.of( face ), blk, Tessellator.instance, color, renderer );
	}

	@Override
	public void renderBlock(int x, int y, int z, RenderBlocks renderer)
	{
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

		int blkId = renderer.blockAccess.getBlockId( x, y, z );
		if ( Block.blocksList[blkId] == null )
			return;

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

	public void renderBlockCurrentBounds(int x, int y, int z, RenderBlocks renderer)
	{
		int blkId = renderer.blockAccess.getBlockId( x, y, z );
		if ( Block.blocksList[blkId] == null )
			return;

		renderer.renderStandardBlock( blk, x, y, z );
	}

	@Override
	public void renderFaceCutout(int x, int y, int z, Icon ico, ForgeDirection face, float edgeThickness, RenderBlocks renderer)
	{
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
	public void renderFace(int x, int y, int z, Icon ico, ForgeDirection face, RenderBlocks renderer)
	{
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
}
