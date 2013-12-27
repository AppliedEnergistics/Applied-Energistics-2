package appeng.tile.networking;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.EnumSet;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ForgeDirection;
import appeng.api.networking.GridFlags;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalCoord;
import appeng.core.Configuration;
import appeng.me.GridAccessException;
import appeng.tile.events.AETileEventHandler;
import appeng.tile.events.TileEventType;
import appeng.tile.grid.AENetworkInvTile;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.InvOperation;

public class TileWireless extends AENetworkInvTile
{

	public static final int POWERED_FLAG = 1;
	public static final int CHANNEL_FLAG = 2;

	final int sides[] = new int[] { 0 };
	AppEngInternalInventory inv = new AppEngInternalInventory( this, 1 );

	public int clientFlags = 0;

	public TileWireless() {
		gridProxy.setFlags( GridFlags.REQURE_CHANNEL );
		addNewHandler( new TileWirelessHandler() );
	}

	@MENetworkEventSubscribe
	public void chanRender(MENetworkChannelsChanged c)
	{
		markForUpdate();
	}

	@MENetworkEventSubscribe
	public void powerRender(MENetworkPowerStatusChange c)
	{
		markForUpdate();
	}

	class TileWirelessHandler extends AETileEventHandler
	{

		public TileWirelessHandler() {
			super( EnumSet.of( TileEventType.NETWORK ) );
		}

		@Override
		public boolean readFromStream(DataInputStream data) throws IOException
		{
			boolean eh = super.readFromStream( data );

			int old = clientFlags;
			clientFlags = data.readByte();

			return eh || old != clientFlags;
		}

		@Override
		public void writeToStream(DataOutputStream data) throws IOException
		{

			clientFlags = 0;

			try
			{
				if ( gridProxy.getEnergy().isNetworkPowered() )
					clientFlags |= POWERED_FLAG;

				if ( gridProxy.getNode().meetsChannelRequirements() )
					clientFlags |= CHANNEL_FLAG;
			}
			catch (GridAccessException e)
			{
				// meh
			}

			data.writeByte( (byte) clientFlags );
		}
	}

	@Override
	public AECableType getCableConnectionType(ForgeDirection dir)
	{
		return AECableType.SMART;
	}

	@Override
	public DimensionalCoord getLocation()
	{
		return new DimensionalCoord( this );
	}

	@Override
	public IInventory getInternalInventory()
	{
		return inv;
	}

	@Override
	public void onReady()
	{
		updatePower();
		super.onReady();
	}

	@Override
	public void onChangeInventory(IInventory inv, int slot, InvOperation mc, ItemStack removed, ItemStack added)
	{
		updatePower();
	}

	private void updatePower()
	{
		ItemStack boosters = inv.getStackInSlot( 0 );
		gridProxy.setIdlePowerUsage( Configuration.instance.wireless_getPowerDrain( boosters == null ? 0 : boosters.stackSize ) );
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int side)
	{
		return sides;
	}

}
