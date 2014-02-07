package appeng.client.render;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.ForgeDirection;
import appeng.api.parts.IFacadePart;
import appeng.api.parts.IPart;
import appeng.facade.FacadeContainer;
import appeng.parts.BusCollisionHelper;
import appeng.parts.CableBusContainer;

public class CableRenderHelper
{

	private static final CableRenderHelper instance = new CableRenderHelper();

	public static CableRenderHelper getInstance()
	{
		return instance;
	}

	public void renderStatic(CableBusContainer cableBusContainer, FacadeContainer fc)
	{
		TileEntity te = cableBusContainer.getTile();
		RenderBlocksWorkaround renderer = BusRenderer.instance.renderer;

		if ( renderer.blockAccess == null )
			renderer.blockAccess = Minecraft.getMinecraft().theWorld;

		for (ForgeDirection s : ForgeDirection.values())
		{
			cableBusContainer.setSide( s );

			IPart part = cableBusContainer.getPart( s );
			if ( part != null )
			{
				renderer.renderAllFaces = true;
				part.renderStatic( te.xCoord, te.yCoord, te.zCoord, BusRenderHelper.instance, renderer );

				renderer.faces = EnumSet.allOf( ForgeDirection.class );
				renderer.calculations = true;
				renderer.useTextures = true;
			}
		}

		if ( !fc.isEmpty() )
		{
			/**
			 * snag list of boxes...
			 */
			List<AxisAlignedBB> boxes = new ArrayList();
			for (ForgeDirection s : ForgeDirection.values())
			{
				IPart part = cableBusContainer.getPart( s );
				if ( part != null )
				{
					cableBusContainer.setSide( s );
					BusCollisionHelper bch = new BusCollisionHelper( boxes, BusRenderHelper.instance.ax, BusRenderHelper.instance.ay,
							BusRenderHelper.instance.az, null, true );
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
				IFacadePart fPart = fc.getFacade( s );
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

					cableBusContainer.setSide( s );
					fPart.renderStatic( te.xCoord, te.yCoord, te.zCoord, BusRenderHelper.instance, renderer, fc, b, cableBusContainer.getPart( s ) == null );
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
				switch (s)
				{
				case DOWN:
					BusRenderHelper.instance.ax = ForgeDirection.EAST;
					BusRenderHelper.instance.ay = ForgeDirection.NORTH;
					BusRenderHelper.instance.az = ForgeDirection.DOWN;
					break;
				case UP:
					BusRenderHelper.instance.ax = ForgeDirection.EAST;
					BusRenderHelper.instance.ay = ForgeDirection.SOUTH;
					BusRenderHelper.instance.az = ForgeDirection.UP;
					break;
				case EAST:
					BusRenderHelper.instance.ax = ForgeDirection.SOUTH;
					BusRenderHelper.instance.ay = ForgeDirection.UP;
					BusRenderHelper.instance.az = ForgeDirection.EAST;
					break;
				case WEST:
					BusRenderHelper.instance.ax = ForgeDirection.NORTH;
					BusRenderHelper.instance.ay = ForgeDirection.UP;
					BusRenderHelper.instance.az = ForgeDirection.WEST;
					break;
				case NORTH:
					BusRenderHelper.instance.ax = ForgeDirection.WEST;
					BusRenderHelper.instance.ay = ForgeDirection.UP;
					BusRenderHelper.instance.az = ForgeDirection.NORTH;
					break;
				case SOUTH:
					BusRenderHelper.instance.ax = ForgeDirection.EAST;
					BusRenderHelper.instance.ay = ForgeDirection.UP;
					BusRenderHelper.instance.az = ForgeDirection.SOUTH;
					break;
				case UNKNOWN:
				default:
					BusRenderHelper.instance.ax = ForgeDirection.EAST;
					BusRenderHelper.instance.ay = ForgeDirection.UP;
					BusRenderHelper.instance.az = ForgeDirection.SOUTH;
					break;

				}
				part.renderDynamic( x, y, z, BusRenderHelper.instance, BusRenderer.instance.renderer );
			}
		}
	}

}
