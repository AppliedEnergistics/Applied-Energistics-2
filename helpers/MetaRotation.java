package appeng.helpers;

import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import appeng.api.util.IOrientable;

public class MetaRotation implements IOrientable
{

	final IBlockAccess w;
	final int x;
	final int y;
	final int z;

	public MetaRotation(IBlockAccess world, int x, int y, int z) {
		w = world;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	@Override
	public void setOrientation(ForgeDirection Forward, ForgeDirection Up)
	{
		if ( w instanceof World )
			((World) w).setBlockMetadataWithNotify( x, y, z, Up.ordinal(), 1 + 2 );
		else
			throw new RuntimeException( w.getClass().getName() + " recieved, exptected World" );
	}

	@Override
	public ForgeDirection getUp()
	{
		return ForgeDirection.getOrientation( w.getBlockMetadata( x, y, z ) );
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
