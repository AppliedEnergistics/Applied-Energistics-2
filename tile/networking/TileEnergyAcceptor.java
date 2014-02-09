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
						internalCurrentPower += eg
								.injectPower( extractAEPower( powerRequested, Actionable.MODULATE, PowerMultiplier.ONE ), Actionable.MODULATE );
					}
				}
				catch (GridAccessException e)
				{
					// null net, probably bads.
				}

			}
		}
	};

	public TileEnergyAcceptor() {
		gridProxy.setIdlePowerUsage( 1.0 / 16.0 );
		addNewHandler( new TilePowerRelayHandler() );
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
	public int[] getAccessibleSlotsFromSide(int side)
	{
		return sides;
	}

}
