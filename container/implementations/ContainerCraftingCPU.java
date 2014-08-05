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
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.container.AEBaseContainer;
import appeng.core.AELog;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketMEInventoryUpdate;
import appeng.core.sync.packets.PacketValueConfig;
import appeng.helpers.ICustomNameObject;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.tile.crafting.TileCraftingTile;
import appeng.util.Platform;

public class ContainerCraftingCPU extends AEBaseContainer implements IMEMonitorHandlerReceiver<IAEItemStack>, ICustomNameObject
{

	CraftingCPUCluster monitor = null;
	String cpuName = null;
	protected IGrid network;

	IItemList<IAEItemStack> list = AEApi.instance().storage().createItemList();

	public ContainerCraftingCPU(InventoryPlayer ip, Object te) {
		super( ip, te );
		IGridHost host = (IGridHost) (te instanceof IGridHost ? te : null);

		if ( host != null )
		{
			findNode( host, ForgeDirection.UNKNOWN );
			for (ForgeDirection d : ForgeDirection.VALID_DIRECTIONS)
				findNode( host, d );
		}

		if ( te instanceof TileCraftingTile )
			setCPU( (ICraftingCPU) ((TileCraftingTile) te).getCluster() );

		if ( network == null && Platform.isServer() )
			isContainerValid = false;
	}

	protected void setCPU(ICraftingCPU c)
	{
		if ( c== monitor)
			return;
		
		if ( monitor != null )
			monitor.removeListener( this );

		for (Object g : this.crafters)
		{
			if ( g instanceof EntityPlayer )
			{
				try
				{
					NetworkHandler.instance.sendTo( new PacketValueConfig( "CraftingStatus", "Clear" ), (EntityPlayerMP) g );
				}
				catch (IOException e)
				{
					AELog.error( e );
				}
			}
		}

		if ( c instanceof CraftingCPUCluster )
		{
			cpuName = ((CraftingCPUCluster) c).getName();

			monitor = (CraftingCPUCluster) c;
			if ( monitor != null )
			{
				list.resetStatus();
				monitor.getListOfItem( list, CraftingItemList.ALL );
				monitor.addListener( this, null );
			}
		}
		else
		{
			monitor = null;
			cpuName = "";
		}
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
		if ( Platform.isServer() && monitor != null && !list.isEmpty() )
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

	@Override
	public String getCustomName()
	{
		return cpuName;
	}

	@Override
	public boolean hasCustomName()
	{
		return cpuName != null && cpuName.length() > 0;
	}
}
