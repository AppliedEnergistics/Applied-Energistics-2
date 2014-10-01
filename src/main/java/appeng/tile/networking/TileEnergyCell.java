package appeng.tile.networking;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.energy.IAEPowerStorage;
import appeng.api.networking.events.MENetworkPowerStorage;
import appeng.api.networking.events.MENetworkPowerStorage.PowerEventType;
import appeng.api.util.AECableType;
import appeng.me.GridAccessException;
import appeng.tile.TileEvent;
import appeng.tile.events.TileEventType;
import appeng.tile.grid.AENetworkTile;
import appeng.util.SettingsFrom;

public class TileEnergyCell extends AENetworkTile implements IAEPowerStorage
{

	protected double internalCurrentPower = 0.0;
	protected double internalMaxPower = 200000.0;

	private byte currentMeta = -1;

	@Override
	public AECableType getCableConnectionType(ForgeDirection dir)
	{
		return AECableType.COVERED;
	}

	private void changePowerLevel()
	{
		if ( notLoaded() )
			return;

		byte boundMetadata = (byte) (8.0 * (internalCurrentPower / internalMaxPower));

		if ( boundMetadata > 7 )
			boundMetadata = 7;
		if ( boundMetadata < 0 )
			boundMetadata = 0;

		if ( currentMeta != boundMetadata )
		{
			currentMeta = boundMetadata;
			worldObj.setBlockMetadataWithNotify( xCoord, yCoord, zCoord, currentMeta, 2 );
		}
	}

	@TileEvent(TileEventType.WORLD_NBT_WRITE)
	public void writeToNBT_TileEnergyCell(NBTTagCompound data)
	{
		if ( !worldObj.isRemote )
			data.setDouble( "internalCurrentPower", internalCurrentPower );
	}

	@TileEvent(TileEventType.WORLD_NBT_READ)
	public void readFromNBT_TileEnergyCell(NBTTagCompound data)
	{
		internalCurrentPower = data.getDouble( "internalCurrentPower" );
	}

	public TileEnergyCell() {
		gridProxy.setIdlePowerUsage( 0 );
	}

	@Override
	public boolean canBeRotated()
	{
		return false;
	}

	@Override
	final public double injectAEPower(double amt, Actionable mode)
	{
		if ( mode == Actionable.SIMULATE )
		{
			double fakeBattery = internalCurrentPower + amt;
			if ( fakeBattery > internalMaxPower )
			{
				return fakeBattery - internalMaxPower;
			}

			return 0;
		}

		if ( internalCurrentPower < 0.01 && amt > 0.01 )
			gridProxy.getNode().getGrid().postEvent( new MENetworkPowerStorage( this, PowerEventType.PROVIDE_POWER ) );

		internalCurrentPower += amt;
		if ( internalCurrentPower > internalMaxPower )
		{
			amt = internalCurrentPower - internalMaxPower;
			internalCurrentPower = internalMaxPower;

			changePowerLevel();
			return amt;
		}

		changePowerLevel();
		return 0;
	}

	private double extractAEPower(double amt, Actionable mode)
	{
		if ( mode == Actionable.SIMULATE )
		{
			if ( internalCurrentPower > amt )
				return amt;
			return internalCurrentPower;
		}

		boolean wasFull = internalCurrentPower >= internalMaxPower - 0.001;

		if ( wasFull && amt > 0.001 )
		{
			try
			{
				gridProxy.getGrid().postEvent( new MENetworkPowerStorage( this, PowerEventType.REQUEST_POWER ) );
			}
			catch (GridAccessException ignored)
			{

			}
		}

		if ( internalCurrentPower > amt )
		{
			internalCurrentPower -= amt;

			changePowerLevel();
			return amt;
		}

		amt = internalCurrentPower;
		internalCurrentPower = 0;

		changePowerLevel();
		return amt;
	}

	@Override
	final public double extractAEPower(double amt, Actionable mode, PowerMultiplier pm)
	{
		return pm.divide( extractAEPower( pm.multiply( amt ), mode ) );
	}

	@Override
	public double getAEMaxPower()
	{
		return internalMaxPower;
	}

	@Override
	public double getAECurrentPower()
	{
		return internalCurrentPower;
	}

	@Override
	public boolean isAEPublicPowerStorage()
	{
		return true;
	}

	@Override
	public AccessRestriction getPowerFlow()
	{
		return AccessRestriction.READ_WRITE;
	}

	@Override
	public void onReady()
	{
		super.onReady();
		currentMeta = (byte) worldObj.getBlockMetadata( xCoord, yCoord, zCoord );
		changePowerLevel();
	}

	@Override
	public NBTTagCompound downloadSettings(SettingsFrom from)
	{
		if ( from == SettingsFrom.DISMANTLE_ITEM )
		{
			NBTTagCompound tag = new NBTTagCompound();
			tag.setDouble( "internalCurrentPower", internalCurrentPower );
			tag.setDouble( "internalMaxPower", internalMaxPower ); // used for tool tip.
			return tag;
		}
		return null;
	}

	@Override
	public void uploadSettings(SettingsFrom from, NBTTagCompound compound)
	{
		if ( from == SettingsFrom.DISMANTLE_ITEM )
		{
			internalCurrentPower = compound.getDouble( "internalCurrentPower" );
		}
	}
}
