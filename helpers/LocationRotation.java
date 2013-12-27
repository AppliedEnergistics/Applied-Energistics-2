package appeng.helpers;

import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.ForgeDirection;
import appeng.api.util.IOrientable;

public class LocationRotation implements IOrientable
{

	final IBlockAccess w;
	final int x;
	final int y;
	final int z;

	public LocationRotation(IBlockAccess world, int x, int y, int z) {
		w = world;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	@Override
	public void setOrientation(ForgeDirection Forward, ForgeDirection Up)
	{

	}

	@Override
	public ForgeDirection getUp()
	{
		int num = Math.abs( x + y + z ) % 6;
		return ForgeDirection.getOrientation( num );
	}

	@Override
	public ForgeDirection getForward()
	{
		if ( getUp().offsetY == 0 )
			return ForgeDirection.UP;
		return ForgeDirection.SOUTH;
	}

	@Override
	public boolean canBeRotated()
	{
		return true;
	}
}
