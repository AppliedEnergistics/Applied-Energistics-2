package appeng.container.implementations;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.container.AEBaseContainer;
import appeng.helpers.IInterfaceHost;
import appeng.parts.misc.PartInterface;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.misc.TileInterface;

public class ContainerInterfaceTerminal  extends AEBaseContainer{
	
	class InvTracker {
		
		public InvTracker(IInventory patterns) {
			server = patterns;
			client = new AppEngInternalInventory(null,server.getSizeInventory());
		}
		
		IInventory client;
		IInventory server;
	};
	
	Map<IInterfaceHost,InvTracker> diList = new HashMap();
	IGrid g;

	public ContainerInterfaceTerminal(InventoryPlayer ip, IInterfaceHost anchor) {
		super(ip, anchor);
		g = anchor.getActionableNode().getGrid();
	}

	@Override
	public void detectAndSendChanges() {
		super.detectAndSendChanges();

		int total = 0;
		boolean missing=false;

		for ( IGridNode gn : g.getMachines( TileInterface.class ) )
		{
			IInterfaceHost ih = (IInterfaceHost) gn.getMachine();
			missing = missing || ! diList.containsKey( ih );
		}

		for ( IGridNode gn : g.getMachines( PartInterface.class ) )
		{
			IInterfaceHost ih = (IInterfaceHost) gn.getMachine();
			missing = missing || ! diList.containsKey( ih );			
		}

		if ( total != diList.size() || missing )
			regenList();
		else
		{

			for ( Entry<IInterfaceHost,InvTracker> en : diList.entrySet() )
			{
				InvTracker inv = en.getValue();
				for ( int x = 0; x < inv.server.getSizeInventory(); x++ )
				{
					if ( isDifferent( inv.server.getStackInSlot(x), inv.client.getStackInSlot(x) ) )
						syncSlots(en.getKey(), x, x );
				}
			}
			
		}
	}

	private boolean isDifferent(ItemStack a, ItemStack b) {

		if ( a == null && b == null )
			return false;
		
		if ( a == null || b == null )
			return true;
		
		return !a.equals(b);
	}

	private void regenList() {
		
		diList.clear();

		for ( IGridNode gn : g.getMachines( TileInterface.class ) )
		{
			IInterfaceHost ih = (IInterfaceHost) gn.getMachine();
			diList.put( ih, new InvTracker( ih.getInterfaceDuality().getPatterns() ));
		}

		for ( IGridNode gn : g.getMachines( PartInterface.class ) )
		{
			IInterfaceHost ih = (IInterfaceHost) gn.getMachine();
			diList.put( ih,  new InvTracker(ih.getInterfaceDuality().getPatterns()) );
		}

		for ( IInterfaceHost ih : diList.keySet() )
			syncSlots(ih, 0, 8);
	}

	void syncSlots( IInterfaceHost ih, int from, int to )
	{
		
	}

}
