package appeng.tile.networking;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.util.AECableType;
import appeng.me.GridAccessException;
import appeng.tile.TileEvent;
import appeng.tile.events.TileEventType;
import appeng.tile.grid.AENetworkPowerTile;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.InvOperation;

public class TileEnergyAcceptor extends AENetworkPowerTile
{

	final int sides[] = new int[] {};
	final static AppEngInternalInventory inv = new AppEngInternalInventory( null, 0 );

	@Override
	public AECableType getCableConnectionType(ForgeDirection dir)
	{
		return AECableType.COVERED;
	}

	@TileEvent(TileEventType.TICK)
	public void Tick_TileEnergyAcceptor()
	{
		if ( internalCurrentPower > 0 )
		{
			try
			{
				IEnergyGrid eg = gridProxy.getEnergy();
				double powerRequested = internalCurrentPower - eg.injectPower( internalCurrentPower, Actionable.SIMULATE );

				if ( powerRequested > 0 )
				{
					eg.injectPower( extractAEPower( powerRequested, Actionable.MODULATE, PowerMultiplier.ONE ), Actionable.MODULATE );
				}
			}
			catch (GridAccessException e)
			{
				// null net, probably bad.
			}

		}
	}

	@Override
	protected double getFunnelPowerDemand(double maxRequired)
	{
		try
		{
			IEnergyGrid grid = gridProxy.getEnergy();
			return grid.getEnergyDemand( maxRequired );
		}
		catch (GridAccessException e)
		{
			return super.getFunnelPowerDemand( maxRequired );
		}
	}

	@Override
	protected double funnelPowerIntoStorage(double newPower, Actionable mode)
	{
		try
		{
			IEnergyGrid grid = gridProxy.getEnergy();
			double leftOver = grid.injectPower( newPower, mode );
			if ( mode == Actionable.SIMULATE )
				return leftOver;
			return 0.0;
		}
		catch (GridAccessException e)
		{
			return super.funnelPowerIntoStorage( newPower, mode );
		}
	}

	public TileEnergyAcceptor() {
		gridProxy.setIdlePowerUsage( 0.0 );
		internalMaxPower = 100;
	}

	@Override
	public IInventory getInternalInventory()
	{
		return inv;
	}

	@Override
	public void onChangeInventory(IInventory inv, int slot, InvOperation mc, ItemStack removed, ItemStack added)
	{

	}

	@Override
	public int[] getAccessibleSlotsBySide(ForgeDirection side)
	{
		return sides;
	}

}
