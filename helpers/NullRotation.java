package appeng.helpers;

import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.util.IOrientable;

public class NullRotation implements IOrientable
{

	public NullRotation() {

	}

	@Override
	public void setOrientation(ForgeDirection Forward, ForgeDirection Up)
	{

	}

	@Override
	public ForgeDirection getUp()
	{
		return ForgeDirection.UP;
	}

	@Override
	public ForgeDirection getForward()
	{
		return ForgeDirection.SOUTH;
	}

	@Override
	public boolean canBeRotated()
	{
		return false;
	}
}
