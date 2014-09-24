package appeng.tile.powersink;

import net.minecraftforge.common.util.ForgeDirection;
import Reika.RotaryCraft.API.ShaftPowerReceiver;
import appeng.api.config.PowerUnits;
import appeng.tile.TileEvent;
import appeng.tile.events.TileEventType;
import appeng.transformer.annotations.integration.Interface;
import appeng.transformer.annotations.integration.Method;
import appeng.util.Platform;

@Interface(iname = "RotaryCraft", iface = "Reika.RotaryCraft.API.ShaftPowerReceiver")
public abstract class RotaryCraft extends IC2 implements ShaftPowerReceiver
{

	private int omega = 0;
	private int torque = 0;
	private long power = 0;
	private int alpha = 0;

	@TileEvent(TileEventType.TICK)
	@Method(iname = "RotaryCraft")
	public void Tick_RotaryCraft()
	{
		if ( worldObj != null && !worldObj.isRemote && power > 0 )
			injectExternalPower( PowerUnits.WA, power );
	}

	@Override
	final public int getOmega()
	{
		return omega;
	}

	@Override
	final public int getTorque()
	{
		return torque;
	}

	@Override
	final public long getPower()
	{
		return power;
	}

	@Override
	final public String getName()
	{
		return "AE";
	}

	@Override
	final public int getIORenderAlpha()
	{
		return alpha;
	}

	@Override
	final public void setIORenderAlpha(int io)
	{
		alpha = io;
	}

	
	final public int getMachineX()
	{
		return xCoord;
	}

	final public int getMachineY()
	{
		return yCoord;
	}

	final public int getMachineZ()
	{
		return zCoord;
	}

	@Override
	final public void setOmega(int o)
	{
		omega = o;
	}

	@Override
	final public void setTorque(int t)
	{
		torque = t;
	}

	@Override
	final public void setPower(long p)
	{
		if ( Platform.isClient() )
			return;

		power = p;
	}

	final public boolean canReadFromBlock(int x, int y, int z)
	{
		ForgeDirection side = ForgeDirection.UNKNOWN;

		if ( x == xCoord - 1 )
			side = ForgeDirection.WEST;
		else if ( x == xCoord + 1 )
			side = ForgeDirection.EAST;
		else if ( z == zCoord - 1 )
			side = ForgeDirection.NORTH;
		else if ( z == zCoord + 1 )
			side = ForgeDirection.SOUTH;
		else if ( y == yCoord - 1 )
			side = ForgeDirection.DOWN;
		else if ( y == yCoord + 1 )
			side = ForgeDirection.UP;

		return internalCanAcceptPower && getPowerSides().contains( side );
	}

	@Override
	final public boolean isReceiving()
	{
		return true;
	}

	@Override
	final public void noInputMachine()
	{
		power = 0;
		torque = 0;
		omega = 0;
	}

	@Override
	final public boolean canReadFrom(ForgeDirection side)
	{
		return internalCanAcceptPower && getPowerSides().contains( side );
	}

	@Override
	final public int getMinTorque(int available)
	{
		return 0;
	}

}
