package appeng.container.implementations;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.AEApi;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.CraftingItemList;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.container.AEBaseContainer;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketMEInventoryUpdate;
import appeng.me.cluster.IAECluster;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.tile.crafting.TileCraftingTile;
import appeng.util.Platform;

public class ContainerCraftingCPU extends AEBaseContainer implements IMEMonitorHandlerReceiver<IAEItemStack>
{

	CraftingCPUCluster monitor = null;
	IGrid network;

	IItemList<IAEItemStack> list = AEApi.instance().storage().createItemList();

	public ContainerCraftingCPU(InventoryPlayer ip, TileCraftingTile te) {
		super( ip, null, null );
		IGridHost host = te;// .getGridHost();

		if ( host != null )
		{
			findNode( host, ForgeDirection.UNKNOWN );
			for (ForgeDirection d : ForgeDirection.VALID_DIRECTIONS)
				findNode( host, d );
		}

		if ( te instanceof TileCraftingTile )
		{
			IAECluster c = ((TileCraftingTile) te).getCluster();
			if ( c instanceof CraftingCPUCluster )
			{
				monitor = (CraftingCPUCluster) c;
				if ( monitor != null )
				{
					monitor.getListOfItem( list, CraftingItemList.ALL );
					monitor.addListener( this, null );
				}
			}
		}

		if ( network == null && Platform.isServer() )
			isContainerValid = false;
	}

	public void cancelCrafting()
	{
		if ( monitor != null )
		{
			monitor.cancel();
		}
	}

	private void findNode(IGridHost host, ForgeDirection d)
	{
		if ( network == null )
		{
			IGridNode node = host.getGridNode( d );
			if ( node != null )
				network = node.getGrid();
		}
	}

	int delay = 40;

	@Override
	public void onContainerClosed(EntityPlayer player)
	{
		super.onContainerClosed( player );
		if ( monitor != null )
			monitor.removeListener( this );
	}

	@Override
	public void removeCraftingFromCrafters(ICrafting c)
	{
		super.removeCraftingFromCrafters( c );

		if ( this.crafters.isEmpty() && monitor != null )
			monitor.removeListener( this );
	}

	@Override
	public void detectAndSendChanges()
	{
		if ( Platform.isServer() && !list.isEmpty() )
		{
			try
			{
				PacketMEInventoryUpdate a = new PacketMEInventoryUpdate( (byte) 0 );
				PacketMEInventoryUpdate b = new PacketMEInventoryUpdate( (byte) 1 );
				PacketMEInventoryUpdate c = new PacketMEInventoryUpdate( (byte) 2 );

				for (IAEItemStack out : list)
				{
					a.appendItem( monitor.getItemStack( out, CraftingItemList.STORAGE ) );
					b.appendItem( monitor.getItemStack( out, CraftingItemList.ACTIVE ) );
					c.appendItem( monitor.getItemStack( out, CraftingItemList.PENDING ) );
				}

				list.resetStatus();

				for (Object g : this.crafters)
				{
					if ( g instanceof EntityPlayer )
					{
						if ( !a.isEmpty() )
							NetworkHandler.instance.sendTo( a, (EntityPlayerMP) g );

						if ( !b.isEmpty() )
							NetworkHandler.instance.sendTo( b, (EntityPlayerMP) g );

						if ( !c.isEmpty() )
							NetworkHandler.instance.sendTo( c, (EntityPlayerMP) g );
					}
				}
			}
			catch (IOException e)
			{
				// :P
			}

		}
		super.detectAndSendChanges();
	}

	@Override
	public boolean isValid(Object verificationToken)
	{
		return true;
	}

	@Override
	public void postChange(IBaseMonitor<IAEItemStack> monitor, IAEItemStack change, BaseActionSource actionSource)
	{
		change = change.copy();
		change.setStackSize( 1 );
		list.add( change );
	}

	@Override
	public void onListUpdate()
	{

	}
}
