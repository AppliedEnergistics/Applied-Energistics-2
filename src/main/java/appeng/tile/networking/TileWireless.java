package appeng.tile.networking;

import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.util.EnumSet;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.implementations.IPowerChannelState;
import appeng.api.implementations.tiles.IWirelessAccessPoint;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalCoord;
import appeng.core.AEConfig;
import appeng.me.GridAccessException;
import appeng.tile.TileEvent;
import appeng.tile.events.TileEventType;
import appeng.tile.grid.AENetworkInvTile;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.InvOperation;
import appeng.util.Platform;

public class TileWireless extends AENetworkInvTile implements IWirelessAccessPoint, IPowerChannelState
{

	public static final int POWERED_FLAG = 1;
	public static final int CHANNEL_FLAG = 2;

	final int sides[] = new int[] { 0 };
	final AppEngInternalInventory inv = new AppEngInternalInventory( this, 1 );

	public int clientFlags = 0;

	public TileWireless() {
		gridProxy.setFlags( GridFlags.REQUIRE_CHANNEL );
		gridProxy.setValidSides( EnumSet.noneOf( ForgeDirection.class ) );
	}

	@Override
	public void setOrientation(ForgeDirection inForward, ForgeDirection inUp)
	{
		super.setOrientation( inForward, inUp );
		gridProxy.setValidSides( EnumSet.of( getForward().getOpposite() ) );
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

	@TileEvent(TileEventType.NETWORK_READ)
	public boolean readFromStream_TileWireless(ByteBuf data) throws IOException
	{
		int old = clientFlags;
		clientFlags = data.readByte();

		return old != clientFlags;
	}

	@TileEvent(TileEventType.NETWORK_WRITE)
	public void writeToStream_TileWireless(ByteBuf data) throws IOException
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
	public void markDirty()
	{
		updatePower();
	}

	private void updatePower()
	{
		gridProxy.setIdlePowerUsage( AEConfig.instance.wireless_getPowerDrain( getBoosters() ) );
	}

	@Override
	public int[] getAccessibleSlotsBySide(ForgeDirection side)
	{
		return sides;
	}

	@Override
	public double getRange()
	{
		return AEConfig.instance.wireless_getMaxRange( getBoosters() );
	}

	@Override
	public boolean isActive()
	{
		if ( Platform.isClient() )
			return isPowered() && (CHANNEL_FLAG == (clientFlags & CHANNEL_FLAG));

		return gridProxy.isActive();
	}

	@Override
	public IGrid getGrid()
	{
		try
		{
			return gridProxy.getGrid();
		}
		catch (GridAccessException e)
		{
			return null;
		}
	}

	private int getBoosters()
	{
		ItemStack boosters = inv.getStackInSlot( 0 );
		return boosters == null ? 0 : boosters.stackSize;
	}

	@Override
	public void onChangeInventory(IInventory inv, int slot, InvOperation mc, ItemStack removed, ItemStack added)
	{
		// :P
	}

	@Override
	public boolean isPowered()
	{
		return POWERED_FLAG == (clientFlags & POWERED_FLAG);
	}

}
