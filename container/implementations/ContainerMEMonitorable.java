package appeng.container.implementations;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.network.packet.Packet;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import appeng.api.implementations.IMEChest;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.parts.IPart;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorHandlerReciever;
import appeng.api.storage.IStorageMonitorable;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.container.AEBaseContainer;
import appeng.core.sync.packets.PacketMEInventoryUpdate;
import appeng.helpers.ICellItemViewer;
import appeng.util.Platform;
import appeng.util.item.ItemList;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

public class ContainerMEMonitorable extends AEBaseContainer implements IMEMonitorHandlerReciever<IAEItemStack>
{

	final IMEMonitor<IAEItemStack> monitor;
	final IItemList<IAEItemStack> items = new ItemList<IAEItemStack>();

	public ContainerMEMonitorable(InventoryPlayer ip, IStorageMonitorable montiorable) {
		super( ip, montiorable instanceof TileEntity ? (TileEntity) montiorable : null, montiorable instanceof IPart ? (IPart) montiorable : null );

		if ( Platform.isServer() )
		{
			monitor = montiorable.getItemInventory();
			monitor.addListener( this, null );

			cellInv = monitor;

			if ( montiorable instanceof ICellItemViewer )
				powerSrc = (ICellItemViewer) montiorable;
			else if ( montiorable instanceof IMEChest )
				powerSrc = (IMEChest) montiorable;
			else if ( montiorable instanceof IGridHost )
			{
				IGridNode node = ((IGridHost) montiorable).getGridNode( ForgeDirection.UNKNOWN );
				if ( node != null )
				{
					IGrid g = node.getGrid();
					if ( g != null )
						powerSrc = g.getCache( IEnergyGrid.class );
				}
			}
		}
		else
			monitor = null;

		bindPlayerInventory( ip, 0, 0 );
	}

	@Override
	public void detectAndSendChanges()
	{
		if ( Platform.isServer() )
		{
			if ( !items.isEmpty() )
			{
				try
				{
					IItemList<IAEItemStack> monitorCache = monitor.getStorageList();

					PacketMEInventoryUpdate piu = new PacketMEInventoryUpdate();

					for (IAEItemStack is : items)
					{
						IAEItemStack send = monitorCache.findPrecise( is );
						if ( send == null )
						{
							is.setStackSize( 0 );
							piu.appendItem( is );
						}
						else
							piu.appendItem( send );
					}

					if ( !piu.isEmpty() )
					{
						Packet p = piu.getPacket();
						items.resetStatus();

						for (Object c : this.crafters)
						{
							if ( c instanceof Player )
								PacketDispatcher.sendPacketToPlayer( p, (Player) c );
						}
					}
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}

			super.detectAndSendChanges();
		}
	}

	@Override
	public void addCraftingToCrafters(ICrafting c)
	{
		super.addCraftingToCrafters( c );

		if ( Platform.isServer() && c instanceof Player )
		{
			try
			{
				PacketMEInventoryUpdate piu = new PacketMEInventoryUpdate();
				IItemList<IAEItemStack> monitorCache = monitor.getStorageList();

				int items = 0;
				for (IAEItemStack send : monitorCache)
				{
					if ( items > 2000 )
					{
						items = 0;
						Packet p = piu.getPacket();
						PacketDispatcher.sendPacketToPlayer( p, (Player) c );
						piu = new PacketMEInventoryUpdate();
					}

					items++;
					piu.appendItem( send );
				}

				Packet p = piu.getPacket();
				PacketDispatcher.sendPacketToPlayer( p, (Player) c );
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}

		}
	}

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

		if ( this.crafters.isEmpty() )
			monitor.removeListener( this );
	}

	@Override
	public void postChange(IMEMonitor<IAEItemStack> monitor, IAEItemStack change, BaseActionSource source)
	{
		items.add( change );
	}

	@Override
	public boolean isValid(Object verificationToken)
	{
		return true;
	}

}
