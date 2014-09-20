package appeng.parts;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.parts.IPartCollisionHelper;

public class BusCollisionHelper implements IPartCollisionHelper
{

	final List<AxisAlignedBB> boxes;

	final private ForgeDirection x;
	final private ForgeDirection y;
	final private ForgeDirection z;

	final private Entity entity;
	final private boolean isVisual;

	public BusCollisionHelper(List<AxisAlignedBB> boxes, ForgeDirection x, ForgeDirection y, ForgeDirection z, Entity e, boolean visual) {
		this.boxes = boxes;
		this.x = x;
		this.y = y;
		this.z = z;
		entity = e;
		isVisual = visual;
	}

	public BusCollisionHelper(List<AxisAlignedBB> boxes, ForgeDirection s, Entity e, boolean visual) {
		this.boxes = boxes;
		entity = e;
		isVisual = visual;

		switch (s)
		{
		case DOWN:
			x = ForgeDirection.EAST;
			y = ForgeDirection.NORTH;
			z = ForgeDirection.DOWN;
			break;
		case UP:
			x = ForgeDirection.EAST;
			y = ForgeDirection.SOUTH;
			z = ForgeDirection.UP;
			break;
		case EAST:
			x = ForgeDirection.SOUTH;
			y = ForgeDirection.UP;
			z = ForgeDirection.EAST;
			break;
		case WEST:
			x = ForgeDirection.NORTH;
			y = ForgeDirection.UP;
			z = ForgeDirection.WEST;
			break;
		case NORTH:
			x = ForgeDirection.WEST;
			y = ForgeDirection.UP;
			z = ForgeDirection.NORTH;
			break;
		case SOUTH:
			x = ForgeDirection.EAST;
			y = ForgeDirection.UP;
			z = ForgeDirection.SOUTH;
			break;
		case UNKNOWN:
		default:
			x = ForgeDirection.EAST;
			y = ForgeDirection.UP;
			z = ForgeDirection.SOUTH;
			break;
		}
	}

	@Override
	public boolean isBBCollision()
	{
		return !isVisual;
	}

	/**
	 * pretty much useless...
	 */
	public Entity getEntity()
	{
		return entity;
	}

	@Override
	public void addBox(double minX, double minY, double minZ, double maxX, double maxY, double maxZ)
	{
		minX /= 16.0;
		minY /= 16.0;
		minZ /= 16.0;
		maxX /= 16.0;
		maxY /= 16.0;
		maxZ /= 16.0;

		double aX = minX * x.offsetX + minY * y.offsetX + minZ * z.offsetX;
		double aY = minX * x.offsetY + minY * y.offsetY + minZ * z.offsetY;
		double aZ = minX * x.offsetZ + minY * y.offsetZ + minZ * z.offsetZ;

		double bX = maxX * x.offsetX + maxY * y.offsetX + maxZ * z.offsetX;
		double bY = maxX * x.offsetY + maxY * y.offsetY + maxZ * z.offsetY;
		double bZ = maxX * x.offsetZ + maxY * y.offsetZ + maxZ * z.offsetZ;

		if ( x.offsetX + y.offsetX + z.offsetX < 0 )
		{
			aX += 1;
			bX += 1;
		}

		if ( x.offsetY + y.offsetY + z.offsetY < 0 )
		{
			aY += 1;
			bY += 1;
		}

		if ( x.offsetZ + y.offsetZ + z.offsetZ < 0 )
		{
			aZ += 1;
			bZ += 1;
		}

		minX = Math.min( aX, bX );
		minY = Math.min( aY, bY );
		minZ = Math.min( aZ, bZ );
		maxX = Math.max( aX, bX );
		maxY = Math.max( aY, bY );
		maxZ = Math.max( aZ, bZ );

		boxes.add( AxisAlignedBB.getBoundingBox( minX, minY, minZ, maxX, maxY, maxZ ) );
	}

	@Override
	public ForgeDirection getWorldX()
	{
		return x;
	}

	@Override
	public ForgeDirection getWorldY()
	{
		return y;
	}

	@Override
	public ForgeDirection getWorldZ()
	{
		return z;
	}

}
