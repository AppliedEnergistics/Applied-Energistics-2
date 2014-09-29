package appeng.client.render;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.parts.IFacadeContainer;
import appeng.api.parts.IFacadePart;
import appeng.api.parts.IPart;
import appeng.parts.BusCollisionHelper;
import appeng.parts.CableBusContainer;

public class CableRenderHelper
{

	private static final CableRenderHelper instance = new CableRenderHelper();

	public static CableRenderHelper getInstance()
	{
		return instance;
	}

	private void setSide(ForgeDirection s)
	{
		ForgeDirection ax, ay, az;

		switch (s)
		{
		case DOWN:
			ax = ForgeDirection.EAST;
			ay = ForgeDirection.NORTH;
			az = ForgeDirection.DOWN;
			break;
		case UP:
			ax = ForgeDirection.EAST;
			ay = ForgeDirection.SOUTH;
			az = ForgeDirection.UP;
			break;
		case EAST:
			ax = ForgeDirection.SOUTH;
			ay = ForgeDirection.UP;
			az = ForgeDirection.EAST;
			break;
		case WEST:
			ax = ForgeDirection.NORTH;
			ay = ForgeDirection.UP;
			az = ForgeDirection.WEST;
			break;
		case NORTH:
			ax = ForgeDirection.WEST;
			ay = ForgeDirection.UP;
			az = ForgeDirection.NORTH;
			break;
		case SOUTH:
			ax = ForgeDirection.EAST;
			ay = ForgeDirection.UP;
			az = ForgeDirection.SOUTH;
			break;
		case UNKNOWN:
		default:
			ax = ForgeDirection.EAST;
			ay = ForgeDirection.UP;
			az = ForgeDirection.SOUTH;
			break;
		}

		BusRenderHelper.instance.setOrientation( ax, ay, az );
	}

	public void renderStatic(CableBusContainer cableBusContainer, IFacadeContainer iFacadeContainer)
	{
		TileEntity te = cableBusContainer.getTile();
		RenderBlocksWorkaround renderer = BusRenderer.instance.renderer;

		if ( renderer.overrideBlockTexture != null )
			BusRenderHelper.instance.setPass( 0 );

		if ( renderer.blockAccess == null )
			renderer.blockAccess = Minecraft.getMinecraft().theWorld;

		for (ForgeDirection s : ForgeDirection.values())
		{
			IPart part = cableBusContainer.getPart( s );
			if ( part != null )
			{
				setSide( s );
				renderer.renderAllFaces = true;

				renderer.flipTexture = false;
				renderer.uvRotateBottom = renderer.uvRotateEast = renderer.uvRotateNorth = renderer.uvRotateSouth = renderer.uvRotateTop = renderer.uvRotateWest = 0;

				part.renderStatic( te.xCoord, te.yCoord, te.zCoord, BusRenderHelper.instance, renderer );

				renderer.faces = EnumSet.allOf( ForgeDirection.class );
				renderer.calculations = true;
				renderer.useTextures = true;
			}
		}

		if ( !iFacadeContainer.isEmpty() )
		{
			/**
			 * snag list of boxes...
			 */
			List<AxisAlignedBB> boxes = new ArrayList<AxisAlignedBB>();
			for (ForgeDirection s : ForgeDirection.values())
			{
				IPart part = cableBusContainer.getPart( s );
				if ( part != null )
				{
					setSide( s );
					BusRenderHelper brh = BusRenderHelper.instance;
					BusCollisionHelper bch = new BusCollisionHelper( boxes, brh.getWorldX(), brh.getWorldY(), brh.getWorldZ(), null, true );
					part.getBoxes( bch );
				}
			}

			boolean useThinFacades = false;
			double min = 2.0 / 16.0;
			double max = 14.0 / 16.0;

			for (AxisAlignedBB bb : boxes)
			{
				int o = 0;
				o += bb.maxX > max ? 1 : 0;
				o += bb.maxY > max ? 1 : 0;
				o += bb.maxZ > max ? 1 : 0;
				o += bb.minX < min ? 1 : 0;
				o += bb.minY < min ? 1 : 0;
				o += bb.minZ < min ? 1 : 0;

				if ( o >= 2 )
					useThinFacades = true;
			}

			for (ForgeDirection s : ForgeDirection.VALID_DIRECTIONS)
			{
				IFacadePart fPart = iFacadeContainer.getFacade( s );
				if ( fPart != null )
				{
					AxisAlignedBB b = null;
					fPart.setThinFacades( useThinFacades );
					AxisAlignedBB pb = fPart.getPrimaryBox();
					for (AxisAlignedBB bb : boxes)
					{
						if ( bb.intersectsWith( pb ) )
						{
							if ( b == null )
								b = bb;
							else
							{
								b.maxX = Math.max( b.maxX, bb.maxX );
								b.maxY = Math.max( b.maxY, bb.maxY );
								b.maxZ = Math.max( b.maxZ, bb.maxZ );
								b.minX = Math.min( b.minX, bb.minX );
								b.minY = Math.min( b.minY, bb.minY );
								b.minZ = Math.min( b.minZ, bb.minZ );
							}
						}
					}

					renderer.flipTexture = false;
					renderer.uvRotateBottom = renderer.uvRotateEast = renderer.uvRotateNorth = renderer.uvRotateSouth = renderer.uvRotateTop = renderer.uvRotateWest = 0;

					setSide( s );
					fPart.renderStatic( te.xCoord, te.yCoord, te.zCoord, BusRenderHelper.instance, renderer, iFacadeContainer, b,
							cableBusContainer.getPart( s ) == null );
				}
			}

			renderer.isFacade = false;
			renderer.enableAO = false;
			renderer.setTexture( null );
			renderer.calculations = true;
		}
	}

	public void renderDynamic(CableBusContainer cableBusContainer, double x, double y, double z)
	{
		for (ForgeDirection s : ForgeDirection.values())
		{
			IPart part = cableBusContainer.getPart( s );
			if ( part != null )
			{
				ForgeDirection ax, ay, az;

				switch (s)
				{
				case DOWN:
					ax = ForgeDirection.EAST;
					ay = ForgeDirection.NORTH;
					az = ForgeDirection.DOWN;
					break;
				case UP:
					ax = ForgeDirection.EAST;
					ay = ForgeDirection.SOUTH;
					az = ForgeDirection.UP;
					break;
				case EAST:
					ax = ForgeDirection.SOUTH;
					ay = ForgeDirection.UP;
					az = ForgeDirection.EAST;
					break;
				case WEST:
					ax = ForgeDirection.NORTH;
					ay = ForgeDirection.UP;
					az = ForgeDirection.WEST;
					break;
				case NORTH:
					ax = ForgeDirection.WEST;
					ay = ForgeDirection.UP;
					az = ForgeDirection.NORTH;
					break;
				case SOUTH:
					ax = ForgeDirection.EAST;
					ay = ForgeDirection.UP;
					az = ForgeDirection.SOUTH;
					break;
				case UNKNOWN:
				default:
					ax = ForgeDirection.EAST;
					ay = ForgeDirection.UP;
					az = ForgeDirection.SOUTH;
					break;
				}

				BusRenderHelper.instance.setOrientation( ax, ay, az );
				part.renderDynamic( x, y, z, BusRenderHelper.instance, BusRenderer.instance.renderer );
			}
		}
	}

}
