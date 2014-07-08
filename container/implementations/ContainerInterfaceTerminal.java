package appeng.container.implementations;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.container.AEBaseContainer;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketCompressedNBT;
import appeng.helpers.DualityInterface;
import appeng.helpers.IInterfaceHost;
import appeng.parts.misc.PartInterface;
import appeng.parts.reporting.PartMonitor;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.misc.TileInterface;
import appeng.util.Platform;

public class ContainerInterfaceTerminal extends AEBaseContainer
{

	/**
	 * this stuff is all server side..
	 */

	static private long autoBase = Long.MIN_VALUE;

	class InvTracker
	{

		long which = autoBase++;
		String unlocalizedName;

		public InvTracker(IInventory patterns, String unlocalizedName) {
			server = patterns;
			client = new AppEngInternalInventory( null, server.getSizeInventory() );
			this.unlocalizedName = unlocalizedName;
		}

		IInventory client;
		IInventory server;

	};

	Map<IInterfaceHost, InvTracker> diList = new HashMap();
	IGrid g;

	public ContainerInterfaceTerminal(InventoryPlayer ip, PartMonitor anchor) {
		super( ip, anchor );

		if ( Platform.isServer() )
			g = anchor.getActionableNode().getGrid();

		bindPlayerInventory( ip, 0, 222 - /* height of playerinventory */82 );
	}

	NBTTagCompound data = new NBTTagCompound();

	@Override
	public void detectAndSendChanges()
	{
		super.detectAndSendChanges();

		if ( g == null )
			return;

		int total = 0;
		boolean missing = false;

		for (IGridNode gn : g.getMachines( TileInterface.class ))
		{
			IInterfaceHost ih = (IInterfaceHost) gn.getMachine();
			missing = missing || !diList.containsKey( ih );
		}

		for (IGridNode gn : g.getMachines( PartInterface.class ))
		{
			IInterfaceHost ih = (IInterfaceHost) gn.getMachine();
			missing = missing || !diList.containsKey( ih );
		}

		if ( total != diList.size() || missing )
			regenList( data );
		else
		{
			for (Entry<IInterfaceHost, InvTracker> en : diList.entrySet())
			{
				InvTracker inv = en.getValue();
				for (int x = 0; x < inv.server.getSizeInventory(); x++)
				{
					if ( isDifferent( inv.server.getStackInSlot( x ), inv.client.getStackInSlot( x ) ) )
						addItems( data, inv, x, 1 );
				}
			}
		}

		if ( !data.hasNoTags() )
		{
			try
			{
				NetworkHandler.instance.sendTo( new PacketCompressedNBT( data ), (EntityPlayerMP) getPlayerInv().player );
			}
			catch (IOException e)
			{
				// :P
			}

			data = new NBTTagCompound();
		}
	}

	private boolean isDifferent(ItemStack a, ItemStack b)
	{
		if ( a == null && b == null )
			return false;

		if ( a == null || b == null )
			return true;

		return !a.equals( b );
	}

	private void regenList(NBTTagCompound data)
	{
		diList.clear();

		for (IGridNode gn : g.getMachines( TileInterface.class ))
		{
			IInterfaceHost ih = (IInterfaceHost) gn.getMachine();
			DualityInterface dual = ih.getInterfaceDuality();
			diList.put( ih, new InvTracker( dual.getPatterns(), dual.getTermName() ) );
		}

		for (IGridNode gn : g.getMachines( PartInterface.class ))
		{
			IInterfaceHost ih = (IInterfaceHost) gn.getMachine();
			DualityInterface dual = ih.getInterfaceDuality();
			diList.put( ih, new InvTracker( dual.getPatterns(), dual.getTermName() ) );
		}

		data.setBoolean( "clear", true );

		for (Entry<IInterfaceHost, InvTracker> en : diList.entrySet())
		{
			InvTracker inv = en.getValue();
			addItems( data, inv, 0, inv.server.getSizeInventory() );
		}
	}

	private void addItems(NBTTagCompound data, InvTracker inv, int offset, int length)
	{
		String name = "=" + Long.toString( inv.which, Character.MAX_RADIX );
		NBTTagCompound invv = data.getCompoundTag( name );

		if ( invv.hasNoTags() )
			invv.setString( "un", inv.unlocalizedName );

		for (int x = 0; x < length; x++)
		{
			NBTTagCompound itemNBT = new NBTTagCompound();

			ItemStack is = inv.server.getStackInSlot( x + offset );

			// "update" client side.
			inv.client.setInventorySlotContents( x + offset, is == null ? null : is.copy() );

			if ( is != null )
				is.writeToNBT( itemNBT );

			invv.setTag( Integer.toString( x + offset ), itemNBT );
		}

		data.setTag( name, invv );
	}
}
