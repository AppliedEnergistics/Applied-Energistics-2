package appeng.tile.networking;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.util.AECableType;
import appeng.me.GridAccessException;
import appeng.tile.events.AETileEventHandler;
import appeng.tile.events.TileEventType;
import appeng.tile.grid.AENetworkPowerTile;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.InvOperation;

public class TileEnergyAcceptor extends AENetworkPowerTile
{

	final int sides[] = new int[] {};
	AppEngInternalInventory inv = new AppEngInternalInventory( this, 0 );

	@Override
	public AECableType getCableConnectionType(ForgeDirection dir)
	{
		return AECableType.COVERED;
	}

	private class TilePowerRelayHandler extends AETileEventHandler
	{

		public TilePowerRelayHandler() {
			super( TileEventType.TICK );
		}

		@Override
		public void Tick()
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
					// null net, probably bads.
				}

			}
		}
	};

	@Override
	protected double funnelPowerIntoStorage(double newPower, Actionable mode)
	{
		try
		{
			IEnergyGrid grid = gridProxy.getEnergy();
			double overFlow = grid.injectPower( newPower, Actionable.SIMULATE );

			if ( mode == Actionable.MODULATE )
				grid.injectPower( Math.max( 0.0, newPower - overFlow ), Actionable.MODULATE );

			return super.funnelPowerIntoStorage( overFlow, mode );
		}
		catch (GridAccessException e)
		{
			return super.funnelPowerIntoStorage( newPower, mode );
		}
	}

	public TileEnergyAcceptor() {
		gridProxy.setIdlePowerUsage( 0.0 );
		addNewHandler( new TilePowerRelayHandler() );
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
