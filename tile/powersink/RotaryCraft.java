package appeng.tile.powersink;

import java.util.EnumSet;

import net.minecraftforge.common.ForgeDirection;
import Reika.RotaryCraft.API.ShaftPowerReceiver;
import appeng.api.config.PowerUnits;
import appeng.tile.events.AETileEventHandler;
import appeng.tile.events.TileEventType;
import appeng.util.Platform;
import cpw.mods.fml.common.Optional.Interface;

@Interface(modid = "RotaryCraft", iface = "Reika.RotaryCraft.API.ShaftPowerReceiver")
public abstract class RotaryCraft extends IC2 implements ShaftPowerReceiver
{

	private int omega = 0;
	private int torque = 0;
	private long power = 0;
	private int alpha = 0;

	class RotaryCraftHandler extends AETileEventHandler
	{

		public RotaryCraftHandler() {
			super( EnumSet.of( TileEventType.TICK ) );
		}

		@Override
		public void Tick()
		{
			injectExternalPower( PowerUnits.WA, power );
		}

	};

	public RotaryCraft() {
		if ( Platform.isServer() )
			addNewHandler( new RotaryCraftHandler() );
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

	@Override
	final public int getMachineX()
	{
		return xCoord;
	}

	@Override
	final public int getMachineY()
	{
		return yCoord;
	}

	@Override
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

	@Override
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

}
